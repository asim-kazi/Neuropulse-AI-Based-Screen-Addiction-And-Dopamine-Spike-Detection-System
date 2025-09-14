package com.neuropulse.app.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomOpenHelper;
import androidx.room.RoomOpenHelper.Delegate;
import androidx.room.RoomOpenHelper.ValidationResult;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.room.util.TableInfo.Column;
import androidx.room.util.TableInfo.ForeignKey;
import androidx.room.util.TableInfo.Index;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Callback;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile EnhancedSessionDao _enhancedSessionDao;

  @Override
  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `enhanced_session_data` (`sessionId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` TEXT, `sessionDuration` INTEGER NOT NULL, `unlockCount` INTEGER NOT NULL, `appName` TEXT, `appCategory` INTEGER NOT NULL, `notifCount` INTEGER NOT NULL, `notifResponse` INTEGER NOT NULL, `appSwitchCount` INTEGER NOT NULL, `timeOfDay` REAL NOT NULL, `consecutiveSameApp` INTEGER NOT NULL, `bingeFlag` INTEGER NOT NULL, `dopamineSpikeFlag` INTEGER NOT NULL, `addictionFlag` INTEGER NOT NULL, `scrollsPerMinute` REAL NOT NULL, `unlockFrequency` REAL NOT NULL, `timestamp` INTEGER NOT NULL)");
        _db.execSQL("CREATE INDEX IF NOT EXISTS `idx_user_timestamp` ON `enhanced_session_data` (`userId`, `timestamp`)");
        _db.execSQL("CREATE INDEX IF NOT EXISTS `idx_app_category` ON `enhanced_session_data` (`appCategory`)");
        _db.execSQL("CREATE INDEX IF NOT EXISTS `idx_dopamine_flag` ON `enhanced_session_data` (`dopamineSpikeFlag`)");
        _db.execSQL("CREATE INDEX IF NOT EXISTS `idx_addiction_flag` ON `enhanced_session_data` (`addictionFlag`)");
        _db.execSQL("CREATE INDEX IF NOT EXISTS `idx_timestamp` ON `enhanced_session_data` (`timestamp`)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'a1e8d0882fb794cd51eafd3687fe4389')");
      }

      @Override
      public void dropAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("DROP TABLE IF EXISTS `enhanced_session_data`");
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onDestructiveMigration(_db);
          }
        }
      }

      @Override
      public void onCreate(SupportSQLiteDatabase _db) {
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onCreate(_db);
          }
        }
      }

      @Override
      public void onOpen(SupportSQLiteDatabase _db) {
        mDatabase = _db;
        internalInitInvalidationTracker(_db);
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onOpen(_db);
          }
        }
      }

      @Override
      public void onPreMigrate(SupportSQLiteDatabase _db) {
        DBUtil.dropFtsSyncTriggers(_db);
      }

      @Override
      public void onPostMigrate(SupportSQLiteDatabase _db) {
      }

      @Override
      public RoomOpenHelper.ValidationResult onValidateSchema(SupportSQLiteDatabase _db) {
        final HashMap<String, TableInfo.Column> _columnsEnhancedSessionData = new HashMap<String, TableInfo.Column>(17);
        _columnsEnhancedSessionData.put("sessionId", new TableInfo.Column("sessionId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEnhancedSessionData.put("userId", new TableInfo.Column("userId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEnhancedSessionData.put("sessionDuration", new TableInfo.Column("sessionDuration", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEnhancedSessionData.put("unlockCount", new TableInfo.Column("unlockCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEnhancedSessionData.put("appName", new TableInfo.Column("appName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEnhancedSessionData.put("appCategory", new TableInfo.Column("appCategory", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEnhancedSessionData.put("notifCount", new TableInfo.Column("notifCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEnhancedSessionData.put("notifResponse", new TableInfo.Column("notifResponse", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEnhancedSessionData.put("appSwitchCount", new TableInfo.Column("appSwitchCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEnhancedSessionData.put("timeOfDay", new TableInfo.Column("timeOfDay", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEnhancedSessionData.put("consecutiveSameApp", new TableInfo.Column("consecutiveSameApp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEnhancedSessionData.put("bingeFlag", new TableInfo.Column("bingeFlag", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEnhancedSessionData.put("dopamineSpikeFlag", new TableInfo.Column("dopamineSpikeFlag", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEnhancedSessionData.put("addictionFlag", new TableInfo.Column("addictionFlag", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEnhancedSessionData.put("scrollsPerMinute", new TableInfo.Column("scrollsPerMinute", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEnhancedSessionData.put("unlockFrequency", new TableInfo.Column("unlockFrequency", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEnhancedSessionData.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEnhancedSessionData = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesEnhancedSessionData = new HashSet<TableInfo.Index>(5);
        _indicesEnhancedSessionData.add(new TableInfo.Index("idx_user_timestamp", false, Arrays.asList("userId","timestamp"), Arrays.asList("ASC","ASC")));
        _indicesEnhancedSessionData.add(new TableInfo.Index("idx_app_category", false, Arrays.asList("appCategory"), Arrays.asList("ASC")));
        _indicesEnhancedSessionData.add(new TableInfo.Index("idx_dopamine_flag", false, Arrays.asList("dopamineSpikeFlag"), Arrays.asList("ASC")));
        _indicesEnhancedSessionData.add(new TableInfo.Index("idx_addiction_flag", false, Arrays.asList("addictionFlag"), Arrays.asList("ASC")));
        _indicesEnhancedSessionData.add(new TableInfo.Index("idx_timestamp", false, Arrays.asList("timestamp"), Arrays.asList("ASC")));
        final TableInfo _infoEnhancedSessionData = new TableInfo("enhanced_session_data", _columnsEnhancedSessionData, _foreignKeysEnhancedSessionData, _indicesEnhancedSessionData);
        final TableInfo _existingEnhancedSessionData = TableInfo.read(_db, "enhanced_session_data");
        if (! _infoEnhancedSessionData.equals(_existingEnhancedSessionData)) {
          return new RoomOpenHelper.ValidationResult(false, "enhanced_session_data(com.neuropulse.app.database.EnhancedSessionData).\n"
                  + " Expected:\n" + _infoEnhancedSessionData + "\n"
                  + " Found:\n" + _existingEnhancedSessionData);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "a1e8d0882fb794cd51eafd3687fe4389", "08d34745abcff57411bbd6c6f5d9637e");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
        .name(configuration.name)
        .callback(_openCallback)
        .build();
    final SupportSQLiteOpenHelper _helper = configuration.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "enhanced_session_data");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `enhanced_session_data`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(EnhancedSessionDao.class, EnhancedSessionDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  public List<Migration> getAutoMigrations(
      @NonNull Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecsMap) {
    return Arrays.asList();
  }

  @Override
  public EnhancedSessionDao sessionDao() {
    if (_enhancedSessionDao != null) {
      return _enhancedSessionDao;
    } else {
      synchronized(this) {
        if(_enhancedSessionDao == null) {
          _enhancedSessionDao = new EnhancedSessionDao_Impl(this);
        }
        return _enhancedSessionDao;
      }
    }
  }
}
