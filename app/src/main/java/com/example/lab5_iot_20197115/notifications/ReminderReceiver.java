package com.example.lab5_iot_20197115.notifications;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.lab5_iot_20197115.MainActivity;
import com.example.lab5_iot_20197115.R;

import android.app.PendingIntent;

import java.util.Locale;

public class ReminderReceiver extends BroadcastReceiver {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @Override public void onReceive(Context ctx, Intent intent) {
        NotificationHelper.ensureChannels(ctx);

        // Extras (con defaults seguros)
        String id    = intent.getStringExtra("id");
        String name  = intent.getStringExtra("name");
        double amount = intent.getDoubleExtra("amount", 0);
        String imp   = intent.getStringExtra("importance");

        // Canal según importancia
        String channel = NotificationHelper.CH_DEFAULT;
        if ("ALTA".equals(imp))      channel = NotificationHelper.CH_HIGH;
        else if ("BAJA".equals(imp)) channel = NotificationHelper.CH_LOW;

        // Intent al abrir la app desde la notificación
        PendingIntent content = PendingIntent.getActivity(
                ctx,
                0,
                new Intent(ctx, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE
        );

        // Texto con Locale para evitar warning de Lint
        String body = String.format(Locale.getDefault(),
                "Monto: S/ %.2f (24h antes)", amount);

        NotificationCompat.Builder nb = new NotificationCompat.Builder(ctx, channel)
                .setSmallIcon(R.drawable.ic_notification) // usa uno tuyo o android.R.drawable.ic_dialog_info
                .setContentTitle("Pago por vencer: " + (name == null ? "Servicio" : name))
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(content);

        // ID de notificación seguro (evita NPE si id viene null)
        int notifId = (id != null ? id.hashCode()
                : (name != null ? name.hashCode() : (int) System.currentTimeMillis()));

        // Usa NotificationManagerCompat (más simple)
        NotificationManagerCompat.from(ctx).notify(notifId, nb.build());
    }
}