// CrashReporter.java
// Location: app/src/main/java/com/neuropulse/app/utils/CrashReporter.java
package com.neuropulse.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CrashReporter implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashReporter";
    private static final String PREFS_NAME = "crash_reports";
    private static final String KEY_CRASH_COUNT = "crash_count";
    private static final String KEY_LAST_CRASH = "last_crash_time";

    private Thread.UncaughtExceptionHandler defaultHandler;
    private Context context;

    public static void initialize(Context context) {
        Thread.setDefaultUncaughtExceptionHandler(new CrashReporter(context));
    }

    private CrashReporter(Context context) {
        this.context = context.getApplicationContext();
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        try {
            // Log the crash
            String crashReport = generateCrashReport(thread, throwable);
            Log.e(TAG, "CRASH DETECTED:\n" + crashReport);

            // Store crash info
            storeCrashInfo(crashReport);

            // Performance cleanup before crash
            System.gc();

        } catch (Exception e) {
            Log.e(TAG, "Error in crash handler", e);
        } finally {
            // Call default handler to allow system crash dialog
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            }
        }
    }

    private String generateCrashReport(Thread thread, Throwable throwable) {
        StringBuilder report = new StringBuilder();

        // Basic info
        report.append("NEUROPULSE CRASH REPORT\n");
        report.append("Time: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date())).append("\n");
        report.append("Thread: ").append(thread.getName()).append("\n");

        // Performance stats if available
        try {
            PerformanceManager perfManager = PerformanceManager.getInstance(context);
            PerformanceManager.PerformanceStats stats = perfManager.getPerformanceStats();
            report.append("Performance: ").append(stats.successRate).append("% success, ")
                    .append(stats.currentMemoryMB).append("MB memory\n");
        } catch (Exception e) {
            report.append("Performance stats unavailable\n");
        }

        // Stack trace
        report.append("Exception: ").append(throwable.getClass().getSimpleName()).append("\n");
        report.append("Message: ").append(throwable.getMessage()).append("\n");

        StringWriter stackTrace = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stackTrace));
        report.append("Stack trace:\n").append(stackTrace.toString());

        return report.toString();
    }

    private void storeCrashInfo(String crashReport) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

            int crashCount = prefs.getInt(KEY_CRASH_COUNT, 0) + 1;
            long currentTime = System.currentTimeMillis();

            prefs.edit()
                    .putInt(KEY_CRASH_COUNT, crashCount)
                    .putLong(KEY_LAST_CRASH, currentTime)
                    .putString("crash_report_" + crashCount, crashReport)
                    .apply();

            Log.i(TAG, "Crash report stored (crash #" + crashCount + ")");

        } catch (Exception e) {
            Log.e(TAG, "Failed to store crash info", e);
        }
    }

    public static int getCrashCount(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_CRASH_COUNT, 0);
    }

    public static long getLastCrashTime(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getLong(KEY_LAST_CRASH, 0);
    }

    public static String getLastCrashReport(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int crashCount = prefs.getInt(KEY_CRASH_COUNT, 0);
        if (crashCount > 0) {
            return prefs.getString("crash_report_" + crashCount, "No crash report available");
        }
        return "No crashes recorded";
    }

    public static void clearCrashReports(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
        Log.i(TAG, "All crash reports cleared");
    }
}