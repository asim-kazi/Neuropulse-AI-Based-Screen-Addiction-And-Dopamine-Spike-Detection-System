package com.neuropulse.app;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.concurrent.atomic.AtomicInteger;

public class NLService extends NotificationListenerService {
    private static final String TAG = "NLService";
    private static final AtomicInteger notifCount30Min = new AtomicInteger(0);

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.d(TAG, "Notification Listener connected");
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.d(TAG, "Notification Listener disconnected");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        notifCount30Min.incrementAndGet();
        Log.d(TAG, "Notification from: " + sbn.getPackageName());
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "Notification removed: " + sbn.getPackageName());
    }

    public static int getNotifCountLast30Min() {
        return notifCount30Min.get();
    }
}
