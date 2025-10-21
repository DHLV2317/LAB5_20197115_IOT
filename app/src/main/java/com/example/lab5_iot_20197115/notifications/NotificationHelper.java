package com.example.lab5_iot_20197115.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class NotificationHelper {
    public static final String CH_HIGH = "reminders_high";
    public static final String CH_DEFAULT = "reminders_default";
    public static final String CH_LOW = "reminders_low";

    public static void ensureChannels(Context ctx) {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager nm = ctx.getSystemService(NotificationManager.class);
        if (nm.getNotificationChannel(CH_HIGH) == null) {
            nm.createNotificationChannel(new NotificationChannel(
                    CH_HIGH, "Pagos (Alta)", NotificationManager.IMPORTANCE_HIGH));
        }
        if (nm.getNotificationChannel(CH_DEFAULT) == null) {
            nm.createNotificationChannel(new NotificationChannel(
                    CH_DEFAULT, "Pagos (Media)", NotificationManager.IMPORTANCE_DEFAULT));
        }
        if (nm.getNotificationChannel(CH_LOW) == null) {
            nm.createNotificationChannel(new NotificationChannel(
                    CH_LOW, "Pagos (Baja)", NotificationManager.IMPORTANCE_LOW));
        }
    }
}