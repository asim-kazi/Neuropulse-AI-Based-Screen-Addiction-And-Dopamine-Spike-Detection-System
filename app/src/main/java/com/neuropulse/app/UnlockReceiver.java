// FileName: MultipleFiles/UnlockReceiver.java
package com.neuropulse.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class UnlockReceiver extends BroadcastReceiver {
    private static final String PREF = "neuropulse_prefs";
    private static final String KEY_UNLOCK_COUNT = "unlock_count_hour";
    private static final String KEY_LAST_RESET_TIME = "last_reset_time"; // To track when it was last reset

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);

        // Simple hourly reset logic (can be improved with a proper scheduler)
        long currentTime = System.currentTimeMillis();
        long lastResetTime = sp.getLong(KEY_LAST_RESET_TIME, 0);

        // Reset if more than an hour has passed since last reset or if it's the first run
        // This is a very basic heuristic; a proper scheduler (WorkManager) is recommended.
        if (currentTime - lastResetTime > 60 * 60 * 1000) { // 1 hour in milliseconds
            sp.edit().putInt(KEY_UNLOCK_COUNT, 0).putLong(KEY_LAST_RESET_TIME, currentTime).apply();
        }

        // Increase unlock counter
        int count = sp.getInt(KEY_UNLOCK_COUNT, 0);
        sp.edit().putInt(KEY_UNLOCK_COUNT, count + 1).apply();
    }

    // helper to get current unlock count
    public static int getUnlockCount(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        // Optionally, you could also trigger a reset check here before returning the count
        // if you want the count to be "fresh" every time it's retrieved.
        return sp.getInt(KEY_UNLOCK_COUNT, 0);
    }

    // Helper to explicitly reset the count (can be called by a scheduled job)
    public static void resetUnlockCount(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putInt(KEY_UNLOCK_COUNT, 0).putLong(KEY_LAST_RESET_TIME, System.currentTimeMillis()).apply();
    }
}
    