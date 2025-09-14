package com.neuropulse.app.ml;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.LruCache;
import com.neuropulse.app.database.EnhancedSessionData;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class AddictionPredictor {

    // ---------------- PredictionResult Inner Class ----------------
    public static class PredictionResult {
        public final float dopamineRisk;
        public final int addictionLevel;
        public final String[] recommendations;
        public final String[] insights;
        public final float confidence;
        public final String primaryReason;

        public PredictionResult(float dopamineRisk, int addictionLevel, String[] recommendations,
                                String[] insights, float confidence, String primaryReason) {
            this.dopamineRisk = Math.max(0f, Math.min(1f, dopamineRisk));
            this.addictionLevel = Math.max(0, Math.min(2, addictionLevel));
            this.recommendations = recommendations != null ? recommendations : new String[]{"No recommendations available"};
            this.insights = insights != null ? insights : new String[]{"No insights available"};
            this.confidence = Math.max(0f, Math.min(1f, confidence));
            this.primaryReason = primaryReason != null ? primaryReason : "Unknown";
        }

        public String getRiskLevel() {
            if (dopamineRisk >= 0.7f) return "HIGH";
            else if (dopamineRisk >= 0.4f) return "MEDIUM";
            else return "LOW";
        }

        public String getAddictionLevelString() {
            switch (addictionLevel) {
                case 0: return "Healthy";
                case 1: return "At Risk";
                case 2: return "High Risk";
                default: return "Unknown";
            }
        }
    }

    // ---------------- AddictionPredictor Main Class ----------------
    public enum Mode { STANDARD, OPTIMIZED }

    private static final String TAG = "AddictionPredictor";
    private static final String PREFS_NAME = "ml_predictor";

    private final Context context;
    private final SharedPreferences prefs;
    private final LruCache<String, PredictionResult> predictionCache;

    private long totalPredictions = 0;
    private long cacheHits = 0;

    public AddictionPredictor(Context context, Mode mode) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.predictionCache = new LruCache<>(100);
    }

    // ---------------- Rule-Based Prediction ----------------
    public PredictionResult predictComprehensive(EnhancedSessionData sessionData) {
        if (sessionData == null) {
            return createDefaultPrediction("No data available");
        }

        totalPredictions++;

        try {
            String cacheKey = generateSecureCacheKey(sessionData);
            PredictionResult cached = predictionCache.get(cacheKey);
            if (cached != null) {
                cacheHits++;
                return cached;
            }

            // Rule-based prediction logic
            float dopamineRisk = calculateRuleBasedDopamineRisk(sessionData);
            int addictionLevel = calculateRuleBasedAddictionLevel(sessionData);
            String[] recommendations = generateRuleBasedRecommendations(sessionData, dopamineRisk, addictionLevel);
            String[] insights = generateRuleBasedInsights(sessionData);
            String primaryReason = determineRuleBasedReason(sessionData);

            PredictionResult result = new PredictionResult(
                    dopamineRisk, addictionLevel, recommendations, insights, 0.8f, primaryReason
            );

            predictionCache.put(cacheKey, result);
            if (totalPredictions % 50 == 0) {
                logPerformanceMetrics();
            }

            return result;

        } catch (Exception e) {
            Log.e(TAG, "Prediction failed", e);
            return createDefaultPrediction("Prediction error: " + e.getMessage());
        }
    }

    private float calculateRuleBasedDopamineRisk(EnhancedSessionData data) {
        float risk = 0.0f;

        // Session duration risk (0-0.3)
        long durationHours = data.sessionDuration / (1000 * 60 * 60);
        if (durationHours > 3) risk += 0.3f;
        else if (durationHours > 1) risk += 0.2f;
        else if (durationHours > 0.5) risk += 0.1f;

        // App category risk (0-0.3)
        if (data.appCategory == 0) risk += 0.3f; // Social media
        else if (data.appCategory == 2) risk += 0.25f; // Entertainment
        else if (data.appCategory == 3) risk += 0.2f; // Games

        // Usage intensity risk (0-0.2)
        if (data.scrollsPerMinute > 15) risk += 0.2f;
        else if (data.scrollsPerMinute > 10) risk += 0.15f;
        else if (data.scrollsPerMinute > 5) risk += 0.1f;

        // Time of day risk (0-0.2)
        float hour = data.timeOfDay * 24;
        if (hour > 22 || hour < 6) risk += 0.2f;
        else if (hour > 18) risk += 0.1f;

        return Math.min(1.0f, risk);
    }

    private int calculateRuleBasedAddictionLevel(EnhancedSessionData data) {
        int score = 0;

        long durationHours = data.sessionDuration / (1000 * 60 * 60);
        if (durationHours > 4) score += 3;
        else if (durationHours > 2) score += 2;
        else if (durationHours > 1) score += 1;

        if (data.appCategory == 0) score += 2; // Social media
        else if (data.appCategory == 2 || data.appCategory == 3) score += 1;

        if (data.bingeFlag == 1) score += 2;
        if (data.scrollsPerMinute > 20) score += 1;

        if (data.consecutiveSameApp > 120) score += 2;
        else if (data.consecutiveSameApp > 60) score += 1;

        if (score >= 6) return 2; // High risk
        else if (score >= 3) return 1; // At risk
        else return 0; // Healthy
    }

    private String[] generateRuleBasedRecommendations(EnhancedSessionData data,
                                                      float dopamineRisk, int addictionLevel) {
        List<String> recommendations = new ArrayList<>();

        if (addictionLevel == 2) {
            recommendations.add("Consider taking a break from this app");
            recommendations.add("Try setting app time limits");
        } else if (addictionLevel == 1) {
            recommendations.add("Monitor your usage time");
            recommendations.add("Consider taking short breaks");
        }

        if (dopamineRisk > 0.7f) {
            recommendations.add("High engagement detected - practice mindful usage");
        }

        long durationHours = data.sessionDuration / (1000 * 60 * 60);
        if (durationHours > 2) {
            recommendations.add("Long session detected - consider other activities");
        }

        float hour = data.timeOfDay * 24;
        if (hour > 22 || hour < 6) {
            recommendations.add("Late night usage may affect sleep quality");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Healthy usage pattern detected");
        }

        return recommendations.toArray(new String[0]);
    }

    private String[] generateRuleBasedInsights(EnhancedSessionData data) {
        List<String> insights = new ArrayList<>();

        long durationMinutes = data.sessionDuration / (1000 * 60);
        insights.add("Session duration: " + durationMinutes + " minutes");

        String appType = getCategoryName(data.appCategory);
        insights.add("App category: " + appType);

        if (data.scrollsPerMinute > 10) {
            insights.add("High interaction rate detected");
        }

        if (data.consecutiveSameApp > 60) {
            insights.add("Extended continuous usage");
        }

        return insights.toArray(new String[0]);
    }

    private String getCategoryName(int category) {
        String[] categories = {"Social Media", "Productivity", "Entertainment",
                "Games", "News", "Shopping", "Communication",
                "Health", "Finance", "Utilities"};
        return category < categories.length ? categories[category] : "Other";
    }

    private String determineRuleBasedReason(EnhancedSessionData data) {
        if (data.bingeFlag == 1) return "Binge usage detected";
        if (data.sessionDuration > 3 * 60 * 60 * 1000) return "Extended session duration";
        if (data.appCategory == 0) return "High-stimulation social media usage";
        if (data.scrollsPerMinute > 15) return "High interaction rate";
        if (data.consecutiveSameApp > 120) return "Prolonged continuous usage";
        return "Moderate usage pattern";
    }

    // ---------------- Helper ----------------
    private String generateSecureCacheKey(EnhancedSessionData data) {
        try {
            String keyData = String.format("%s_%d_%d_%d_%.2f_%d",
                    data.appName,
                    data.sessionDuration / 300000,
                    data.appCategory,
                    data.consecutiveSameApp / 10,
                    Math.round(data.timeOfDay * 24) / 24.0,
                    data.bingeFlag);

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(keyData.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString().substring(0, 16);
        } catch (Exception e) {
            return "fallback_" + System.currentTimeMillis() % 10000;
        }
    }

    private PredictionResult createDefaultPrediction(String message) {
        return new PredictionResult(0f, 0, new String[]{message}, new String[]{message}, 0.5f, "Unknown");
    }

    private void logPerformanceMetrics() {
        float hitRate = totalPredictions > 0 ? (cacheHits / (float) totalPredictions) : 0f;
        Log.i(TAG, String.format("Predictions: %d, Cache hit rate: %.1f%%", totalPredictions, hitRate * 100));
    }

    public void resetPredictor() {
        predictionCache.evictAll();
        totalPredictions = 0;
        cacheHits = 0;
        prefs.edit().clear().apply();
    }
}
