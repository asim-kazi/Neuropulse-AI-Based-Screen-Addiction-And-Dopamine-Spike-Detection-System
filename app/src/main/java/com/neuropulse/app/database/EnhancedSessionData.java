// EnhancedSessionData.java
package com.neuropulse.app.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Index;
import androidx.room.ColumnInfo;
import java.util.Objects;

@Entity(
        tableName = "enhanced_session_data",
        indices = {
                @Index(value = {"userId", "timestamp"}, name = "idx_user_timestamp"),
                @Index(value = {"appCategory"}, name = "idx_app_category"),
                @Index(value = {"dopamineSpikeFlag"}, name = "idx_dopamine_flag"),
                @Index(value = {"addictionFlag"}, name = "idx_addiction_flag"),
                @Index(value = {"timestamp"}, name = "idx_timestamp")
        }
)
public class EnhancedSessionData {
    @PrimaryKey(autoGenerate = true)
    public long sessionId;

    @ColumnInfo(name = "userId")
    public String userId; // Anonymized user identifier

    @ColumnInfo(name = "sessionDuration")
    public long sessionDuration; // milliseconds

    @ColumnInfo(name = "unlockCount")
    public int unlockCount;

    @ColumnInfo(name = "appName")
    public String appName;

    @ColumnInfo(name = "appCategory")
    public int appCategory; // 0-9 categories (social=0, productivity=1, etc.)

    @ColumnInfo(name = "notifCount")
    public int notifCount;

    @ColumnInfo(name = "notifResponse")
    public int notifResponse; // 0=ignored, 1=dismissed, 2=acted_upon

    @ColumnInfo(name = "appSwitchCount")
    public int appSwitchCount;

    @ColumnInfo(name = "timeOfDay")
    public float timeOfDay; // 0.0-1.0 (0=midnight, 0.5=noon, 1.0=midnight)

    @ColumnInfo(name = "consecutiveSameApp")
    public int consecutiveSameApp; // minutes in same app

    @ColumnInfo(name = "bingeFlag")
    public int bingeFlag; // 0=no, 1=yes (>2 hours continuous)

    @ColumnInfo(name = "dopamineSpikeFlag")
    public int dopamineSpikeFlag; // 0=no, 1=yes (AI predicted)

    @ColumnInfo(name = "addictionFlag")
    public int addictionFlag; // 0=healthy, 1=at_risk, 2=addicted

    @ColumnInfo(name = "scrollsPerMinute")
    public float scrollsPerMinute;

    @ColumnInfo(name = "unlockFrequency")
    public float unlockFrequency; // unlocks per hour

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    public EnhancedSessionData() {
        // Empty constructor required by Room
    }

    public EnhancedSessionData(String userId, long sessionDuration, int unlockCount,
                               String appName, int appCategory, int notifCount,
                               int notifResponse, int appSwitchCount, float timeOfDay,
                               int consecutiveSameApp, int bingeFlag,
                               int dopamineSpikeFlag, int addictionFlag,
                               float scrollsPerMinute, float unlockFrequency, long timestamp) {
        // Input validation
        this.userId = userId != null ? userId : "unknown";
        this.sessionDuration = Math.max(0, sessionDuration);
        this.unlockCount = Math.max(0, unlockCount);
        this.appName = appName != null ? appName : "unknown";
        this.appCategory = Math.max(0, Math.min(9, appCategory));
        this.notifCount = Math.max(0, notifCount);
        this.notifResponse = Math.max(0, Math.min(2, notifResponse));
        this.appSwitchCount = Math.max(0, appSwitchCount);
        this.timeOfDay = Math.max(0f, Math.min(1f, timeOfDay));
        this.consecutiveSameApp = Math.max(0, consecutiveSameApp);
        this.bingeFlag = bingeFlag == 1 ? 1 : 0;
        this.dopamineSpikeFlag = dopamineSpikeFlag == 1 ? 1 : 0;
        this.addictionFlag = Math.max(0, Math.min(2, addictionFlag));
        this.scrollsPerMinute = Math.max(0f, scrollsPerMinute);
        this.unlockFrequency = Math.max(0f, unlockFrequency);
        this.timestamp = Math.max(0, timestamp);
    }

    // Utility methods
    public boolean isHighRisk() {
        return dopamineSpikeFlag == 1 || addictionFlag >= 2;
    }

    public String getFormattedDuration() {
        long minutes = sessionDuration / 60000;
        long seconds = (sessionDuration % 60000) / 1000;
        return String.format("%dm %ds", minutes, seconds);
    }

    public String getCategoryName() {
        String[] categories = {
                "Social Media", "Productivity", "Entertainment", "Games",
                "News", "Shopping", "Communication", "Health", "Finance", "Utilities"
        };
        return appCategory < categories.length ? categories[appCategory] : "Other";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EnhancedSessionData that = (EnhancedSessionData) obj;
        return sessionId == that.sessionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }

    @Override
    public String toString() {
        return String.format("Session{id=%d, user=%s, duration=%s, app=%s, category=%s}",
                sessionId, userId, getFormattedDuration(), appName, getCategoryName());
    }
}