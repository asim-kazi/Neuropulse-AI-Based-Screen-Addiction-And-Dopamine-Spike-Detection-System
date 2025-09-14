// EnhancedDebugActivity.java
package com.neuropulse.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.neuropulse.app.adapters.EnhancedDebugAdapter;
import com.neuropulse.app.database.AppDatabase;
import com.neuropulse.app.database.EnhancedSessionData;
import com.neuropulse.app.features.EnhancedFeatureExtractor;
import com.neuropulse.app.ml.AddictionPredictor;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.neuropulse.app.models.EnhancedDebugInfo;
import android.app.AppOpsManager;
import android.content.Context;
import android.widget.Toast;
import android.util.Log;

public class EnhancedDebugActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EnhancedDebugAdapter debugAdapter;
    private EnhancedFeatureExtractor featureExtractor;
    private AddictionPredictor predictor;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private long sessionStartTime = System.currentTimeMillis();
    private String userId = "user_" + Math.abs(UUID.randomUUID().hashCode() % 1000);

    private static final String TAG = "EnhancedDebugActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enhanced_debug);

        // Check permissions first
        if (!hasUsageStatsPermission()) {
            Toast.makeText(this, "Usage stats permission required", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupComponents();
        startRealTimeMonitoring();
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void setupComponents() {
        recyclerView = findViewById(R.id.recyclerEnhancedDebug);
        debugAdapter = new EnhancedDebugAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(debugAdapter);

        featureExtractor = new EnhancedFeatureExtractor(this);
        predictor = new AddictionPredictor(this, AddictionPredictor.Mode.STANDARD);
    }

    private void startRealTimeMonitoring() {
        Runnable monitoringRunnable = new Runnable() {
            @Override
            public void run() {
                updateEnhancedMetrics();
                mainHandler.postDelayed(this, 3000); // Update every 3 seconds
            }
        };
        mainHandler.post(monitoringRunnable);
    }

    // 🔹 Updated method
    private void updateEnhancedMetrics() {
        executorService.execute(() -> {
            try {
                // Get real-time current app assessment
                EnhancedFeatureExtractor.InstantAddictionAssessment currentAssessment =
                        featureExtractor.getInstantAssessment();

                if (currentAssessment == null) {
                    Log.w(TAG, "No current app assessment available");
                    return;
                }

                // Extract session features with current app info
                long currentTime = System.currentTimeMillis();
                EnhancedSessionData sessionData = featureExtractor.extractFeaturesWithCurrentApp(
                        userId, sessionStartTime, currentTime
                );

                if (sessionData == null) {
                    // Create session data based on current app assessment
                    sessionData = new EnhancedSessionData();
                    sessionData.userId = userId;
                    sessionData.appName = currentAssessment.appName;
                    sessionData.sessionDuration = currentTime - sessionStartTime;
                    sessionData.appCategory = 0; // Will be updated by real-time detector
                    sessionData.dopamineSpikeFlag = currentAssessment.addictionRisk > 0.6f ? 1 : 0;
                    sessionData.addictionFlag = currentAssessment.riskLevel.equals("HIGH") ? 2 :
                            currentAssessment.riskLevel.equals("MEDIUM") ? 1 : 0;
                    sessionData.timestamp = currentTime;
                }

                // Get comprehensive prediction
                AddictionPredictor.PredictionResult prediction = predictor.predictComprehensive(sessionData);

                if (prediction == null) {
                    prediction = new AddictionPredictor.PredictionResult(
                            currentAssessment.addictionRisk,
                            sessionData.addictionFlag,
                            currentAssessment.recommendations,
                            new String[]{"Current app: " + currentAssessment.appName,
                                    "Risk reason: " + currentAssessment.riskReason},
                            0.8f,
                            currentAssessment.riskReason
                    );
                }

                // Create enhanced debug info with current app data
                EnhancedDebugInfo debugInfo = new EnhancedDebugInfoWithCurrentApp(
                        sessionData, prediction, currentAssessment);

                mainHandler.post(() -> {
                    debugAdapter.updateEnhancedInfo(debugInfo);
                });

            } catch (Exception e) {
                Log.e(TAG, "Error updating real-time metrics", e);

                // Fallback to dummy data
                mainHandler.post(() -> {
                    EnhancedSessionData dummyData = createDummySessionData();
                    AddictionPredictor.PredictionResult dummyPrediction = createDummyPrediction();
                    EnhancedDebugInfo debugInfo = new EnhancedDebugInfo(dummyData, dummyPrediction);
                    debugAdapter.updateEnhancedInfo(debugInfo);
                });
            }
        });
    }

    private EnhancedSessionData createDummySessionData() {
        EnhancedSessionData data = new EnhancedSessionData();
        data.userId = userId;
        data.appName = "Current App Detection";
        data.sessionDuration = System.currentTimeMillis() - sessionStartTime;
        data.appCategory = 0;
        data.dopamineSpikeFlag = 0;
        data.addictionFlag = 0;
        data.timestamp = System.currentTimeMillis();
        return data;
    }

    private AddictionPredictor.PredictionResult createDummyPrediction() {
        return new AddictionPredictor.PredictionResult(
                0.1f, 0,
                new String[]{"Initializing real-time detection..."},
                new String[]{"Setting up current app monitoring"},
                0.5f, "System initialization"
        );
    }

    // 🔹 Extended debug info class
    private static class EnhancedDebugInfoWithCurrentApp extends EnhancedDebugInfo {
        private final EnhancedFeatureExtractor.InstantAddictionAssessment currentApp;

        public EnhancedDebugInfoWithCurrentApp(EnhancedSessionData sessionData,
                                               AddictionPredictor.PredictionResult prediction,
                                               EnhancedFeatureExtractor.InstantAddictionAssessment currentApp) {
            super(sessionData, prediction);
            this.currentApp = currentApp;
            prepareEnhancedDisplayData();
        }

        private void prepareEnhancedDisplayData() {
            featureLabels = new String[]{
                    "🔴 CURRENT APP", "⚡ REAL-TIME RISK", "📊 RISK LEVEL", "🎯 RISK REASON",
                    "⏱️ SESSION DURATION", "📱 APP CATEGORY", "🔄 UNLOCK COUNT",
                    "🔔 NOTIFICATIONS", "⏰ TIME OF DAY", "🎮 BINGE FLAG",
                    "📜 SCROLLS/MIN", "🧠 DOPAMINE RISK", "⚠️ ADDICTION LEVEL",
                    "💡 AI RECOMMENDATION", "📈 CONFIDENCE"
            };

            featureValues = new String[]{
                    currentApp.appName,
                    String.format("%.1f%%", currentApp.addictionRisk * 100),
                    currentApp.riskLevel,
                    currentApp.riskReason,
                    formatDuration(sessionData.sessionDuration),
                    getCategoryName(sessionData.appCategory),
                    String.valueOf(sessionData.unlockCount),
                    String.valueOf(sessionData.notifCount),
                    formatTimeOfDay((int)sessionData.timeOfDay),
                    sessionData.bingeFlag == 1 ? "YES" : "NO",
                    String.format("%.1f", sessionData.scrollsPerMinute),
                    String.format("%.2f (%s)", prediction.dopamineRisk, prediction.getRiskLevel()),
                    prediction.getAddictionLevelString(),
                    prediction.recommendations.length > 0 ? prediction.recommendations[0] : "No recommendations",
                    String.format("%.1f%%", prediction.confidence * 100)
            };
        }
    }
    // Helper to format session duration into mm:ss
    private static String formatDuration(long durationMillis) {
        long seconds = durationMillis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // Helper to convert app category int → name
    private static String getCategoryName(int category) {
        switch (category) {
            case 1: return "SOCIAL";
            case 2: return "GAMES";
            case 3: return "PRODUCTIVITY";
            case 4: return "ENTERTAINMENT";
            case 5: return "EDUCATION";
            default: return "OTHER";
        }
    }

    // Helper to format time of day (e.g., morning, evening, etc.)
    private static String formatTimeOfDay(int hour) {
        if (hour < 6) return "NIGHT";
        if (hour < 12) return "MORNING";
        if (hour < 18) return "AFTERNOON";
        return "EVENING";
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
