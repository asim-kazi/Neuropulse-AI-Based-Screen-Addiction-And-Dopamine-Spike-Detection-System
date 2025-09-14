// AppDatabase.java
package com.neuropulse.app.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import android.content.Context;
import android.util.Log;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SupportFactory;

@Database(
        entities = {EnhancedSessionData.class},
        version = 2,
        exportSchema = true
)
public abstract class AppDatabase extends RoomDatabase {
    private static final String TAG = "AppDatabase";
    private static volatile AppDatabase INSTANCE;
    private static final String DATABASE_NAME = "neuropulse_encrypted.db";

    public abstract EnhancedSessionDao sessionDao();
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = buildDatabase(context);
                }
            }
        }
        return INSTANCE;
    }

    private static AppDatabase buildDatabase(Context context) {
        // Encrypt database for user privacy
        byte[] passphrase = SQLiteDatabase.getBytes("neuropulse_secure_key".toCharArray());
        SupportFactory factory = new SupportFactory(passphrase);

        return Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        DATABASE_NAME
                )
                .openHelperFactory(factory)
                .addMigrations(MIGRATION_1_2)
                .addCallback(new DatabaseCallback())
                .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                .fallbackToDestructiveMigration() // For development only
                .build();
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            try {
                // Add indexes for better query performance
                database.execSQL("CREATE INDEX IF NOT EXISTS index_session_appCategory " +
                        "ON enhanced_session_data(appCategory)");
                database.execSQL("CREATE INDEX IF NOT EXISTS index_session_dopamineFlag " +
                        "ON enhanced_session_data(dopamineSpikeFlag)");
                database.execSQL("CREATE INDEX IF NOT EXISTS index_session_userId_timestamp " +
                        "ON enhanced_session_data(userId, timestamp DESC)");

                // Add data retention trigger
                database.execSQL("CREATE TRIGGER IF NOT EXISTS cleanup_old_data " +
                        "AFTER INSERT ON enhanced_session_data " +
                        "BEGIN " +
                        "DELETE FROM enhanced_session_data " +
                        "WHERE timestamp < (NEW.timestamp - 2592000000); " + // 30 days
                        "END");

                Log.i(TAG, "Migration 1->2 completed successfully");
            } catch (Exception e) {
                Log.e(TAG, "Migration failed", e);
                throw e;
            }
        }
    };

    private static class DatabaseCallback extends RoomDatabase.Callback {
        @Override
        public void onCreate(SupportSQLiteDatabase db) {
            super.onCreate(db);
            Log.i(TAG, "Database created successfully");
        }

        @Override
        public void onOpen(SupportSQLiteDatabase db) {
            super.onOpen(db);
            // Enable WAL mode and configure for better performance
            db.execSQL("PRAGMA journal_mode=WAL");
            db.execSQL("PRAGMA synchronous=NORMAL");
            db.execSQL("PRAGMA cache_size=10000");
        }
    }

    public static void destroyInstance() {
        synchronized (AppDatabase.class) {
            if (INSTANCE != null && INSTANCE.isOpen()) {
                INSTANCE.close();
            }
            INSTANCE = null;
        }
    }

    // Add backup functionality
    public static void backupDatabase(Context context) {
        // Implementation for database backup
        Log.i(TAG, "Database backup initiated");
    }
}