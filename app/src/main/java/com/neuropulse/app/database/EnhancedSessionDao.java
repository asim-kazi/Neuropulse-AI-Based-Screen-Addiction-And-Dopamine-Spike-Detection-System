// EnhancedSessionDao.java
package com.neuropulse.app.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import java.util.List;
import com.neuropulse.app.database.DatabaseHelpers.*;

@Dao
public interface EnhancedSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertEnhancedSession(EnhancedSessionData session);

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertMultipleSessions(List<EnhancedSessionData> sessions);

    @Query("SELECT * FROM enhanced_session_data WHERE userId = :userId " +
            "ORDER BY timestamp DESC LIMIT :limit")
    List<EnhancedSessionData> getUserRecentSessions(String userId, int limit);

    @Query("SELECT AVG(sessionDuration) FROM enhanced_session_data " +
            "WHERE userId = :userId AND appCategory = :category " +
            "AND timestamp > :startTime")
    Float getAvgSessionDurationByCategory(String userId, int category, long startTime);

    @Query("SELECT COUNT(*) FROM enhanced_session_data " +
            "WHERE userId = :userId AND bingeFlag = 1 " +
            "AND timestamp > :startTime")
    int getBingeSessionsCount(String userId, long startTime);

    @Query("SELECT * FROM enhanced_session_data " +
            "WHERE dopamineSpikeFlag = 1 AND timestamp > :startTime " +
            "ORDER BY timestamp DESC LIMIT :limit")
    List<EnhancedSessionData> getRecentDopamineSpikeSessions(long startTime, int limit);

    @Query("SELECT appName, COUNT(*) as usage_count, AVG(sessionDuration) as avg_duration " +
            "FROM enhanced_session_data WHERE userId = :userId " +
            "AND timestamp > :startTime " +
            "GROUP BY appName ORDER BY usage_count DESC LIMIT :limit")
    List<AppUsageStats> getTopUsedApps(String userId, long startTime, int limit);

    @Query("SELECT " +
            "CAST(timeOfDay * 24 AS INTEGER) as hour, " +
            "AVG(CAST(addictionFlag AS REAL)) as avg_addiction, " +
            "COUNT(*) as session_count " +
            "FROM enhanced_session_data " +
            "WHERE userId = :userId AND timestamp > :startTime " +
            "GROUP BY CAST(timeOfDay * 24 AS INTEGER) " +
            "ORDER BY hour")
    List<HourlyPattern> getHourlyAddictionPatterns(String userId, long startTime);

    @Transaction
    @Query("DELETE FROM enhanced_session_data WHERE timestamp < :cutoffTime")
    int deleteOldSessions(long cutoffTime);

    @Query("SELECT COUNT(*) FROM enhanced_session_data")
    int getTotalSessionCount();

    @Query("SELECT userId, sessionDuration, appCategory, dopamineSpikeFlag, addictionFlag, timestamp " +
            "FROM enhanced_session_data WHERE timestamp > :startTime " +
            "ORDER BY timestamp DESC LIMIT 10000") // Add reasonable limit
    List<SessionSummary> getSessionSummariesForExport(long startTime);

    @Query("SELECT AVG(sessionDuration) as avgSessionDuration, " +
            "COUNT(*) as totalSessions, MAX(timestamp) as lastSessionTime " +
            "FROM enhanced_session_data WHERE userId = :userId")
    UserStats getUserStats(String userId);

    // Additional useful queries
    @Query("SELECT * FROM enhanced_session_data WHERE userId = :userId " +
            "AND timestamp BETWEEN :startTime AND :endTime " +
            "ORDER BY timestamp DESC")
    List<EnhancedSessionData> getSessionsInTimeRange(String userId, long startTime, long endTime);

    @Query("SELECT COUNT(DISTINCT userId) FROM enhanced_session_data")
    int getUniqueUserCount();

    @Transaction
    @Query("DELETE FROM enhanced_session_data WHERE userId = :userId")
    int deleteUserData(String userId);
}