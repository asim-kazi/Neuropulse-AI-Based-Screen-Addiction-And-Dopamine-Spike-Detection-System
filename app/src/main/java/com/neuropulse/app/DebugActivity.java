package com.neuropulse.app;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;
import java.util.List;

public class DebugActivity extends AppCompatActivity {

    private TextView liveStats, debugInfo;
    private RecyclerView recyclerView;
    private SessionAdapter adapter;
    private AppDatabase db;
    private Button btnTestSession, btnClearSessions, btnRefresh;

    private final BroadcastReceiver sessionUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UsageMonitorService.ACTION_SESSION_UPDATE.equals(intent.getAction())) {
                String appName = intent.getStringExtra("app");
                long time = intent.getLongExtra("time", 0);
                String action = intent.getStringExtra("action");

                String statusText = "Last " + action + " app: " + appName + "\nAt: " +
                        new Date(time).toString();
                liveStats.setText(statusText);

                // Refresh sessions if an app was stopped (session ended)
                if ("stopped".equals(action)) {
                    refreshSessions();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        liveStats = findViewById(R.id.liveStats);
        debugInfo = findViewById(R.id.debugInfo);
        recyclerView = findViewById(R.id.recyclerView);

        btnTestSession = findViewById(R.id.btnTestSession);
        btnClearSessions = findViewById(R.id.btnClearSessions);
        btnRefresh = findViewById(R.id.btnRefresh);

        db = AppDatabase.getInstance(this);

        setupButtons();
        refreshSessions();
        updateDebugInfo();
    }

    private void setupButtons() {
        btnTestSession.setOnClickListener(v -> insertTestSession());
        btnClearSessions.setOnClickListener(v -> clearAllSessions());
        btnRefresh.setOnClickListener(v -> {
            refreshSessions();
            updateDebugInfo();
        });
    }

    private void insertTestSession() {
        new Thread(() -> {
            // Insert Instagram test session
            SessionEntity testSession1 = createTestSession("com.instagram.android", "social", 45000);
            db.sessionDao().insert(testSession1);

            // Insert YouTube test session
            SessionEntity testSession2 = createTestSession("com.google.android.youtube", "short-video", 120000);
            db.sessionDao().insert(testSession2);

            // Insert WhatsApp test session
            SessionEntity testSession3 = createTestSession("com.whatsapp", "social", 30000);
            db.sessionDao().insert(testSession3);

            runOnUiThread(() -> {
                Toast.makeText(this, "3 test sessions inserted", Toast.LENGTH_SHORT).show();
                refreshSessions();
            });
        }).start();
    }

    private SessionEntity createTestSession(String packageName, String category, long durationMs) {
        SessionEntity session = new SessionEntity();
        session.userInstallId = "test-user-" + System.currentTimeMillis();
        session.appPackage = packageName;
        session.appCategory = category;

        long now = System.currentTimeMillis();
        session.sessionEndTs = now;
        session.sessionStartTs = now - durationMs;
        session.sessionDurationSec = durationMs / 1000;
        session.timestamp = now;
        session.duration = durationMs;

        // Set addiction risk based on duration and category
        long minutes = durationMs / 60000;
        if (category.equals("social") || category.equals("short-video")) {
            if (minutes > 60) session.addictionRisk = "High";
            else if (minutes > 20) session.addictionRisk = "Medium";
            else session.addictionRisk = "Low";
        } else {
            session.addictionRisk = "Low";
        }

        session.unlocksLastHour = (int) (Math.random() * 20);
        session.notifCountLast30Min = (int) (Math.random() * 10);
        session.nightFlag = 0;
        session.bingeFlag = minutes > 30 ? 1 : 0;
        session.dopamineSpikeLabel = 0;
        session.returnAfterNotificationSec = -1;
        session.appSwitchCount = 0;
        session.consecutiveSameAppMinutes = (int) minutes;

        return session;
    }

    private void clearAllSessions() {
        new Thread(() -> {
            db.sessionDao().clearAll();
            runOnUiThread(() -> {
                Toast.makeText(this, "All sessions cleared", Toast.LENGTH_SHORT).show();
                refreshSessions();
            });
        }).start();
    }

    private void refreshSessions() {
        new Thread(() -> {
            List<SessionEntity> sessions = db.sessionDao().getAllSessions();
            runOnUiThread(() -> {
                if (adapter == null) {
                    adapter = new SessionAdapter(sessions);
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setAdapter(adapter);
                } else {
                    // Update adapter data
                    adapter = new SessionAdapter(sessions);
                    recyclerView.setAdapter(adapter);
                }

                liveStats.setText("Monitoring active...\nTotal sessions: " + sessions.size());
            });
        }).start();
    }

    private void updateDebugInfo() {
        new Thread(() -> {
            StringBuilder info = new StringBuilder();

            // Check permissions
            info.append("=== PERMISSIONS ===\n");
            boolean hasUsagePermission = hasUsageStatsPermission();
            info.append("Usage Stats Permission: ").append(hasUsagePermission).append("\n");

            // Check service status
            info.append("\n=== SERVICE STATUS ===\n");
            boolean serviceRunning = isServiceRunning(UsageMonitorService.class);
            info.append("UsageMonitorService Running: ").append(serviceRunning).append("\n");

            // Check recent usage events
            info.append("\n=== RECENT APPS (Last 5 minutes) ===\n");
            if (hasUsagePermission) {
                UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
                long end = System.currentTimeMillis();
                long start = end - 300000; // 5 minutes
                List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end);

                if (stats != null) {
                    int count = 0;
                    for (UsageStats stat : stats) {
                        if (stat.getLastTimeUsed() > start && count < 10) {
                            String appName = Utils.pkgToAppName(this, stat.getPackageName());
                            info.append("- ").append(appName)
                                    .append(" (").append(stat.getPackageName()).append(")\n");
                            count++;
                        }
                    }
                    if (count == 0) {
                        info.append("No recent app usage detected\n");
                    }
                } else {
                    info.append("Unable to retrieve usage stats\n");
                }
            } else {
                info.append("Cannot access usage stats - permission denied\n");
            }

            // Database info
            info.append("\n=== DATABASE ===\n");
            List<SessionEntity> allSessions = db.sessionDao().getAllSessions();
            info.append("Total sessions: ").append(allSessions.size()).append("\n");

            if (!allSessions.isEmpty()) {
                SessionEntity latest = allSessions.get(0);
                String appName = Utils.pkgToAppName(this, latest.appPackage);
                info.append("Latest session: ").append(appName).append("\n");
                info.append("Package: ").append(latest.appPackage).append("\n");
                info.append("Duration: ").append(latest.duration / 1000).append(" seconds\n");
                info.append("Risk: ").append(latest.addictionRisk).append("\n");
                info.append("Timestamp: ").append(new Date(latest.timestamp)).append("\n");
            }

            // System info
            info.append("\n=== SYSTEM INFO ===\n");
            info.append("Unlock count (last hour): ").append(UnlockReceiver.getUnlockCount(this)).append("\n");
            info.append("Notification count (30min): ").append(NLService.getNotifCountLast30Min()).append("\n");

            final String finalInfo = info.toString();

            runOnUiThread(() -> {
                if (debugInfo != null) {
                    debugInfo.setText(finalInfo);
                    Log.d("DebugActivity", finalInfo);
                }
            });
        }).start();
    }

    private boolean hasUsageStatsPermission() {
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long end = System.currentTimeMillis();
        long start = end - 60000; // 1 minute
        List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end);
        return stats != null && !stats.isEmpty();
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                sessionUpdateReceiver,
                new IntentFilter(UsageMonitorService.ACTION_SESSION_UPDATE)
        );
        refreshSessions();
        updateDebugInfo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(sessionUpdateReceiver);
    }
}