package com.example.lab5_iot_20197115.notifications;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.lab5_iot_20197115.MainActivity;
import com.example.lab5_iot_20197115.R;

import java.util.Locale;

public class ReminderReceiver extends BroadcastReceiver {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @Override public void onReceive(Context ctx, Intent intent) {
        NotificationHelper.ensureChannels(ctx);

        // Extras seguros
        String id = intent.getStringExtra("id");
        String name = intent.getStringExtra("name");
        double amount = intent.getDoubleExtra("amount", 0);
        String imp = intent.getStringExtra("importance");

        // Canal según importancia
        String channel = NotificationHelper.CH_DEFAULT;
        if ("ALTA".equals(imp))      channel = NotificationHelper.CH_HIGH;
        else if ("BAJA".equals(imp)) channel = NotificationHelper.CH_LOW;

        // Intent al abrir la app desde la notificación
        PendingIntent content = PendingIntent.getActivity(
                ctx, 0, new Intent(ctx, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);

        // Mensaje
        String body = String.format(Locale.getDefault(), "Monto: S/ %.2f (24h antes)", amount);

        NotificationCompat.Builder nb = new NotificationCompat.Builder(ctx, channel)
                .setSmallIcon(android.R.drawable.ic_popup_reminder) // ícono del sistema
                .setContentTitle("Pago por vencer: " + (name == null ? "Servicio" : name))
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(content);

        int notifId = (id != null ? id.hashCode() : (int) System.currentTimeMillis());
        NotificationManagerCompat.from(ctx).notify(notifId, nb.build());
    }
}