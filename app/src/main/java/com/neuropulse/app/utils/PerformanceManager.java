// PerformanceManager.java
// Location: app/src/main/java/com/neuropulse/app/utils/PerformanceManager.java
package com.neuropulse.app.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.util.Log;

import java.util.concurrent.atomic.AtomicLong;

public class PerformanceManager {
    private static final String TAG = "PerformanceManager";
    private static PerformanceManager instance;

    private Context context;
    private AtomicLong totalOperations = new AtomicLong(0);
    private AtomicLong successfulOperations = new AtomicLong(0);
    private AtomicLong failedOperations = new AtomicLong(0);

    // Memory tracking
    private long initialMemory;
    private long peakMemory;

    // Timing tracking
    private long serviceStartTime;
    private long lastPerformanceLog;

    private PerformanceManager(Context context) {
        this.context = context.getApplicationContext();
        this.serviceStartTime = System.currentTimeMillis();
        this.initialMemory = getUsedMemory();
        this.peakMemory = initialMemory;
        this.lastPerformanceLog = serviceStartTime;
    }

    public static synchronized PerformanceManager getInstance(Context context) {
        if (instance == null) {
            instance = new PerformanceManager(context);
        }
        return instance;
    }

    public void recordOperation(boolean successful) {
        totalOperations.incrementAndGet();
        if (successful) {
            successfulOperations.incrementAndGet();
        } else {
            failedOperations.incrementAndGet();
        }

        // Track memory usage
        long currentMemory = getUsedMemory();
        if (currentMemory > peakMemory) {
            peakMemory = currentMemory;
        }

        // Log performance every 5 minutes
        if (System.currentTimeMillis() - lastPerformanceLog > 5 * 60 * 1000) {
            logPerformanceMetrics();
        }
    }

    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    public PerformanceStats getPerformanceStats() {
        long currentTime = System.currentTimeMillis();
        long uptime = currentTime - serviceStartTime;
        long currentMemory = getUsedMemory();

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memInfo);

        float successRate = totalOperations.get() > 0 ?
                (successfulOperations.get() / (float) totalOperations.get()) * 100f : 0f;

        return new PerformanceStats(
                uptime,
                totalOperations.get(),
                successfulOperations.get(),
                failedOperations.get(),
                successRate,
                currentMemory,
                peakMemory,
                memInfo.availMem,
                memInfo.lowMemory
        );
    }

    private void logPerformanceMetrics() {
        PerformanceStats stats = getPerformanceStats();

        Log.i(TAG, String.format(
                "Performance Summary - Uptime: %dms, Operations: %d (%.1f%% success), " +
                        "Memory: %dMB current, %dMB peak, Available: %dMB, Low memory: %b",
                stats.uptimeMs,
                stats.totalOperations,
                stats.successRate,
                stats.currentMemoryMB,
                stats.peakMemoryMB,
                stats.availableMemoryMB,
                stats.isLowMemory
        ));

        // Warning for concerning metrics
        if (stats.successRate < 80f) {
            Log.w(TAG, "Low success rate detected: " + stats.successRate + "%");
        }

        if (stats.isLowMemory) {
            Log.w(TAG, "System is running low on memory");
        }

        if (stats.currentMemoryMB > 100) { // More than 100MB
            Log.w(TAG, "High memory usage detected: " + stats.currentMemoryMB + "MB");
        }

        lastPerformanceLog = System.currentTimeMillis();
    }

    public boolean shouldReduceOperations() {
        PerformanceStats stats = getPerformanceStats();
        return stats.isLowMemory || stats.successRate < 70f || stats.currentMemoryMB > 150;
    }

    public void forceGarbageCollection() {
        Log.i(TAG, "Forcing garbage collection");
        System.gc();

        // Log memory improvement
        long memoryAfterGC = getUsedMemory();
        Log.i(TAG, "Memory after GC: " + (memoryAfterGC / 1024 / 1024) + "MB");
    }

    public static class PerformanceStats {
        public final long uptimeMs;
        public final long totalOperations;
        public final long successfulOperations;
        public final long failedOperations;
        public final float successRate;
        public final long currentMemoryMB;
        public final long peakMemoryMB;
        public final long availableMemoryMB;
        public final boolean isLowMemory;

        PerformanceStats(long uptimeMs, long totalOperations, long successfulOperations,
                         long failedOperations, float successRate, long currentMemory,
                         long peakMemory, long availableMemory, boolean isLowMemory) {
            this.uptimeMs = uptimeMs;
            this.totalOperations = totalOperations;
            this.successfulOperations = successfulOperations;
            this.failedOperations = failedOperations;
            this.successRate = successRate;
            this.currentMemoryMB = currentMemory / 1024 / 1024;
            this.peakMemoryMB = peakMemory / 1024 / 1024;
            this.availableMemoryMB = availableMemory / 1024 / 1024;
            this.isLowMemory = isLowMemory;
        }

        public String getFormattedUptime() {
            long seconds = uptimeMs / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;

            if (hours > 0) {
                return String.format("%dh %dm", hours, minutes % 60);
            } else {
                return String.format("%dm %ds", minutes, seconds % 60);
            }
        }
    }
}