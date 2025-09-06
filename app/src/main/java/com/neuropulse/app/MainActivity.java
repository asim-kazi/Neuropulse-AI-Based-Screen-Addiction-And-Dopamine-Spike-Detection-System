package com.neuropulse.app;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button startBtn, stopBtn, reqUsageBtn, reqNotifBtn, debugBtn;
    private TextView tvTotalTime, tvMostUsed, tvAddictionRisk, tvStatus;
    private RecyclerView rvSessions;
    private SessionAdapter sessionAdapter;

    private SessionDao sessionDao;

    // Broadcast receiver to listen for session updates
    private final BroadcastReceiver sessionUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UsageMonitorService.ACTION_SESSION_UPDATE.equals(intent.getAction())) {
                String appName = intent.getStringExtra("app");
                String action = intent.getStringExtra("action");
                long time = intent.getLongExtra("time", 0);

                Log.d("MainActivity", "Session update: " + appName + " " + action);

                // Refresh sessions when a session ends
                if ("stopped".equals(action)) {
                    loadSessions(); // Reload the sessions from database
                }

                // Update status
                tvStatus.setText("Monitoring: ON\nLast " + action + ": " + appName);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        startBtn = findViewById(R.id.btnStart);
        stopBtn = findViewById(R.id.btnStop);
        reqUsageBtn = findViewById(R.id.btnReqUsage);
        reqNotifBtn = findViewById(R.id.btnReqNotif);
        debugBtn = findViewById(R.id.btnDebug);

        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvMostUsed = findViewById(R.id.tvMostUsed);
        tvAddictionRisk = findViewById(R.id.tvAddictionRisk);
        tvStatus = findViewById(R.id.tvStatus);

        rvSessions = findViewById(R.id.rvSessions);
        rvSessions.setLayoutManager(new LinearLayoutManager(this));

        // Initialize DB
        AppDatabase db = AppDatabase.getInstance(this);
        sessionDao = db.sessionDao();

        // Load recent sessions
        loadSessions();

        // Check initial status
        updateServiceStatus();

        // Button Listeners
        startBtn.setOnClickListener(v -> {
            if (hasUsageStatsPermission()) {
                startService(new Intent(this, UsageMonitorService.class));
                tvStatus.setText("Monitoring: ON");
                Toast.makeText(this, "Monitoring started", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please grant Usage Access permission first", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
            }
        });

        stopBtn.setOnClickListener(v -> {
            stopService(new Intent(this, UsageMonitorService.class));
            tvStatus.setText("Monitoring: OFF");
            Toast.makeText(this, "Monitoring stopped", Toast.LENGTH_SHORT).show();
        });

        reqUsageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        });

        reqNotifBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        });

        debugBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, DebugActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register for session updates
        LocalBroadcastManager.getInstance(this).registerReceiver(
                sessionUpdateReceiver,
                new IntentFilter(UsageMonitorService.ACTION_SESSION_UPDATE)
        );

        // Refresh sessions when activity resumes
        loadSessions();
        updateServiceStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(sessionUpdateReceiver);
    }

    private void loadSessions() {
        new Thread(() -> {
            List<SessionEntity> sessions = sessionDao.getRecentSessions();
            runOnUiThread(() -> {
                sessionAdapter = new SessionAdapter(sessions);
                rvSessions.setAdapter(sessionAdapter);

                // Update dashboard with stats
                updateDashboard(sessions);
            });
        }).start();
    }

    private void updateDashboard(List<SessionEntity> sessions) {
        if (sessions == null || sessions.isEmpty()) {
            tvTotalTime.setText("0m");
            tvMostUsed.setText("N/A");
            tvAddictionRisk.setText("Low");
            return;
        }

        long totalTime = 0;
        String mostUsedApp = "";
        long maxTime = 0;

        for (SessionEntity s : sessions) {
            long duration = s.duration / 1000 / 60; // minutes
            totalTime += duration;
            if (s.duration > maxTime) {
                maxTime = s.duration;
                mostUsedApp = Utils.pkgToAppName(this, s.appPackage);
            }
        }

        tvTotalTime.setText(totalTime + "m");
        tvMostUsed.setText(mostUsedApp.isEmpty() ? "N/A" : mostUsedApp);

        // Simple risk logic based on total time
        if (totalTime > 300) {
            tvAddictionRisk.setText("High");
            tvAddictionRisk.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else if (totalTime > 120) {
            tvAddictionRisk.setText("Medium");
            tvAddictionRisk.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            tvAddictionRisk.setText("Low");
            tvAddictionRisk.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }

    private void updateServiceStatus() {
        boolean serviceRunning = isServiceRunning(UsageMonitorService.class);
        boolean hasPermission = hasUsageStatsPermission();

        if (serviceRunning && hasPermission) {
            tvStatus.setText("Monitoring: ON");
        } else if (!hasPermission) {
            tvStatus.setText("Permission needed");
        } else {
            tvStatus.setText("Monitoring: OFF");
        }
    }

    private boolean hasUsageStatsPermission() {
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long end = System.currentTimeMillis();
        long start = end - 1000 * 60; // 1 minute ago

        List<UsageStats> usageStatsList = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, start, end);

        return usageStatsList != null && !usageStatsList.isEmpty();
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
}