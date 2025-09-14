// BootReceiver.java
package com.neuropulse.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.neuropulse.app.services.UsageMonitorService;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
                Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {

            // Restart the monitoring service after boot
            Intent serviceIntent = new Intent(context, UsageMonitorService.class);
            context.startForegroundService(serviceIntent);
        }
    }
}