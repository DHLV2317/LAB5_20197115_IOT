package com.example.lab5_iot_20197115.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.lab5_iot_20197115.data.ServiceReminder;

public class ReminderScheduler {

    /** Programa una notificación 24h antes de la fecha de vencimiento */
    public static void schedule(Context ctx, ServiceReminder r) {
        if (r == null) return;

        long triggerAt = r.dueDateMillis - 24L * 60 * 60 * 1000; // 24h antes
        long now = System.currentTimeMillis();
        if (triggerAt <= now) return; // si ya pasó, no programes

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        PendingIntent pi = pending(ctx, r.id, r.name, r.amount, r.importance.name());

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+: requiere permiso SCHEDULE_EXACT_ALARM para alarmas exactas
                if (am.canScheduleExactAlarms()) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
                } else {
                    // Fallback sin crash: no exacta pero funciona y no necesita el permiso
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
                }
            } else {
                // Android <= 11
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            }
        } catch (SecurityException se) {
            // Última red: si por algo lanza SecurityException, evita crash y usa fallback
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }

    /** Cancela la alarma para el id dado */
    public static void cancel(Context ctx, String id) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null || id == null) return;
        am.cancel(pending(ctx, id, "", 0, "MEDIA"));
    }

    /** Crea el PendingIntent único por recordatorio */
    private static PendingIntent pending(Context ctx, String id, String name, double amount, String imp) {
        Intent i = new Intent(ctx, ReminderReceiver.class);
        i.putExtra("id", id);
        i.putExtra("name", name);
        i.putExtra("amount", amount);
        i.putExtra("importance", imp);

        int requestCode = (id == null ? 0 : id.hashCode());
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getBroadcast(ctx, requestCode, i, flags);
    }
}