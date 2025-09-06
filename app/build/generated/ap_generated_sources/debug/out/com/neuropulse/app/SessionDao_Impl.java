package com.neuropulse.app;

import android.database.Cursor;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SessionDao_Impl implements SessionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SessionEntity> __insertionAdapterOfSessionEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public SessionDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSessionEntity = new EntityInsertionAdapter<SessionEntity>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR ABORT INTO `sessions` (`id`,`userInstallId`,`appPackage`,`appCategory`,`sessionStartTs`,`sessionEndTs`,`sessionDurationSec`,`timestamp`,`unlocksLastHour`,`appSwitchCount`,`consecutiveSameAppMinutes`,`notifCountLast30Min`,`returnAfterNotificationSec`,`nightFlag`,`bingeFlag`,`dopamineSpikeLabel`,`addictionRisk`,`duration`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, SessionEntity value) {
        stmt.bindLong(1, value.id);
        if (value.userInstallId == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.userInstallId);
        }
        if (value.appPackage == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.appPackage);
        }
        if (value.appCategory == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.appCategory);
        }
        stmt.bindLong(5, value.sessionStartTs);
        stmt.bindLong(6, value.sessionEndTs);
        stmt.bindLong(7, value.sessionDurationSec);
        stmt.bindLong(8, value.timestamp);
        stmt.bindLong(9, value.unlocksLastHour);
        stmt.bindLong(10, value.appSwitchCount);
        stmt.bindLong(11, value.consecutiveSameAppMinutes);
        stmt.bindLong(12, value.notifCountLast30Min);
        stmt.bindLong(13, value.returnAfterNotificationSec);
        stmt.bindLong(14, value.nightFlag);
        stmt.bindLong(15, value.bingeFlag);
        stmt.bindLong(16, value.dopamineSpikeLabel);
        if (value.addictionRisk == null) {
          stmt.bindNull(17);
        } else {
          stmt.bindString(17, value.addictionRisk);
        }
        stmt.bindLong(18, value.duration);
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM sessions";
        return _query;
      }
    };
  }

  @Override
  public void insert(final SessionEntity session) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfSessionEntity.insert(session);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void clearAll() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfClearAll.release(_stmt);
    }
  }

  @Override
  public List<SessionEntity> getAllSessions() {
    final String _sql = "SELECT * FROM sessions ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfUserInstallId = CursorUtil.getColumnIndexOrThrow(_cursor, "userInstallId");
      final int _cursorIndexOfAppPackage = CursorUtil.getColumnIndexOrThrow(_cursor, "appPackage");
      final int _cursorIndexOfAppCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "appCategory");
      final int _cursorIndexOfSessionStartTs = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionStartTs");
      final int _cursorIndexOfSessionEndTs = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionEndTs");
      final int _cursorIndexOfSessionDurationSec = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionDurationSec");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfUnlocksLastHour = CursorUtil.getColumnIndexOrThrow(_cursor, "unlocksLastHour");
      final int _cursorIndexOfAppSwitchCount = CursorUtil.getColumnIndexOrThrow(_cursor, "appSwitchCount");
      final int _cursorIndexOfConsecutiveSameAppMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "consecutiveSameAppMinutes");
      final int _cursorIndexOfNotifCountLast30Min = CursorUtil.getColumnIndexOrThrow(_cursor, "notifCountLast30Min");
      final int _cursorIndexOfReturnAfterNotificationSec = CursorUtil.getColumnIndexOrThrow(_cursor, "returnAfterNotificationSec");
      final int _cursorIndexOfNightFlag = CursorUtil.getColumnIndexOrThrow(_cursor, "nightFlag");
      final int _cursorIndexOfBingeFlag = CursorUtil.getColumnIndexOrThrow(_cursor, "bingeFlag");
      final int _cursorIndexOfDopamineSpikeLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "dopamineSpikeLabel");
      final int _cursorIndexOfAddictionRisk = CursorUtil.getColumnIndexOrThrow(_cursor, "addictionRisk");
      final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
      final List<SessionEntity> _result = new ArrayList<SessionEntity>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final SessionEntity _item;
        _item = new SessionEntity();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        if (_cursor.isNull(_cursorIndexOfUserInstallId)) {
          _item.userInstallId = null;
        } else {
          _item.userInstallId = _cursor.getString(_cursorIndexOfUserInstallId);
        }
        if (_cursor.isNull(_cursorIndexOfAppPackage)) {
          _item.appPackage = null;
        } else {
          _item.appPackage = _cursor.getString(_cursorIndexOfAppPackage);
        }
        if (_cursor.isNull(_cursorIndexOfAppCategory)) {
          _item.appCategory = null;
        } else {
          _item.appCategory = _cursor.getString(_cursorIndexOfAppCategory);
        }
        _item.sessionStartTs = _cursor.getLong(_cursorIndexOfSessionStartTs);
        _item.sessionEndTs = _cursor.getLong(_cursorIndexOfSessionEndTs);
        _item.sessionDurationSec = _cursor.getLong(_cursorIndexOfSessionDurationSec);
        _item.timestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _item.unlocksLastHour = _cursor.getInt(_cursorIndexOfUnlocksLastHour);
        _item.appSwitchCount = _cursor.getInt(_cursorIndexOfAppSwitchCount);
        _item.consecutiveSameAppMinutes = _cursor.getInt(_cursorIndexOfConsecutiveSameAppMinutes);
        _item.notifCountLast30Min = _cursor.getInt(_cursorIndexOfNotifCountLast30Min);
        _item.returnAfterNotificationSec = _cursor.getLong(_cursorIndexOfReturnAfterNotificationSec);
        _item.nightFlag = _cursor.getInt(_cursorIndexOfNightFlag);
        _item.bingeFlag = _cursor.getInt(_cursorIndexOfBingeFlag);
        _item.dopamineSpikeLabel = _cursor.getInt(_cursorIndexOfDopamineSpikeLabel);
        if (_cursor.isNull(_cursorIndexOfAddictionRisk)) {
          _item.addictionRisk = null;
        } else {
          _item.addictionRisk = _cursor.getString(_cursorIndexOfAddictionRisk);
        }
        _item.duration = _cursor.getLong(_cursorIndexOfDuration);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<SessionEntity> getRecentSessions() {
    final String _sql = "SELECT * FROM sessions ORDER BY sessionEndTs DESC LIMIT 20";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfUserInstallId = CursorUtil.getColumnIndexOrThrow(_cursor, "userInstallId");
      final int _cursorIndexOfAppPackage = CursorUtil.getColumnIndexOrThrow(_cursor, "appPackage");
      final int _cursorIndexOfAppCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "appCategory");
      final int _cursorIndexOfSessionStartTs = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionStartTs");
      final int _cursorIndexOfSessionEndTs = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionEndTs");
      final int _cursorIndexOfSessionDurationSec = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionDurationSec");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfUnlocksLastHour = CursorUtil.getColumnIndexOrThrow(_cursor, "unlocksLastHour");
      final int _cursorIndexOfAppSwitchCount = CursorUtil.getColumnIndexOrThrow(_cursor, "appSwitchCount");
      final int _cursorIndexOfConsecutiveSameAppMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "consecutiveSameAppMinutes");
      final int _cursorIndexOfNotifCountLast30Min = CursorUtil.getColumnIndexOrThrow(_cursor, "notifCountLast30Min");
      final int _cursorIndexOfReturnAfterNotificationSec = CursorUtil.getColumnIndexOrThrow(_cursor, "returnAfterNotificationSec");
      final int _cursorIndexOfNightFlag = CursorUtil.getColumnIndexOrThrow(_cursor, "nightFlag");
      final int _cursorIndexOfBingeFlag = CursorUtil.getColumnIndexOrThrow(_cursor, "bingeFlag");
      final int _cursorIndexOfDopamineSpikeLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "dopamineSpikeLabel");
      final int _cursorIndexOfAddictionRisk = CursorUtil.getColumnIndexOrThrow(_cursor, "addictionRisk");
      final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
      final List<SessionEntity> _result = new ArrayList<SessionEntity>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final SessionEntity _item;
        _item = new SessionEntity();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        if (_cursor.isNull(_cursorIndexOfUserInstallId)) {
          _item.userInstallId = null;
        } else {
          _item.userInstallId = _cursor.getString(_cursorIndexOfUserInstallId);
        }
        if (_cursor.isNull(_cursorIndexOfAppPackage)) {
          _item.appPackage = null;
        } else {
          _item.appPackage = _cursor.getString(_cursorIndexOfAppPackage);
        }
        if (_cursor.isNull(_cursorIndexOfAppCategory)) {
          _item.appCategory = null;
        } else {
          _item.appCategory = _cursor.getString(_cursorIndexOfAppCategory);
        }
        _item.sessionStartTs = _cursor.getLong(_cursorIndexOfSessionStartTs);
        _item.sessionEndTs = _cursor.getLong(_cursorIndexOfSessionEndTs);
        _item.sessionDurationSec = _cursor.getLong(_cursorIndexOfSessionDurationSec);
        _item.timestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _item.unlocksLastHour = _cursor.getInt(_cursorIndexOfUnlocksLastHour);
        _item.appSwitchCount = _cursor.getInt(_cursorIndexOfAppSwitchCount);
        _item.consecutiveSameAppMinutes = _cursor.getInt(_cursorIndexOfConsecutiveSameAppMinutes);
        _item.notifCountLast30Min = _cursor.getInt(_cursorIndexOfNotifCountLast30Min);
        _item.returnAfterNotificationSec = _cursor.getLong(_cursorIndexOfReturnAfterNotificationSec);
        _item.nightFlag = _cursor.getInt(_cursorIndexOfNightFlag);
        _item.bingeFlag = _cursor.getInt(_cursorIndexOfBingeFlag);
        _item.dopamineSpikeLabel = _cursor.getInt(_cursorIndexOfDopamineSpikeLabel);
        if (_cursor.isNull(_cursorIndexOfAddictionRisk)) {
          _item.addictionRisk = null;
        } else {
          _item.addictionRisk = _cursor.getString(_cursorIndexOfAddictionRisk);
        }
        _item.duration = _cursor.getLong(_cursorIndexOfDuration);
        _result.add(_item);
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
