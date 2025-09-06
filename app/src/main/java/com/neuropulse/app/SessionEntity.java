package com.neuropulse.app;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sessions")
public class SessionEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String userInstallId;
    public String appPackage;
    public String appCategory;

    public long sessionStartTs;      // when app moved to foreground
    public long sessionEndTs;        // when app moved to background
    public long sessionDurationSec;  // derived duration

    public long timestamp;           // when record was saved

    // Extra fields used in SessionAdapter
    public int unlocksLastHour;      // snapshot when session ended
    public int appSwitchCount;
    public int consecutiveSameAppMinutes;

    public int notifCountLast30Min;
    public long returnAfterNotificationSec; // -1 if none

    public int nightFlag;            // 0/1
    public int bingeFlag;            // 0/1
    public int dopamineSpikeLabel;   // 0/1

    public String addictionRisk;     // e.g., "Low", "Medium", "High"
    public long duration;
}
