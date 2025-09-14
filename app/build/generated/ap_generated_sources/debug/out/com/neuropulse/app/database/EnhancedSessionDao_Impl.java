package com.neuropulse.app.database;

import android.database.Cursor;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Float;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class EnhancedSessionDao_Impl implements EnhancedSessionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<EnhancedSessionData> __insertionAdapterOfEnhancedSessionData;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOldSessions;

  private final SharedSQLiteStatement __preparedStmtOfDeleteUserData;

  public EnhancedSessionDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfEnhancedSessionData = new EntityInsertionAdapter<EnhancedSessionData>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `enhanced_session_data` (`sessionId`,`userId`,`sessionDuration`,`unlockCount`,`appName`,`appCategory`,`notifCount`,`notifResponse`,`appSwitchCount`,`timeOfDay`,`consecutiveSameApp`,`bingeFlag`,`dopamineSpikeFlag`,`addictionFlag`,`scrollsPerMinute`,`unlockFrequency`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, EnhancedSessionData value) {
        stmt.bindLong(1, value.sessionId);
        if (value.userId == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.userId);
        }
        stmt.bindLong(3, value.sessionDuration);
        stmt.bindLong(4, value.unlockCount);
        if (value.appName == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.appName);
        }
        stmt.bindLong(6, value.appCategory);
        stmt.bindLong(7, value.notifCount);
        stmt.bindLong(8, value.notifResponse);
        stmt.bindLong(9, value.appSwitchCount);
        stmt.bindDouble(10, value.timeOfDay);
        stmt.bindLong(11, value.consecutiveSameApp);
        stmt.bindLong(12, value.bingeFlag);
        stmt.bindLong(13, value.dopamineSpikeFlag);
        stmt.bindLong(14, value.addictionFlag);
        stmt.bindDouble(15, value.scrollsPerMinute);
        stmt.bindDouble(16, value.unlockFrequency);
        stmt.bindLong(17, value.timestamp);
      }
    };
    this.__preparedStmtOfDeleteOldSessions = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM enhanced_session_data WHERE timestamp < ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteUserData = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM enhanced_session_data WHERE userId = ?";
        return _query;
      }
    };
  }

  @Override
  public long insertEnhancedSession(final EnhancedSessionData session) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      long _result = __insertionAdapterOfEnhancedSessionData.insertAndReturnId(session);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<Long> insertMultipleSessions(final List<EnhancedSessionData> sessions) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      List<Long> _result = __insertionAdapterOfEnhancedSessionData.insertAndReturnIdsList(sessions);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int deleteOldSessions(final long cutoffTime) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOldSessions.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, cutoffTime);
    __db.beginTransaction();
    try {
      final int _result = _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
      __preparedStmtOfDeleteOldSessions.release(_stmt);
    }
  }

  @Override
  public int deleteUserData(final String userId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteUserData.acquire();
    int _argIndex = 1;
    if (userId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, userId);
    }
    __db.beginTransaction();
    try {
      final int _result = _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
      __preparedStmtOfDeleteUserData.release(_stmt);
    }
  }

  @Override
  public List<EnhancedSessionData> getUserRecentSessions(final String userId, final int limit) {
    final String _sql = "SELECT * FROM enhanced_session_data WHERE userId = ? ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (userId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userId);
    }
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
      final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
      final int _cursorIndexOfSessionDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionDuration");
      final int _cursorIndexOfUnlockCount = CursorUtil.getColumnIndexOrThrow(_cursor, "unlockCount");
      final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
      final int _cursorIndexOfAppCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "appCategory");
      final int _cursorIndexOfNotifCount = CursorUtil.getColumnIndexOrThrow(_cursor, "notifCount");
      final int _cursorIndexOfNotifResponse = CursorUtil.getColumnIndexOrThrow(_cursor, "notifResponse");
      final int _cursorIndexOfAppSwitchCount = CursorUtil.getColumnIndexOrThrow(_cursor, "appSwitchCount");
      final int _cursorIndexOfTimeOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "timeOfDay");
      final int _cursorIndexOfConsecutiveSameApp = CursorUtil.getColumnIndexOrThrow(_cursor, "consecutiveSameApp");
      final int _cursorIndexOfBingeFlag = CursorUtil.getColumnIndexOrThrow(_cursor, "bingeFlag");
      final int _cursorIndexOfDopamineSpikeFlag = CursorUtil.getColumnIndexOrThrow(_cursor, "dopamineSpikeFlag");
      final int _cursorIndexOfAddictionFlag = CursorUtil.getColumnIndexOrThrow(_cursor, "addictionFlag");
      final int _cursorIndexOfScrollsPerMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "scrollsPerMinute");
      final int _cursorIndexOfUnlockFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "unlockFrequency");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final List<EnhancedSessionData> _result = new ArrayList<EnhancedSessionData>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final EnhancedSessionData _item;
        _item = new EnhancedSessionData();
        _item.sessionId = _cursor.getLong(_cursorIndexOfSessionId);
        if (_cursor.isNull(_cursorIndexOfUserId)) {
          _item.userId = null;
        } else {
          _item.userId = _cursor.getString(_cursorIndexOfUserId);
        }
        _item.sessionDuration = _cursor.getLong(_cursorIndexOfSessionDuration);
        _item.unlockCount = _cursor.getInt(_cursorIndexOfUnlockCount);
        if (_cursor.isNull(_cursorIndexOfAppName)) {
          _item.appName = null;
        } else {
          _item.appName = _cursor.getString(_cursorIndexOfAppName);
        }
        _item.appCategory = _cursor.getInt(_cursorIndexOfAppCategory);
        _item.notifCount = _cursor.getInt(_cursorIndexOfNotifCount);
        _item.notifResponse = _cursor.getInt(_cursorIndexOfNotifResponse);
        _item.appSwitchCount = _cursor.getInt(_cursorIndexOfAppSwitchCount);
        _item.timeOfDay = _cursor.getFloat(_cursorIndexOfTimeOfDay);
        _item.consecutiveSameApp = _cursor.getInt(_cursorIndexOfConsecutiveSameApp);
        _item.bingeFlag = _cursor.getInt(_cursorIndexOfBingeFlag);
        _item.dopamineSpikeFlag = _cursor.getInt(_cursorIndexOfDopamineSpikeFlag);
        _item.addictionFlag = _cursor.getInt(_cursorIndexOfAddictionFlag);
        _item.scrollsPerMinute = _cursor.getFloat(_cursorIndexOfScrollsPerMinute);
        _item.unlockFrequency = _cursor.getFloat(_cursorIndexOfUnlockFrequency);
        _item.timestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public Float getAvgSessionDurationByCategory(final String userId, final int category,
      final long startTime) {
    final String _sql = "SELECT AVG(sessionDuration) FROM enhanced_session_data WHERE userId = ? AND appCategory = ? AND timestamp > ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    if (userId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userId);
    }
    _argIndex = 2;
    _statement.bindLong(_argIndex, category);
    _argIndex = 3;
    _statement.bindLong(_argIndex, startTime);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final Float _result;
      if(_cursor.moveToFirst()) {
        final Float _tmp;
        if (_cursor.isNull(0)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getFloat(0);
        }
        _result = _tmp;
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int getBingeSessionsCount(final String userId, final long startTime) {
    final String _sql = "SELECT COUNT(*) FROM enhanced_session_data WHERE userId = ? AND bingeFlag = 1 AND timestamp > ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (userId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userId);
    }
    _argIndex = 2;
    _statement.bindLong(_argIndex, startTime);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if(_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<EnhancedSessionData> getRecentDopamineSpikeSessions(final long startTime,
      final int limit) {
    final String _sql = "SELECT * FROM enhanced_session_data WHERE dopamineSpikeFlag = 1 AND timestamp > ? ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
      final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
      final int _cursorIndexOfSessionDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionDuration");
      final int _cursorIndexOfUnlockCount = CursorUtil.getColumnIndexOrThrow(_cursor, "unlockCount");
      final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
      final int _cursorIndexOfAppCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "appCategory");
      final int _cursorIndexOfNotifCount = CursorUtil.getColumnIndexOrThrow(_cursor, "notifCount");
      final int _cursorIndexOfNotifResponse = CursorUtil.getColumnIndexOrThrow(_cursor, "notifResponse");
      final int _cursorIndexOfAppSwitchCount = CursorUtil.getColumnIndexOrThrow(_cursor, "appSwitchCount");
      final int _cursorIndexOfTimeOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "timeOfDay");
      final int _cursorIndexOfConsecutiveSameApp = CursorUtil.getColumnIndexOrThrow(_cursor, "consecutiveSameApp");
      final int _cursorIndexOfBingeFlag = CursorUtil.getColumnIndexOrThrow(_cursor, "bingeFlag");
      final int _cursorIndexOfDopamineSpikeFlag = CursorUtil.getColumnIndexOrThrow(_cursor, "dopamineSpikeFlag");
      final int _cursorIndexOfAddictionFlag = CursorUtil.getColumnIndexOrThrow(_cursor, "addictionFlag");
      final int _cursorIndexOfScrollsPerMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "scrollsPerMinute");
      final int _cursorIndexOfUnlockFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "unlockFrequency");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final List<EnhancedSessionData> _result = new ArrayList<EnhancedSessionData>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final EnhancedSessionData _item;
        _item = new EnhancedSessionData();
        _item.sessionId = _cursor.getLong(_cursorIndexOfSessionId);
        if (_cursor.isNull(_cursorIndexOfUserId)) {
          _item.userId = null;
        } else {
          _item.userId = _cursor.getString(_cursorIndexOfUserId);
        }
        _item.sessionDuration = _cursor.getLong(_cursorIndexOfSessionDuration);
        _item.unlockCount = _cursor.getInt(_cursorIndexOfUnlockCount);
        if (_cursor.isNull(_cursorIndexOfAppName)) {
          _item.appName = null;
        } else {
          _item.appName = _cursor.getString(_cursorIndexOfAppName);
        }
        _item.appCategory = _cursor.getInt(_cursorIndexOfAppCategory);
        _item.notifCount = _cursor.getInt(_cursorIndexOfNotifCount);
        _item.notifResponse = _cursor.getInt(_cursorIndexOfNotifResponse);
        _item.appSwitchCount = _cursor.getInt(_cursorIndexOfAppSwitchCount);
        _item.timeOfDay = _cursor.getFloat(_cursorIndexOfTimeOfDay);
        _item.consecutiveSameApp = _cursor.getInt(_cursorIndexOfConsecutiveSameApp);
        _item.bingeFlag = _cursor.getInt(_cursorIndexOfBingeFlag);
        _item.dopamineSpikeFlag = _cursor.getInt(_cursorIndexOfDopamineSpikeFlag);
        _item.addictionFlag = _cursor.getInt(_cursorIndexOfAddictionFlag);
        _item.scrollsPerMinute = _cursor.getFloat(_cursorIndexOfScrollsPerMinute);
        _item.unlockFrequency = _cursor.getFloat(_cursorIndexOfUnlockFrequency);
        _item.timestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<DatabaseHelpers.AppUsageStats> getTopUsedApps(final String userId,
      final long startTime, final int limit) {
    final String _sql = "SELECT appName, COUNT(*) as usage_count, AVG(sessionDuration) as avg_duration FROM enhanced_session_data WHERE userId = ? AND timestamp > ? GROUP BY appName ORDER BY usage_count DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    if (userId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userId);
    }
    _argIndex = 2;
    _statement.bindLong(_argIndex, startTime);
    _argIndex = 3;
    _statement.bindLong(_argIndex, limit);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfAppName = 0;
      final int _cursorIndexOfUsageCount = 1;
      final int _cursorIndexOfAvgDuration = 2;
      final List<DatabaseHelpers.AppUsageStats> _result = new ArrayList<DatabaseHelpers.AppUsageStats>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final DatabaseHelpers.AppUsageStats _item;
        final String _tmpAppName;
        if (_cursor.isNull(_cursorIndexOfAppName)) {
          _tmpAppName = null;
        } else {
          _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
        }
        final int _tmpUsage_count;
        _tmpUsage_count = _cursor.getInt(_cursorIndexOfUsageCount);
        final double _tmpAvg_duration;
        _tmpAvg_duration = _cursor.getDouble(_cursorIndexOfAvgDuration);
        _item = new DatabaseHelpers.AppUsageStats(_tmpAppName,_tmpUsage_count,_tmpAvg_duration);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<DatabaseHelpers.HourlyPattern> getHourlyAddictionPatterns(final String userId,
      final long startTime) {
    final String _sql = "SELECT CAST(timeOfDay * 24 AS INTEGER) as hour, AVG(CAST(addictionFlag AS REAL)) as avg_addiction, COUNT(*) as session_count FROM enhanced_session_data WHERE userId = ? AND timestamp > ? GROUP BY CAST(timeOfDay * 24 AS INTEGER) ORDER BY hour";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (userId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userId);
    }
    _argIndex = 2;
    _statement.bindLong(_argIndex, startTime);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfHour = 0;
      final int _cursorIndexOfAvgAddiction = 1;
      final int _cursorIndexOfSessionCount = 2;
      final List<DatabaseHelpers.HourlyPattern> _result = new ArrayList<DatabaseHelpers.HourlyPattern>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final DatabaseHelpers.HourlyPattern _item;
        final int _tmpHour;
        _tmpHour = _cursor.getInt(_cursorIndexOfHour);
        final double _tmpAvg_addiction;
        _tmpAvg_addiction = _cursor.getDouble(_cursorIndexOfAvgAddiction);
        final int _tmpSession_count;
        _tmpSession_count = _cursor.getInt(_cursorIndexOfSessionCount);
        _item = new DatabaseHelpers.HourlyPattern(_tmpHour,_tmpAvg_addiction,_tmpSession_count);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int getTotalSessionCount() {
    final String _sql = "SELECT COUNT(*) FROM enhanced_session_data";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if(_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<DatabaseHelpers.SessionSummary> getSessionSummariesForExport(final long startTime) {
    final String _sql = "SELECT userId, sessionDuration, appCategory, dopamineSpikeFlag, addictionFlag, timestamp FROM enhanced_session_data WHERE timestamp > ? ORDER BY timestamp DESC LIMIT 10000";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfUserId = 0;
      final int _cursorIndexOfSessionDuration = 1;
      final int _cursorIndexOfAppCategory = 2;
      final int _cursorIndexOfDopamineSpikeFlag = 3;
      final int _cursorIndexOfAddictionFlag = 4;
      final int _cursorIndexOfTimestamp = 5;
      final List<DatabaseHelpers.SessionSummary> _result = new ArrayList<DatabaseHelpers.SessionSummary>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final DatabaseHelpers.SessionSummary _item;
        final String _tmpUserId;
        if (_cursor.isNull(_cursorIndexOfUserId)) {
          _tmpUserId = null;
        } else {
          _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
        }
        final long _tmpSessionDuration;
        _tmpSessionDuration = _cursor.getLong(_cursorIndexOfSessionDuration);
        final int _tmpAppCategory;
        _tmpAppCategory = _cursor.getInt(_cursorIndexOfAppCategory);
        final int _tmpDopamineSpikeFlag;
        _tmpDopamineSpikeFlag = _cursor.getInt(_cursorIndexOfDopamineSpikeFlag);
        final int _tmpAddictionFlag;
        _tmpAddictionFlag = _cursor.getInt(_cursorIndexOfAddictionFlag);
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _item = new DatabaseHelpers.SessionSummary(_tmpUserId,_tmpSessionDuration,_tmpAppCategory,_tmpDopamineSpikeFlag,_tmpAddictionFlag,_tmpTimestamp);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public DatabaseHelpers.UserStats getUserStats(final String userId) {
    final String _sql = "SELECT AVG(sessionDuration) as avgSessionDuration, COUNT(*) as totalSessions, MAX(timestamp) as lastSessionTime FROM enhanced_session_data WHERE userId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (userId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userId);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfAvgSessionDuration = 0;
      final int _cursorIndexOfTotalSessions = 1;
      final int _cursorIndexOfLastSessionTime = 2;
      final DatabaseHelpers.UserStats _result;
      if(_cursor.moveToFirst()) {
        final double _tmpAvgSessionDuration;
        _tmpAvgSessionDuration = _cursor.getDouble(_cursorIndexOfAvgSessionDuration);
        final int _tmpTotalSessions;
        _tmpTotalSessions = _cursor.getInt(_cursorIndexOfTotalSessions);
        final long _tmpLastSessionTime;
        _tmpLastSessionTime = _cursor.getLong(_cursorIndexOfLastSessionTime);
        _result = new DatabaseHelpers.UserStats(_tmpAvgSessionDuration,_tmpTotalSessions,_tmpLastSessionTime);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<EnhancedSessionData> getSessionsInTimeRange(final String userId, final long startTime,
      final long endTime) {
    final String _sql = "SELECT * FROM enhanced_session_data WHERE userId = ? AND timestamp BETWEEN ? AND ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    if (userId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userId);
    }
    _argIndex = 2;
    _statement.bindLong(_argIndex, startTime);
    _argIndex = 3;
    _statement.bindLong(_argIndex, endTime);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
      final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
      final int _cursorIndexOfSessionDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionDuration");
      final int _cursorIndexOfUnlockCount = CursorUtil.getColumnIndexOrThrow(_cursor, "unlockCount");
      final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
      final int _cursorIndexOfAppCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "appCategory");
      final int _cursorIndexOfNotifCount = CursorUtil.getColumnIndexOrThrow(_cursor, "notifCount");
      final int _cursorIndexOfNotifResponse = CursorUtil.getColumnIndexOrThrow(_cursor, "notifResponse");
      final int _cursorIndexOfAppSwitchCount = CursorUtil.getColumnIndexOrThrow(_cursor, "appSwitchCount");
      final int _cursorIndexOfTimeOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "timeOfDay");
      final int _cursorIndexOfConsecutiveSameApp = CursorUtil.getColumnIndexOrThrow(_cursor, "consecutiveSameApp");
      final int _cursorIndexOfBingeFlag = CursorUtil.getColumnIndexOrThrow(_cursor, "bingeFlag");
      final int _cursorIndexOfDopamineSpikeFlag = CursorUtil.getColumnIndexOrThrow(_cursor, "dopamineSpikeFlag");
      final int _cursorIndexOfAddictionFlag = CursorUtil.getColumnIndexOrThrow(_cursor, "addictionFlag");
      final int _cursorIndexOfScrollsPerMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "scrollsPerMinute");
      final int _cursorIndexOfUnlockFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "unlockFrequency");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final List<EnhancedSessionData> _result = new ArrayList<EnhancedSessionData>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final EnhancedSessionData _item;
        _item = new EnhancedSessionData();
        _item.sessionId = _cursor.getLong(_cursorIndexOfSessionId);
        if (_cursor.isNull(_cursorIndexOfUserId)) {
          _item.userId = null;
        } else {
          _item.userId = _cursor.getString(_cursorIndexOfUserId);
        }
        _item.sessionDuration = _cursor.getLong(_cursorIndexOfSessionDuration);
        _item.unlockCount = _cursor.getInt(_cursorIndexOfUnlockCount);
        if (_cursor.isNull(_cursorIndexOfAppName)) {
          _item.appName = null;
        } else {
          _item.appName = _cursor.getString(_cursorIndexOfAppName);
        }
        _item.appCategory = _cursor.getInt(_cursorIndexOfAppCategory);
        _item.notifCount = _cursor.getInt(_cursorIndexOfNotifCount);
        _item.notifResponse = _cursor.getInt(_cursorIndexOfNotifResponse);
        _item.appSwitchCount = _cursor.getInt(_cursorIndexOfAppSwitchCount);
        _item.timeOfDay = _cursor.getFloat(_cursorIndexOfTimeOfDay);
        _item.consecutiveSameApp = _cursor.getInt(_cursorIndexOfConsecutiveSameApp);
        _item.bingeFlag = _cursor.getInt(_cursorIndexOfBingeFlag);
        _item.dopamineSpikeFlag = _cursor.getInt(_cursorIndexOfDopamineSpikeFlag);
        _item.addictionFlag = _cursor.getInt(_cursorIndexOfAddictionFlag);
        _item.scrollsPerMinute = _cursor.getFloat(_cursorIndexOfScrollsPerMinute);
        _item.unlockFrequency = _cursor.getFloat(_cursorIndexOfUnlockFrequency);
        _item.timestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int getUniqueUserCount() {
    final String _sql = "SELECT COUNT(DISTINCT userId) FROM enhanced_session_data";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if(_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
