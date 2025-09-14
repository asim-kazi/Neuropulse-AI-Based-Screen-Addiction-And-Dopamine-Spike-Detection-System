// MainActivity.java
package com.neuropulse.app;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.neuropulse.app.services.UsageMonitorService;

public class MainActivity extends AppCompatActivity {
    private TextView statusText;
    private Button startMonitoringBtn;
    private Button debugBtn;
    private Button permissionsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupClickListeners();
        updateStatus();
    }

    private void initViews() {
        statusText = findViewById(R.id.statusText);
        startMonitoringBtn = findViewById(R.id.startMonitoringBtn);
        debugBtn = findViewById(R.id.debugBtn);
        permissionsBtn = findViewById(R.id.permissionsBtn);
    }

    private void setupClickListeners() {
        startMonitoringBtn.setOnClickListener(v -> {
            if (hasUsageStatsPermission()) {
                startMonitoringService();
            } else {
                requestUsageStatsPermission();
            }
        });

        debugBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, EnhancedDebugActivity.class);
            startActivity(intent);
        });

        permissionsBtn.setOnClickListener(v -> {
            requestAllPermissions();
        });
    }

    private void startMonitoringService() {
        Intent serviceIntent = new Intent(this, UsageMonitorService.class);
        startForegroundService(serviceIntent);

        Toast.makeText(this, "Monitoring started", Toast.LENGTH_SHORT).show();
        updateStatus();
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void requestUsageStatsPermission() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
        Toast.makeText(this, "Please enable usage access for Neuropulse",
                Toast.LENGTH_LONG).show();
    }

    private void requestAllPermissions() {
        // Usage Stats Permission
        if (!hasUsageStatsPermission()) {
            requestUsageStatsPermission();
        }

        // Notification Listener Permission
        Intent notifIntent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        startActivity(notifIntent);
        Toast.makeText(this, "Please enable notification access for Neuropulse",
                Toast.LENGTH_LONG).show();
    }

    private void updateStatus() {
        String status = "Status: ";
        if (hasUsageStatsPermission()) {
            status += "Ready to monitor usage patterns\n";
            startMonitoringBtn.setText("Start Monitoring");
        } else {
            status += "Needs permissions\n";
            startMonitoringBtn.setText("Grant Permissions & Start");
        }

        status += "This app helps you understand your device usage patterns for digital wellness.";
        statusText.setText(status);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }
}