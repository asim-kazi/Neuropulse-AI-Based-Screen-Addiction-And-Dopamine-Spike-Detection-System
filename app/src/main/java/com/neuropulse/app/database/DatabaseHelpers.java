// DatabaseHelpers.java
package com.neuropulse.app.database;

import java.util.Objects;

public class DatabaseHelpers {

    public static class AppUsageStats {
        public final String appName;
        public final int usage_count;
        public final double avg_duration;

        public AppUsageStats(String appName, int usage_count, double avg_duration) {
            this.appName = appName != null ? appName : "unknown";
            this.usage_count = Math.max(0, usage_count);
            this.avg_duration = Math.max(0.0, avg_duration);
        }

        @Override
        public String toString() {
            return String.format("App: %s, Usage: %d, Avg Duration: %.1fs",
                    appName, usage_count, avg_duration / 1000.0);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            AppUsageStats that = (AppUsageStats) obj;
            return usage_count == that.usage_count &&
                    Double.compare(that.avg_duration, avg_duration) == 0 &&
                    Objects.equals(appName, that.appName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(appName, usage_count, avg_duration);
        }
    }

    public static class HourlyPattern {
        public final int hour;
        public final double avg_addiction;
        public final int session_count;

        public HourlyPattern(int hour, double avg_addiction, int session_count) {
            this.hour = Math.max(0, Math.min(23, hour));
            this.avg_addiction = Math.max(0.0, Math.min(2.0, avg_addiction));
            this.session_count = Math.max(0, session_count);
        }

        @Override
        public String toString() {
            return String.format("Hour: %d, Avg Addiction: %.2f, Sessions: %d",
                    hour, avg_addiction, session_count);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            HourlyPattern that = (HourlyPattern) obj;
            return hour == that.hour &&
                    Double.compare(that.avg_addiction, avg_addiction) == 0 &&
                    session_count == that.session_count;
        }

        @Override
        public int hashCode() {
            return Objects.hash(hour, avg_addiction, session_count);
        }
    }

    public static class SessionSummary {
        public final String userId;
        public final long sessionDuration;
        public final int appCategory;
        public final int dopamineSpikeFlag;
        public final int addictionFlag;
        public final long timestamp;

        public SessionSummary(String userId, long sessionDuration, int appCategory,
                              int dopamineSpikeFlag, int addictionFlag, long timestamp) {
            this.userId = userId != null ? userId : "unknown";
            this.sessionDuration = Math.max(0, sessionDuration);
            this.appCategory = Math.max(0, Math.min(9, appCategory));
            this.dopamineSpikeFlag = dopamineSpikeFlag == 1 ? 1 : 0;
            this.addictionFlag = Math.max(0, Math.min(2, addictionFlag));
            this.timestamp = Math.max(0, timestamp);
        }

        public boolean isHighRisk() {
            return dopamineSpikeFlag == 1 || addictionFlag >= 2;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            SessionSummary that = (SessionSummary) obj;
            return sessionDuration == that.sessionDuration &&
                    appCategory == that.appCategory &&
                    dopamineSpikeFlag == that.dopamineSpikeFlag &&
                    addictionFlag == that.addictionFlag &&
                    timestamp == that.timestamp &&
                    Objects.equals(userId, that.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, sessionDuration, appCategory, dopamineSpikeFlag, addictionFlag, timestamp);
        }
    }

    public static class UserStats {
        public final double avgSessionDuration;
        public final int totalSessions;
        public final long lastSessionTime;

        public UserStats(double avgSessionDuration, int totalSessions, long lastSessionTime) {
            this.avgSessionDuration = Math.max(0.0, avgSessionDuration);
            this.totalSessions = Math.max(0, totalSessions);
            this.lastSessionTime = Math.max(0, lastSessionTime);
        }

        public String getFormattedAvgDuration() {
            if (avgSessionDuration <= 0) return "0m 0s";

            long minutes = (long) (avgSessionDuration / 60000);
            long seconds = (long) ((avgSessionDuration % 60000) / 1000);
            return String.format("%dm %ds", minutes, seconds);
        }

        @Override
        public String toString() {
            return String.format("Total Sessions: %d, Avg Duration: %s, Last: %d",
                    totalSessions, getFormattedAvgDuration(), lastSessionTime);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            UserStats userStats = (UserStats) obj;
            return Double.compare(userStats.avgSessionDuration, avgSessionDuration) == 0 &&
                    totalSessions == userStats.totalSessions &&
                    lastSessionTime == userStats.lastSessionTime;
        }

        @Override
        public int hashCode() {
            return Objects.hash(avgSessionDuration, totalSessions, lastSessionTime);
        }
    }
}