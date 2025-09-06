package com.neuropulse.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UsageMonitorService extends Service {

    public static final String CHANNEL_ID = "UsageMonitorChannel";
    public static final String ACTION_SESSION_UPDATE = "com.neuropulse.app.SESSION_UPDATE";

    private static final String TAG = "UsageMonitorService";

    // Track active sessions
    private Map<String, SessionData> activeSessions = new HashMap<>();
    private AppDatabase database;
    private volatile boolean isMonitoring = true;

    // Helper class to track session data
    private static class SessionData {
        String packageName;
        long startTime;
        String userInstallId;

        SessionData(String packageName, long startTime) {
            this.packageName = packageName;
            this.startTime = startTime;
            this.userInstallId = UUID.randomUUID().toString();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        // Initialize database
        database = AppDatabase.getInstance(this);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Monitoring App Usage")
                .setContentText("Tracking app usage in real-time")
                .setSmallIcon(R.mipmap.ic_launcher) // Using launcher icon as fallback
                .build();

        startForeground(1, notification);

        // Start monitoring in background
        new Thread(this::monitorUsage).start();

        Log.d(TAG, "UsageMonitorService started");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isMonitoring = false;

        // Save any remaining active sessions
        for (SessionData sessionData : activeSessions.values()) {
            saveSession(sessionData, System.currentTimeMillis());
        }
        activeSessions.clear();

        Log.d(TAG, "UsageMonitorService destroyed");
    }

    private void monitorUsage() {
        UsageStatsManager usageStatsManager =
                (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        long lastQueryTime = System.currentTimeMillis() - 10000; // Start 10 seconds ago

        while (isMonitoring) {
            try {
                long currentTime = System.currentTimeMillis();

                UsageEvents events = usageStatsManager.queryEvents(lastQueryTime, currentTime);
                UsageEvents.Event event = new UsageEvents.Event();

                while (events.hasNextEvent()) {
                    events.getNextEvent(event);
                    handleUsageEvent(event);
                }

                lastQueryTime = currentTime;
                Thread.sleep(2000); // Check every 2 seconds for better responsiveness

            } catch (InterruptedException e) {
                Log.e(TAG, "Monitoring thread interrupted", e);
                break;
            } catch (Exception e) {
                Log.e(TAG, "Error in usage monitoring", e);
                try {
                    Thread.sleep(5000); // Wait before retrying
                } catch (InterruptedException ie) {
                    break;
                }
            }
        }
    }

    private void handleUsageEvent(UsageEvents.Event event) {
        String packageName = event.getPackageName();
        long timestamp = event.getTimeStamp();

        // Skip system apps and our own app
        if (packageName.startsWith("android") || packageName.startsWith("com.android") ||
                packageName.equals(getPackageName())) {
            return;
        }

        switch (event.getEventType()) {
            case UsageEvents.Event.ACTIVITY_RESUMED:
                handleAppStarted(packageName, timestamp);
                break;

            case UsageEvents.Event.ACTIVITY_PAUSED:
                handleAppStopped(packageName, timestamp);
                break;
        }
    }

    private void handleAppStarted(String packageName, long timestamp) {
        Log.d(TAG, "App started: " + packageName + " at " + timestamp);

        // If app is already active, end previous session first
        if (activeSessions.containsKey(packageName)) {
            SessionData existingSession = activeSessions.get(packageName);
            saveSession(existingSession, timestamp);
        }

        // Start new session
        activeSessions.put(packageName, new SessionData(packageName, timestamp));

        // Broadcast update for live stats
        Intent intent = new Intent(ACTION_SESSION_UPDATE);
        intent.putExtra("app", packageName);
        intent.putExtra("time", timestamp);
        intent.putExtra("action", "started");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        // Trigger spike detection
        Intent spikeIntent = new Intent(this, SpikeDetectionService.class);
        spikeIntent.putExtra("packageName", packageName);
        startService(spikeIntent);
    }

    private void handleAppStopped(String packageName, long timestamp) {
        Log.d(TAG, "App stopped: " + packageName + " at " + timestamp);

        SessionData sessionData = activeSessions.remove(packageName);
        if (sessionData != null) {
            saveSession(sessionData, timestamp);

            // Broadcast update
            Intent intent = new Intent(ACTION_SESSION_UPDATE);
            intent.putExtra("app", packageName);
            intent.putExtra("time", timestamp);
            intent.putExtra("action", "stopped");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    private void saveSession(SessionData sessionData, long endTime) {
        long duration = endTime - sessionData.startTime;

        // Only save sessions longer than 5 seconds to avoid noise
        if (duration < 5000) {
            Log.d(TAG, "Session too short, not saving: " + sessionData.packageName + " (" + duration + "ms)");
            return;
        }

        // Create session entity
        SessionEntity session = new SessionEntity();
        session.userInstallId = sessionData.userInstallId;
        session.appPackage = sessionData.packageName;
        session.appCategory = Utils.pkgToCategory(sessionData.packageName);
        session.sessionStartTs = sessionData.startTime;
        session.sessionEndTs = endTime;
        session.sessionDurationSec = duration / 1000;
        session.timestamp = System.currentTimeMillis();

        // Set additional fields
        session.duration = duration;
        session.unlocksLastHour = UnlockReceiver.getUnlockCount(this);
        session.notifCountLast30Min = NLService.getNotifCountLast30Min();
        session.nightFlag = Utils.isNight(sessionData.startTime) ? 1 : 0;

        // Calculate addiction risk based on duration and app category
        session.addictionRisk = calculateAddictionRisk(session);

        // Set other flags (simplified logic)
        session.bingeFlag = duration > 1800000 ? 1 : 0; // > 30 minutes
        session.dopamineSpikeLabel = 0; // Will be set by spike detection service
        session.returnAfterNotificationSec = -1; // Not implemented yet
        session.appSwitchCount = 0; // Not implemented yet
        session.consecutiveSameAppMinutes = (int) (duration / 60000);

        // Save to database in background thread
        new Thread(() -> {
            try {
                database.sessionDao().insert(session);
                Log.d(TAG, "Session saved: " + sessionData.packageName +
                        " duration: " + (duration / 1000) + "s");
            } catch (Exception e) {
                Log.e(TAG, "Error saving session", e);
            }
        }).start();
    }

    private String calculateAddictionRisk(SessionEntity session) {
        long durationMinutes = session.duration / 60000;
        String category = session.appCategory;

        if (category.equals("short-video") || category.equals("social")) {
            if (durationMinutes > 60) return "High";
            if (durationMinutes > 20) return "Medium";
            return "Low";
        } else if (category.equals("gaming")) {
            if (durationMinutes > 120) return "High";
            if (durationMinutes > 45) return "Medium";
            return "Low";
        } else {
            if (durationMinutes > 180) return "High";
            if (durationMinutes > 60) return "Medium";
            return "Low";
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Usage Monitor Channel",
                    NotificationManager.IMPORTANCE_LOW // Changed to LOW to be less intrusive
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}