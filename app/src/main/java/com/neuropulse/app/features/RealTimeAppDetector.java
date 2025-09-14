// RealTimeAppDetector.java
// Location: app/src/main/java/com/neuropulse/app/features/RealTimeAppDetector.java
package com.neuropulse.app.features;

import android.app.ActivityManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class RealTimeAppDetector {
    private static final String TAG = "RealTimeAppDetector";
    private final Context context;
    private final UsageStatsManager usageStatsManager;
    private final PackageManager packageManager;
    private final Map<String, AppRiskProfile> appRiskProfiles;

    public RealTimeAppDetector(Context context) {
        this.context = context.getApplicationContext();
        this.usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        this.packageManager = context.getPackageManager();
        this.appRiskProfiles = initializeAppRiskProfiles();
    }

    // Main method to get current app and its addiction risk
    public CurrentAppInfo getCurrentAppWithRisk() {
        String currentApp = getCurrentForegroundApp();
        if (currentApp == null || currentApp.equals("unknown")) {
            return new CurrentAppInfo("unknown", "Unknown App", 0, 0.0f, "No active app detected");
        }

        AppRiskProfile riskProfile = appRiskProfiles.getOrDefault(currentApp, createDefaultRiskProfile());
        String appDisplayName = getAppDisplayName(currentApp);

        // Calculate real-time addiction risk
        float currentRisk = calculateRealTimeRisk(currentApp, riskProfile);

        return new CurrentAppInfo(
                currentApp,
                appDisplayName,
                riskProfile.category,
                currentRisk,
                generateRiskReason(currentApp, riskProfile, currentRisk)
        );
    }

    // Get the currently foreground app
    private String getCurrentForegroundApp() {
        try {
            // Method 1: Use UsageStats (most reliable)
            long currentTime = System.currentTimeMillis();
            long queryTime = currentTime - TimeUnit.MINUTES.toMillis(1); // Last 1 minute

            if (usageStatsManager == null) {
                return getFallbackCurrentApp();
            }

            UsageEvents events = usageStatsManager.queryEvents(queryTime, currentTime);
            if (events == null) return getFallbackCurrentApp();

            String lastForegroundApp = null;
            long lastEventTime = 0;

            UsageEvents.Event event = new UsageEvents.Event();
            while (events.hasNextEvent()) {
                events.getNextEvent(event);
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    if (event.getTimeStamp() > lastEventTime) {
                        lastEventTime = event.getTimeStamp();
                        lastForegroundApp = event.getPackageName();
                    }
                }
            }

            return lastForegroundApp != null ? lastForegroundApp : getFallbackCurrentApp();

        } catch (Exception e) {
            Log.e(TAG, "Error getting current app", e);
            return getFallbackCurrentApp();
        }
    }

    // Fallback method using ActivityManager (deprecated but still works)
    private String getFallbackCurrentApp() {
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager != null) {
                List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);

                if (tasks != null && !tasks.isEmpty()) {
                    return tasks.get(0).topActivity.getPackageName();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Fallback method failed", e);
        }
        return "unknown";
    }

    // Calculate real-time addiction risk based on usage patterns
    private float calculateRealTimeRisk(String packageName, AppRiskProfile profile) {
        float baseRisk = profile.baseRisk;

        try {
            // Get recent usage intensity
            long currentTime = System.currentTimeMillis();
            long recentPeriod = TimeUnit.MINUTES.toMillis(30); // Last 30 minutes

            float usageIntensity = getRecentUsageIntensity(packageName, currentTime - recentPeriod, currentTime);
            float timeOfDayRisk = getTimeOfDayRisk();
            float continuousUsageRisk = getContinuousUsageRisk(packageName);

            // Combine risk factors
            float totalRisk = baseRisk +
                    (usageIntensity * 0.3f) +
                    (timeOfDayRisk * 0.2f) +
                    (continuousUsageRisk * 0.3f);

            return Math.min(1.0f, Math.max(0.0f, totalRisk));

        } catch (Exception e) {
            Log.e(TAG, "Error calculating real-time risk", e);
            return baseRisk;
        }
    }

    private float getRecentUsageIntensity(String packageName, long startTime, long endTime) {
        try {
            if (usageStatsManager == null) return 0.2f; // Default moderate intensity

            UsageEvents events = usageStatsManager.queryEvents(startTime, endTime);
            if (events == null) return 0.2f;

            int eventCount = 0;
            UsageEvents.Event event = new UsageEvents.Event();

            while (events.hasNextEvent()) {
                events.getNextEvent(event);
                if (event.getPackageName().equals(packageName)) {
                    eventCount++;
                }
            }

            // Normalize by time period (events per minute)
            long durationMinutes = (endTime - startTime) / (1000 * 60);
            return durationMinutes > 0 ? Math.min(1.0f, eventCount / (float) durationMinutes / 10.0f) : 0.0f;

        } catch (Exception e) {
            return 0.2f; // Default fallback
        }
    }

    private float getTimeOfDayRisk() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        // Higher risk late at night or very early morning
        if (hour >= 22 || hour <= 6) return 0.3f;
        if (hour >= 18) return 0.1f;
        return 0.0f;
    }

    private float getContinuousUsageRisk(String packageName) {
        // This would need to track usage session length
        // For now, return moderate risk based on app type
        AppRiskProfile profile = appRiskProfiles.get(packageName);
        if (profile != null && profile.category == 0) { // Social media
            return 0.3f;
        }
        return 0.1f;
    }

    private String getAppDisplayName(String packageName) {
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationLabel(appInfo).toString();
        } catch (Exception e) {
            // Extract simple name from package
            if (packageName.contains(".")) {
                String[] parts = packageName.split("\\.");
                return parts[parts.length - 1];
            }
            return packageName;
        }
    }

    private String generateRiskReason(String packageName, AppRiskProfile profile, float currentRisk) {
        if (currentRisk >= 0.7f) {
            return "High addiction risk - " + profile.riskFactors[0];
        } else if (currentRisk >= 0.4f) {
            return "Moderate risk - " + profile.primaryConcern;
        } else {
            return "Low risk - healthy usage pattern";
        }
    }

    // Initialize risk profiles for different apps
    private Map<String, AppRiskProfile> initializeAppRiskProfiles() {
        Map<String, AppRiskProfile> profiles = new HashMap<>();

        // High-risk social media apps
        profiles.put("com.instagram.android", new AppRiskProfile(0, 0.8f, "Infinite scroll addiction",
                new String[]{"Infinite scroll mechanism", "Dopamine-driven engagement", "Social comparison"}));
        profiles.put("com.zhiliaoapp.musically", new AppRiskProfile(0, 0.9f, "Short-form video addiction",
                new String[]{"Algorithmic content delivery", "Endless video stream", "High dopamine triggers"}));
        profiles.put("com.facebook.katana", new AppRiskProfile(0, 0.7f, "Social validation seeking",
                new String[]{"News feed algorithm", "Social interactions", "Notification triggers"}));
        profiles.put("com.snapchat.android", new AppRiskProfile(0, 0.7f, "Streak maintenance compulsion",
                new String[]{"Streak pressure", "Instant gratification", "FOMO triggers"}));
        profiles.put("com.twitter.android", new AppRiskProfile(0, 0.6f, "Information overload",
                new String[]{"Real-time updates", "Outrage engagement", "Infinite timeline"}));
        profiles.put("com.reddit.frontpage", new AppRiskProfile(0, 0.6f, "Endless browsing",
                new String[]{"Infinite scroll", "Discussion addiction", "Time sink"}));

        // Entertainment apps
        profiles.put("com.google.android.youtube", new AppRiskProfile(2, 0.7f, "Binge-watching tendency",
                new String[]{"Autoplay feature", "Recommendation algorithm", "Endless content"}));
        profiles.put("com.netflix.mediaclient", new AppRiskProfile(2, 0.6f, "Episode binge-watching",
                new String[]{"Autoplay next episode", "Cliffhanger content", "Binge-friendly interface"}));
        profiles.put("com.amazon.avod.thirdpartyclient", new AppRiskProfile(2, 0.5f, "Video streaming",
                new String[]{"Autoplay content", "Recommendation system"}));

        // Gaming apps
        profiles.put("com.king.candycrushsaga", new AppRiskProfile(3, 0.8f, "Reward schedule manipulation",
                new String[]{"Variable reward schedules", "In-app purchases", "Progress blocking"}));
        profiles.put("com.supercell.clashofclans", new AppRiskProfile(3, 0.7f, "Time-gated progression",
                new String[]{"Wait timers", "Social pressure", "Collection mechanics"}));
        profiles.put("com.roblox.client", new AppRiskProfile(3, 0.6f, "Gaming addiction",
                new String[]{"Social gaming", "Virtual rewards", "Time investment"}));

        // Communication apps (lower risk)
        profiles.put("com.whatsapp", new AppRiskProfile(6, 0.3f, "Communication necessity",
                new String[]{"Social obligation", "Group pressure"}));
        profiles.put("com.facebook.orca", new AppRiskProfile(6, 0.4f, "Messaging addiction",
                new String[]{"Constant messaging", "Social pressure"}));
        profiles.put("com.discord", new AppRiskProfile(6, 0.4f, "Community engagement",
                new String[]{"Real-time chat", "Gaming communities"}));
        profiles.put("org.telegram.messenger", new AppRiskProfile(6, 0.2f, "Basic messaging",
                new String[]{"Essential communication"}));

        // Productivity apps (very low risk)
        profiles.put("com.google.android.apps.docs.editors.docs", new AppRiskProfile(1, 0.1f, "Document editing",
                new String[]{"Productive use"}));
        profiles.put("com.microsoft.office.word", new AppRiskProfile(1, 0.1f, "Document creation",
                new String[]{"Work-related"}));

        return profiles;
    }

    private AppRiskProfile createDefaultRiskProfile() {
        return new AppRiskProfile(5, 0.2f, "Unknown app - moderate caution", new String[]{"Unknown risk factors"});
    }

    // Data classes
    public static class CurrentAppInfo {
        public final String packageName;
        public final String displayName;
        public final int category;
        public final float addictionRisk;
        public final String riskReason;

        public CurrentAppInfo(String packageName, String displayName, int category,
                              float addictionRisk, String riskReason) {
            this.packageName = packageName;
            this.displayName = displayName;
            this.category = category;
            this.addictionRisk = addictionRisk;
            this.riskReason = riskReason;
        }

        public String getRiskLevel() {
            if (addictionRisk >= 0.7f) return "HIGH";
            else if (addictionRisk >= 0.4f) return "MEDIUM";
            else return "LOW";
        }
    }

    private static class AppRiskProfile {
        public final int category;
        public final float baseRisk;
        public final String primaryConcern;
        public final String[] riskFactors;

        public AppRiskProfile(int category, float baseRisk, String primaryConcern, String[] riskFactors) {
            this.category = category;
            this.baseRisk = baseRisk;
            this.primaryConcern = primaryConcern;
            this.riskFactors = riskFactors;
        }
    }
}