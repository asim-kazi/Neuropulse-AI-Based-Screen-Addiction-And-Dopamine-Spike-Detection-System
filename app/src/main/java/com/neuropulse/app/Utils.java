package com.neuropulse.app;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.Calendar;
import java.util.Locale;

public class Utils {

    public static String pkgToAppName(Context ctx, String pkg) {
        if (pkg == null || pkg.trim().isEmpty()) {
            return "Unknown App";
        }
        try {
            PackageManager pm = ctx.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
            return pm.getApplicationLabel(ai).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return pkg; // fallback to package string
        } catch (Exception e) {
            return "Unknown App";
        }
    }

    public static String pkgToCategory(String pkg) {
        if (pkg == null) return "utility";
        pkg = pkg.toLowerCase(Locale.ROOT);

        if (pkg.contains("youtube") || pkg.contains("tiktok") || pkg.contains("reel") || pkg.contains("shorts"))
            return "short-video";

        if (pkg.contains("facebook") || pkg.contains("instagram") || pkg.contains("twitter") || pkg.contains("x") || pkg.contains("snap"))
            return "social";

        if (pkg.contains("spotify") || pkg.contains("music") || pkg.contains("netflix") || pkg.contains("primevideo"))
            return "entertainment";

        if (pkg.contains("game") || pkg.contains("puzzle") || pkg.contains("king"))
            return "gaming";

        return "utility";
    }

    public static boolean isNight(long ts) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ts);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        return (hour < 6 || hour >= 23); // customize for your needs
    }
}
