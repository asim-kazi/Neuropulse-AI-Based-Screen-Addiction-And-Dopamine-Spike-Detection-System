// EnhancedFeatureExtractor.java (UPDATED VERSION)
package com.neuropulse.app.features;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.LruCache;

import com.neuropulse.app.database.EnhancedSessionData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class EnhancedFeatureExtractor {
    private static final String TAG = "EnhancedFeatureExtractor";
    private static final String PREFS_NAME = "feature_cache";
    private static final int MAX_CACHE_SIZE = 100;

    private final Context context;
    private final UsageStatsManager usageStatsManager;
    private final SharedPreferences prefs;
    private final ReentrantLock cacheLock = new ReentrantLock();

    private final LruCache<String, Integer> appCategoryCache;
    private final ConcurrentHashMap<String, AppUsageTracker> appUsageTrackers;
    private final NotificationTracker notificationTracker;
    private final RealTimeAppDetector realTimeDetector; // NEW

    public EnhancedFeatureExtractor(Context context) {
        this.context = context.getApplicationContext();
        this.usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        this.appCategoryCache = new LruCache<>(MAX_CACHE_SIZE);
        this.appUsageTrackers = new ConcurrentHashMap<>();
        this.notificationTracker = new NotificationTracker();
        this.realTimeDetector = new RealTimeAppDetector(context); // NEW

        initializeAppCategories();
        cleanupOldTrackers();
    }

    /**
     * NEW METHOD: Get current app with real-time addiction risk
     */
    public RealTimeAppDetector.CurrentAppInfo getCurrentAppRisk() {
        return realTimeDetector.getCurrentAppWithRisk();
    }

    /**
     * NEW METHOD: Get instant addiction assessment for current app
     */
    public InstantAddictionAssessment getInstantAssessment() {
        RealTimeAppDetector.CurrentAppInfo currentApp = getCurrentAppRisk();

        return new InstantAddictionAssessment(
                currentApp.packageName,
                currentApp.displayName,
                currentApp.addictionRisk,
                currentApp.getRiskLevel(),
                currentApp.riskReason,
                generateInstantRecommendations(currentApp),
                System.currentTimeMillis()
        );
    }

    /**
     * MODIFIED METHOD: Enhanced feature extraction with real-time current app
     */
    public EnhancedSessionData extractFeaturesWithCurrentApp(String userId, long sessionStart, long sessionEnd) {
        if (userId == null || userId.trim().isEmpty()) return null;
        if (sessionStart >= sessionEnd) return null;

        try {
            // Get current app information
            RealTimeAppDetector.CurrentAppInfo currentAppInfo = getCurrentAppRisk();

            // Extract session features normally
            EnhancedSessionData sessionData = extractFeatures(userId, sessionStart, sessionEnd);

            if (sessionData != null) {
                // Override with current app information
                sessionData.appName = currentAppInfo.displayName;
                sessionData.appCategory = currentAppInfo.category;

                // Set dopamine spike flag based on current app risk
                sessionData.dopamineSpikeFlag = currentAppInfo.addictionRisk > 0.6f ? 1 : 0;

                // Update addiction flag based on risk level
                if (currentAppInfo.addictionRisk >= 0.7f) {
                    sessionData.addictionFlag = 2; // High risk
                } else if (currentAppInfo.addictionRisk >= 0.4f) {
                    sessionData.addictionFlag = 1; // At risk
                } else {
                    sessionData.addictionFlag = 0; // Healthy
                }
            } else {
                // Create new session data with current app info
                sessionData = createSessionDataFromCurrentApp(userId, sessionStart, sessionEnd, currentAppInfo);
            }

            return sessionData;

        } catch (Exception e) {
            Log.e(TAG, "Error in enhanced feature extraction", e);
            return createDummySessionData(userId, sessionStart, sessionEnd);
        }
    }

    // Helper method to create session data from current app info
    private EnhancedSessionData createSessionDataFromCurrentApp(String userId, long sessionStart,
                                                                long sessionEnd,
                                                                RealTimeAppDetector.CurrentAppInfo currentApp) {
        EnhancedSessionData sessionData = new EnhancedSessionData();
        sessionData.userId = userId;
        sessionData.appName = currentApp.displayName;
        sessionData.appCategory = currentApp.category;
        sessionData.sessionDuration = sessionEnd - sessionStart;
        sessionData.unlockFrequency = 5; // Default value
        sessionData.scrollsPerMinute = 10.0f; // Default value
        sessionData.consecutiveSameApp = 30; // Default value
        sessionData.timeOfDay = (sessionStart % TimeUnit.DAYS.toMillis(1)) / (float) TimeUnit.DAYS.toMillis(1);
        sessionData.bingeFlag = currentApp.addictionRisk > 0.7f ? 1 : 0;
        sessionData.dopamineSpikeFlag = currentApp.addictionRisk > 0.6f ? 1 : 0;
        sessionData.addictionFlag = currentApp.addictionRisk >= 0.7f ? 2 :
                currentApp.addictionRisk >= 0.4f ? 1 : 0;
        sessionData.timestamp = sessionEnd;
        return sessionData;
    }

    private String[] generateInstantRecommendations(RealTimeAppDetector.CurrentAppInfo appInfo) {
        List<String> recommendations = new ArrayList<>();

        if (appInfo.addictionRisk >= 0.8f) {
            recommendations.add("Consider taking a break - high addiction risk detected");
            recommendations.add("Try the 20-20-20 rule: look away every 20 minutes");
            recommendations.add("Set a usage timer for this session");
        } else if (appInfo.addictionRisk >= 0.5f) {
            recommendations.add("Monitor your usage time with this app");
            recommendations.add("Consider taking short breaks");
        } else {
            recommendations.add("Healthy usage pattern detected");
        }

        // App-specific recommendations
        if (appInfo.packageName.contains("instagram") || appInfo.packageName.contains("tiktok")) {
            recommendations.add("Avoid infinite scrolling - set specific viewing goals");
        } else if (appInfo.packageName.contains("youtube")) {
            recommendations.add("Disable autoplay to prevent binge-watching");
        } else if (appInfo.packageName.contains("facebook")) {
            recommendations.add("Limit news feed browsing time");
        }

        return recommendations.toArray(new String[0]);
    }

    // Data class for instant assessment
    public static class InstantAddictionAssessment {
        public final String packageName;
        public final String appName;
        public final float addictionRisk;
        public final String riskLevel;
        public final String riskReason;
        public final String[] recommendations;
        public final long timestamp;

        public InstantAddictionAssessment(String packageName, String appName, float addictionRisk,
                                          String riskLevel, String riskReason, String[] recommendations,
                                          long timestamp) {
            this.packageName = packageName;
            this.appName = appName;
            this.addictionRisk = addictionRisk;
            this.riskLevel = riskLevel;
            this.riskReason = riskReason;
            this.recommendations = recommendations;
            this.timestamp = timestamp;
        }
    }

    // =========================== EXISTING METHODS (UNCHANGED) ===========================

    private void initializeAppCategories() {
        Map<String, Integer> targetApps = new HashMap<>();

        // Social Media Apps (Category 0 - High Risk)
        targetApps.put("com.instagram.android", 0);
        targetApps.put("com.facebook.katana", 0);
        targetApps.put("com.zhiliaoapp.musically", 0); // TikTok
        targetApps.put("com.snapchat.android", 0);
        targetApps.put("com.twitter.android", 0);
        targetApps.put("com.reddit.frontpage", 0);
        targetApps.put("com.pinterest", 0);

        // Video/Entertainment (Category 2 - Medium-High Risk)
        targetApps.put("com.google.android.youtube", 2);
        targetApps.put("com.netflix.mediaclient", 2);
        targetApps.put("com.amazon.avod.thirdpartyclient", 2); // Prime Video
        targetApps.put("com.hulu.plus", 2);

        // Messaging (Category 6 - Medium Risk)
        targetApps.put("com.whatsapp", 6);
        targetApps.put("com.facebook.orca", 6); // Messenger
        targetApps.put("com.discord", 6);
        targetApps.put("org.telegram.messenger", 6);

        // Games (Category 3 - High Risk)
        targetApps.put("com.king.candycrushsaga", 3);
        targetApps.put("com.supercell.clashofclans", 3);
        targetApps.put("com.roblox.client", 3);

        cacheLock.lock();
        try {
            for (Map.Entry<String, Integer> entry : targetApps.entrySet()) {
                appCategoryCache.put(entry.getKey(), entry.getValue());
            }
        } finally {
            cacheLock.unlock();
        }
    }

    // Original method (unchanged)
    public EnhancedSessionData extractFeatures(String userId, long sessionStart, long sessionEnd) {
        if (userId == null || userId.trim().isEmpty()) return null;
        if (sessionStart >= sessionEnd) return null;

        // Check if we have usage stats permission
        if (usageStatsManager == null) {
            Log.e(TAG, "UsageStatsManager is null");
            return createDummySessionData(userId, sessionStart, sessionEnd);
        }

        try {
            long sessionDuration = sessionEnd - sessionStart;
            if (sessionDuration < TimeUnit.SECONDS.toMillis(5)) return null;

            Map<String, Integer> appUsageCount = new HashMap<>();
            Map<String, Long> lastUsageTime = new HashMap<>();
            int totalUnlocks = 0;
            int totalScrolls = 0;
            int bingeFlag = 0;

            UsageEvents events = usageStatsManager.queryEvents(sessionStart, sessionEnd);

            if (events == null) {
                Log.w(TAG, "No usage events available, creating dummy data");
                return createDummySessionData(userId, sessionStart, sessionEnd);
            }

            UsageEvents.Event event = new UsageEvents.Event();
            while (events.hasNextEvent()) {
                events.getNextEvent(event);
                String packageName = event.getPackageName();
                int eventType = event.getEventType();

                appUsageCount.put(packageName, appUsageCount.getOrDefault(packageName, 0) + 1);
                lastUsageTime.put(packageName, event.getTimeStamp());

                if (eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) totalUnlocks++;

                AppUsageTracker tracker = appUsageTrackers.computeIfAbsent(packageName, k -> new AppUsageTracker());
                if (tracker.recordUsage(event.getTimeStamp())) bingeFlag = 1;
            }

            // Determine primary app
            String primaryApp = appUsageCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("unknown");

            int appCategory = appCategoryCache.get(primaryApp) != null ? appCategoryCache.get(primaryApp) : 5;
            long consecutiveTime = lastUsageTime.getOrDefault(primaryApp, 0L) - sessionStart;
            float normalizedConsecutive = Math.min(consecutiveTime / (3f * 3600f * 1000f), 1f);

            EnhancedSessionData sessionData = new EnhancedSessionData();
            sessionData.userId = userId;
            sessionData.appName = primaryApp;
            sessionData.appCategory = appCategory;
            sessionData.sessionDuration = sessionDuration;
            sessionData.unlockFrequency = totalUnlocks;
            sessionData.scrollsPerMinute = totalScrolls / (sessionDuration / 60000f);
            sessionData.consecutiveSameApp = (int) (normalizedConsecutive * 180);
            sessionData.timeOfDay = (sessionStart % TimeUnit.DAYS.toMillis(1)) / (float) TimeUnit.DAYS.toMillis(1);
            sessionData.bingeFlag = bingeFlag;
            sessionData.timestamp = sessionEnd;

            return sessionData;

        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied for usage stats", e);
            return createDummySessionData(userId, sessionStart, sessionEnd);
        } catch (Exception e) {
            Log.e(TAG, "Error extracting features", e);
            return createDummySessionData(userId, sessionStart, sessionEnd);
        }
    }

    // New helper for dummy fallback
    private EnhancedSessionData createDummySessionData(String userId, long sessionStart, long sessionEnd) {
        EnhancedSessionData sessionData = new EnhancedSessionData();
        sessionData.userId = userId;
        sessionData.appName = "demo_app";
        sessionData.appCategory = 0;
        sessionData.sessionDuration = sessionEnd - sessionStart;
        sessionData.unlockFrequency = 5;   // matches your model field
        sessionData.scrollsPerMinute = 10.0f;
        sessionData.consecutiveSameApp = 30;
        sessionData.timeOfDay = 0.5f; // noon
        sessionData.bingeFlag = 0;
        sessionData.timestamp = sessionEnd;
        return sessionData;
    }

    private void cleanupOldTrackers() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                appUsageTrackers.entrySet().removeIf(e -> (now - e.getValue().lastUsage) > TimeUnit.HOURS.toMillis(1));
            }
        }, TimeUnit.MINUTES.toMillis(1), TimeUnit.MINUTES.toMillis(5));
    }

    private static class AppUsageTracker {
        private long lastUsage = 0;
        private int consecutiveCount = 0;

        boolean recordUsage(long timestamp) {
            if (lastUsage == 0 || timestamp - lastUsage > TimeUnit.MINUTES.toMillis(10)) {
                consecutiveCount = 1;
            } else {
                consecutiveCount++;
            }
            lastUsage = timestamp;
            return consecutiveCount >= 10;
        }
    }

    private static class NotificationTracker { }
}