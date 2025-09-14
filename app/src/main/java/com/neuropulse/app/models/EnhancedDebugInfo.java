package com.neuropulse.app.models;

import com.neuropulse.app.database.EnhancedSessionData;
import com.neuropulse.app.ml.AddictionPredictor;

public class EnhancedDebugInfo {
    public EnhancedSessionData sessionData;

    public AddictionPredictor.PredictionResult prediction;
    public String[] featureLabels;
    public String[] featureValues;

    public EnhancedDebugInfo(EnhancedSessionData sessionData, AddictionPredictor.PredictionResult prediction) {
        this.sessionData = sessionData;
        this.prediction = prediction;
        prepareDisplayData();
    }

    private void prepareDisplayData() {
        featureLabels = new String[] {
                "Session Duration", "Unlock Count", "App Name", "App Category",
                "Notification Count", "Notification Response", "App Switches",
                "Time of Day", "Consecutive Same App", "Binge Flag",
                "Scrolls/Minute", "Unlock Frequency", "Dopamine Risk",
                "Addiction Level", "AI Recommendation"
        };

        featureValues = new String[] {
                formatDuration(sessionData.sessionDuration),
                String.valueOf(sessionData.unlockCount),
                sessionData.appName,
                getCategoryName(sessionData.appCategory),
                String.valueOf(sessionData.notifCount),
                getResponseType(sessionData.notifResponse),
                String.valueOf(sessionData.appSwitchCount),
                formatTimeOfDay(sessionData.timeOfDay),
                sessionData.consecutiveSameApp + " min",
                sessionData.bingeFlag == 1 ? "YES" : "NO",
                String.format("%.1f", sessionData.scrollsPerMinute),
                String.format("%.1f/hr", sessionData.unlockFrequency),
                String.format("%.2f (%s)", prediction.dopamineRisk, prediction.getRiskLevel()),
                prediction.getAddictionLevelString(),
                prediction.recommendations.length > 0 ? prediction.recommendations[0] : "None"
        };
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        if (hours > 0) return String.format("%dh %dm", hours, minutes % 60);
        else return String.format("%dm %ds", minutes, seconds % 60);
    }

    private String getCategoryName(int category) {
        String[] categories = {
                "Social Media", "Productivity", "Entertainment", "Games",
                "News", "Shopping", "Communication", "Health", "Finance", "Utilities"
        };
        return category < categories.length ? categories[category] : "Other";
    }

    private String getResponseType(int response) {
        switch (response) {
            case 0: return "Ignored";
            case 1: return "Dismissed";
            case 2: return "Acted Upon";
            default: return "Unknown";
        }
    }

    private String formatTimeOfDay(float timeOfDay) {
        int hour = (int)(timeOfDay * 24);
        int minute = (int)((timeOfDay * 24 - hour) * 60);
        return String.format("%02d:%02d", hour, minute);
    }
}
