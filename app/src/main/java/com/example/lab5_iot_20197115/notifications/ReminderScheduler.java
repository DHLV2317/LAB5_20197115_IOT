package com.example.lab5_iot_20197115.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.lab5_iot_20197115.data.ServiceReminder;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class ReminderScheduler {

    private static final String TAG = "ReminderScheduler";

    /** Activa modo demo para programar la notificación a pocos minutos (útil para clase). */
    private static final boolean DEMO_MODE = true;
    /** Anticipación en modo demo: 2 minutos antes. */
    private static final long DEMO_LEAD_MS = 2L * 60L * 1000L;

    /** Programa una notificación 24 h antes de la fecha de vencimiento (o DEMO si está activo). */
    public static void schedule(Context ctx, ServiceReminder r) {
        if (r == null || r.id == null) return;

        long leadTime = DEMO_MODE ? DEMO_LEAD_MS : 24L * 60L * 60L * 1000L; // 24 h o 2 min
        long triggerAt = r.dueDateMillis - leadTime;
        long now = System.currentTimeMillis();
        if (triggerAt <= now) {
            Log.d(TAG, "No se programa (ya pasó): " + r.name + " id=" + r.id);
            return;
        }

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        PendingIntent pi = pending(ctx, r.id, r.name, r.amount, r.importance.name());

        String when = DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault()
        ).format(new Date(triggerAt));
        Log.d(TAG, "Programando '" + r.name + "' id=" + r.id + " para: " + when +
                " (lead=" + (DEMO_MODE ? "DEMO 2min" : "24h") + ")");

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (am.canScheduleExactAlarms()) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
                } else {
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            }
        } catch (SecurityException e) {
            // Fallback sin crash
            Log.w(TAG, "Sin permiso exacto; usando setAndAllowWhileIdle. " + e.getMessage());
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }

    /** ⚙️ Modo prueba: dispara una notificación en delayMs desde ahora. */
    public static void scheduleTest(Context ctx, String id, String name, double amount, String imp, long delayMs) {
        long triggerAt = System.currentTimeMillis() + delayMs;

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        PendingIntent pi = pending(ctx, id, name, amount, imp);

        String when = DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault()
        ).format(new Date(triggerAt));
        Log.d(TAG, "Programando TEST '" + name + "' id=" + id + " para: " + when);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }

    /** Cancela la alarma para el id dado. */
    public static void cancel(Context ctx, String id) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null || id == null) return;
        am.cancel(pending(ctx, id, "", 0, "MEDIA"));
        Log.d(TAG, "Cancelada alarma id=" + id);
    }

    /** Crea el PendingIntent único por recordatorio (por id). */
    private static PendingIntent pending(Context ctx, String id, String name, double amount, String imp) {
        Intent i = new Intent(ctx, ReminderReceiver.class);
        i.putExtra("id", id);
        i.putExtra("name", name);
        i.putExtra("amount", amount);
        i.putExtra("importance", imp);

        int req = id.hashCode();
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getBroadcast(ctx, req, i, flags);
    }
}