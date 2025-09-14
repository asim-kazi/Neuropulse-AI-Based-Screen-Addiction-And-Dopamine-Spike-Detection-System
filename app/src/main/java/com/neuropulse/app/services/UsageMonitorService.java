package com.neuropulse.app.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.neuropulse.app.database.AppDatabase;
import com.neuropulse.app.database.EnhancedSessionData;
import com.neuropulse.app.features.EnhancedFeatureExtractor;
import com.neuropulse.app.ml.AddictionPredictor;
import com.neuropulse.app.utils.PerformanceManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class UsageMonitorService extends Service {
    private static final String TAG = "UsageMonitorService";
    private static final String CHANNEL_ID = "usage_monitor_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static final String PREFS_NAME = "usage_monitor_prefs";

    // Monitoring intervals
    private static final long BASE_MONITOR_INTERVAL = 30_000L; // 30s
    private static final long MAX_MONITOR_INTERVAL = 300_000L; // 5min
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int MAX_CONSECUTIVE_ERRORS = 5;

    // Threading
    private Handler mainHandler;
    private ExecutorService monitoringExecutor;
    private ExecutorService databaseExecutor;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);

    // Core
    private EnhancedFeatureExtractor featureExtractor;
    private AddictionPredictor predictor;
    private AppDatabase database;
    private PerformanceManager performanceManager;
    private SharedPreferences preferences;

    // State
    private volatile long sessionStartTime;
    private volatile String userId;
    private volatile Future<?> currentMonitoringTask;
    private volatile long lastFeatureExtractionTime = 0;
    private volatile EnhancedSessionData cachedSessionData;

    // Real-time
    private volatile EnhancedFeatureExtractor.InstantAddictionAssessment lastAssessment;

    // Performance
    private final AtomicInteger consecutiveErrors = new AtomicInteger(0);
    private volatile long currentMonitoringInterval = BASE_MONITOR_INTERVAL;

    // Regular monitoring runnable
    private final Runnable monitoringRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isRunning.get()) {
                Log.i(TAG, "Service stopped, cancelling monitoring");
                return;
            }
            if (currentMonitoringTask != null && !currentMonitoringTask.isDone()) {
                Log.w(TAG, "Previous monitoring task still running, skipping iteration");
                scheduleNextRun();
                return;
            }
            currentMonitoringTask = performMonitoringAsync();
        }
    };

    // Real-time monitoring runnable
    private final Runnable realTimeMonitoringRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isRunning.get()) return;
            performRealTimeMonitoring();
            mainHandler.postDelayed(this, 5000); // every 5s
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "UsageMonitorService creating...");

        try {
            createNotificationChannel();
            initializeComponents();

            startForeground(NOTIFICATION_ID, createNotification("Initializing monitoring system..."));
            Log.i(TAG, "UsageMonitorService created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to create service", e);
            stopSelf();
        }
    }

    private void initializeComponents() {
        mainHandler = new Handler(Looper.getMainLooper());
        monitoringExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "MonitoringThread");
            t.setPriority(Thread.NORM_PRIORITY - 1);
            return t;
        });
        databaseExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "DatabaseThread");
            t.setPriority(Thread.NORM_PRIORITY - 2);
            return t;
        });

        sessionStartTime = System.currentTimeMillis();
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userId = generateAnonymousUserId();

        featureExtractor = new EnhancedFeatureExtractor(this);
        predictor = new AddictionPredictor(this, AddictionPredictor.Mode.STANDARD);
        performanceManager = PerformanceManager.getInstance(this);

        try {
            database = AppDatabase.getInstance(this);
        } catch (Exception e) {
            Log.e(TAG, "Database initialization failed", e);
        }

        isInitialized.set(true);
    }

    private String generateAnonymousUserId() {
        String existingId = preferences.getString("anonymous_user_id", null);
        if (existingId != null) return existingId;
        String newId = "user_" + Math.abs((Build.MODEL + Build.ID).hashCode() % 100000);
        preferences.edit().putString("anonymous_user_id", newId).apply();
        return newId;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isInitialized.get()) {
            mainHandler.postDelayed(this::startMonitoring, 2000);
        } else {
            startMonitoring();
        }
        return START_STICKY;
    }

    private void startMonitoring() {
        if (isRunning.compareAndSet(false, true)) {
            consecutiveErrors.set(0);
            currentMonitoringInterval = BASE_MONITOR_INTERVAL;

            // Start both session monitoring and real-time app monitoring
            mainHandler.postDelayed(monitoringRunnable, 1000);
            mainHandler.postDelayed(realTimeMonitoringRunnable, 2000);
        }
    }

    private Future<?> performMonitoringAsync() {
        return monitoringExecutor.submit(() -> {
            int retryCount = 0;
            Exception lastException = null;

            while (retryCount < MAX_RETRY_ATTEMPTS && isRunning.get()) {
                try {
                    performMonitoringInternal();
                    consecutiveErrors.set(0);
                    currentMonitoringInterval = BASE_MONITOR_INTERVAL;
                    performanceManager.recordOperation(true);
                    break;
                } catch (Exception e) {
                    lastException = e;
                    retryCount++;
                    int currentErrors = consecutiveErrors.incrementAndGet();
                    Log.e(TAG, "Monitoring failed (attempt " + retryCount + ", consecutive errors: " + currentErrors + ")", e);
                    performanceManager.recordOperation(false);

                    if (currentErrors >= MAX_CONSECUTIVE_ERRORS) {
                        currentMonitoringInterval = Math.min(MAX_MONITOR_INTERVAL, currentMonitoringInterval * 2);
                        if (currentErrors % 10 == 0) performanceManager.forceGarbageCollection();
                    }

                    if (retryCount < MAX_RETRY_ATTEMPTS) {
                        try { Thread.sleep(Math.min(5000, 1000 * retryCount)); }
                        catch (InterruptedException ie) { Thread.currentThread().interrupt(); return; }
                    }
                }
            }

            if (retryCount >= MAX_RETRY_ATTEMPTS && lastException != null) {
                Log.e(TAG, "All retry attempts failed", lastException);
            }

            if (isRunning.get()) scheduleNextRun();
        });
    }

    // --- Modified internal monitoring ---
    private void performMonitoringInternal() {
        long now = System.currentTimeMillis();

        cachedSessionData = featureExtractor.extractFeaturesWithCurrentApp(
                userId, sessionStartTime, now);

        if (cachedSessionData != null && predictor != null) {
            AddictionPredictor.PredictionResult result =
                    predictor.predictComprehensive(cachedSessionData);

            Log.i(TAG, String.format("Session prediction - App: %s, Addiction: %d, Dopamine: %.2f",
                    cachedSessionData.appName, result.addictionLevel, result.dopamineRisk));

            storeSessionForMLTraining(cachedSessionData, result);
        }

        lastFeatureExtractionTime = now;
    }

    // --- Real-time monitoring ---
    private void performRealTimeMonitoring() {
        monitoringExecutor.submit(() -> {
            try {
                EnhancedFeatureExtractor.InstantAddictionAssessment assessment =
                        featureExtractor.getInstantAssessment();

                if (assessment != null) {
                    lastAssessment = assessment;

                    updateNotificationWithCurrentApp(assessment);

                    if (assessment.addictionRisk >= 0.8f) {
                        handleHighRiskDetection(assessment);
                    }

                    Log.d(TAG, String.format("Current app: %s, Risk: %.2f (%s) - %s",
                            assessment.appName, assessment.addictionRisk,
                            assessment.riskLevel, assessment.riskReason));
                }

            } catch (Exception e) {
                Log.e(TAG, "Real-time monitoring failed", e);
            }
        });
    }

    private void updateNotificationWithCurrentApp(EnhancedFeatureExtractor.InstantAddictionAssessment assessment) {
        String notificationText = String.format("Monitoring: %s (%s risk)",
                assessment.appName, assessment.riskLevel.toLowerCase());

        Notification updatedNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Neuropulse - Digital Wellness")
                .setContentText(notificationText)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setColor(getRiskColor(assessment.riskLevel))
                .build();

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, updatedNotification);
        }
    }

    private int getRiskColor(String riskLevel) {
        switch (riskLevel) {
            case "HIGH": return 0xFFFF5722; // Red-orange
            case "MEDIUM": return 0xFFFF9800; // Orange
            case "LOW": return 0xFF4CAF50; // Green
            default: return 0xFF2196F3; // Blue
        }
    }

    private void handleHighRiskDetection(EnhancedFeatureExtractor.InstantAddictionAssessment assessment) {
        databaseExecutor.execute(() -> {
            try {
                EnhancedSessionData riskEvent = new EnhancedSessionData();
                riskEvent.userId = userId;
                riskEvent.appName = assessment.packageName;
                riskEvent.sessionDuration = 0;
                riskEvent.dopamineSpikeFlag = 1;
                riskEvent.addictionFlag = 2; // High risk
                riskEvent.timestamp = assessment.timestamp;

                database.sessionDao().insertEnhancedSession(riskEvent);

                Log.w(TAG, "High addiction risk detected: " + assessment.appName +
                        " - " + assessment.riskReason);

            } catch (Exception e) {
                Log.e(TAG, "Failed to store high-risk event", e);
            }
        });
    }

    public EnhancedFeatureExtractor.InstantAddictionAssessment getCurrentAssessment() {
        return lastAssessment;
    }

    private void scheduleNextRun() {
        mainHandler.postDelayed(monitoringRunnable, currentMonitoringInterval);
    }

    private Notification createNotification(String message) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Neuropulse Monitoring")
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Usage Monitor", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void storeSessionForMLTraining(EnhancedSessionData sessionData,
                                           AddictionPredictor.PredictionResult prediction) {
        databaseExecutor.execute(() -> {
            try {
                database.sessionDao().insertEnhancedSession(sessionData);

                Log.i(TAG, "Stored session: " + sessionData.appName +
                        ", Duration: " + (sessionData.sessionDuration / 1000 / 60) + "min" +
                        ", Risk: " + prediction.dopamineRisk);
            } catch (Exception e) {
                Log.e(TAG, "Failed to store session data", e);
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning.set(false);

        if (currentMonitoringTask != null) {
            currentMonitoringTask.cancel(true);
        }
        if (monitoringExecutor != null) monitoringExecutor.shutdownNow();
        if (databaseExecutor != null) databaseExecutor.shutdownNow();

        Log.i(TAG, "UsageMonitorService destroyed");
    }
}
