// NotificationListenerService.java
package com.neuropulse.app.services;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.content.Intent;
import android.util.Log;

public class NotificationListener extends NotificationListenerService {
    private static final String TAG = "NeuropulseNotifListener";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        String packageName = sbn.getPackageName();
        String title = "";
        String text = "";

        if (sbn.getNotification().extras != null) {
            title = sbn.getNotification().extras.getString("android.title", "");
            text = sbn.getNotification().extras.getString("android.text", "");
        }

        // Log notification for tracking (in production, store in database)
        Log.d(TAG, String.format("Notification from %s: %s - %s", packageName, title, text));

        // Broadcast to other components if needed
        Intent intent = new Intent("com.neuropulse.NOTIFICATION_POSTED");
        intent.putExtra("package", packageName);
        intent.putExtra("title", title);
        intent.putExtra("text", text);
        intent.putExtra("timestamp", System.currentTimeMillis());
        sendBroadcast(intent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);

        String packageName = sbn.getPackageName();

        // Broadcast notification removal
        Intent intent = new Intent("com.neuropulse.NOTIFICATION_REMOVED");
        intent.putExtra("package", packageName);
        intent.putExtra("timestamp", System.currentTimeMillis());
        sendBroadcast(intent);
    }
}