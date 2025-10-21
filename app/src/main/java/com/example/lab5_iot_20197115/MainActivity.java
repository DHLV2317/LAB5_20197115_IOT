package com.example.lab5_iot_20197115;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab5_iot_20197115.data.LocalStore;
import com.example.lab5_iot_20197115.data.PaymentRecord;
import com.example.lab5_iot_20197115.data.ServiceReminder;
import com.example.lab5_iot_20197115.notifications.NotificationHelper;
import com.example.lab5_iot_20197115.notifications.ReminderScheduler;
import com.example.lab5_iot_20197115.ui.ReminderFormActivity;
import com.example.lab5_iot_20197115.ui.RemindersAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private LocalStore store;
    private List<ServiceReminder> items = new ArrayList<>();
    private List<PaymentRecord> history = new ArrayList<>();
    private RemindersAdapter adapter;

    private final ActivityResultLauncher<Intent> addEditLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
                if (res.getResultCode() == RESULT_OK && res.getData() != null) {
                    String json = res.getData().getStringExtra(ReminderFormActivity.EXTRA_RESULT);
                    ServiceReminder r = new com.google.gson.Gson().fromJson(json, ServiceReminder.class);
                    upsertReminder(r);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        // Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Recordatorios de Pagos");

        store = new LocalStore(this);
        NotificationHelper.ensureChannels(this);
        requestPostNotificationsIfNeeded();

        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RemindersAdapter(items, new RemindersAdapter.Listener() {
            @Override public void onPay(ServiceReminder r) { markAsPaidAndCreateNext(r); }
            @Override public void onEdit(ServiceReminder r) { editReminder(r); }
            @Override public void onDelete(ServiceReminder r) { deleteReminder(r); }
        });
        rv.setAdapter(adapter);

        Button btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> addReminder());

        loadAll();
        refreshEmpty();

        // Reprograma todas las notificaciones pendientes al abrir la app
        rescheduleExisting();
    }

    private void rescheduleExisting() {
        for (ServiceReminder r : items) {
            if (!r.paid) {
                ReminderScheduler.cancel(this, r.id);
                ReminderScheduler.schedule(this, r);
            }
        }
    }

    private void requestPostNotificationsIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 100);
        }
    }

    private void loadAll() {
        items = store.loadReminders();
        history = store.loadHistory();
        adapter = new RemindersAdapter(items, new RemindersAdapter.Listener() {
            @Override public void onPay(ServiceReminder r) { markAsPaidAndCreateNext(r); }
            @Override public void onEdit(ServiceReminder r) { editReminder(r); }
            @Override public void onDelete(ServiceReminder r) { deleteReminder(r); }
        });
        ((RecyclerView) findViewById(R.id.recycler)).setAdapter(adapter);
    }

    private void saveAll() {
        store.saveReminders(items);
        store.saveHistory(history);
    }

    private void refreshEmpty() {
        findViewById(R.id.empty)
                .setVisibility(items.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private void addReminder() {
        addEditLauncher.launch(new Intent(this, ReminderFormActivity.class));
    }

    private void editReminder(ServiceReminder r) {
        Intent i = new Intent(this, ReminderFormActivity.class);
        i.putExtra(ReminderFormActivity.EXTRA_EDIT, new com.google.gson.Gson().toJson(r));
        addEditLauncher.launch(i);
    }

    private void upsertReminder(ServiceReminder r) {
        int idx = -1;
        for (int i = 0; i < items.size(); i++)
            if (items.get(i).id.equals(r.id)) { idx = i; break; }
        if (idx >= 0) items.set(idx, r); else items.add(r);
        saveAll(); adapter.notifyDataSetChanged(); refreshEmpty();

        // Re-programar notificación (24 h antes)
        ReminderScheduler.cancel(this, r.id);
        ReminderScheduler.schedule(this, r);
    }

    private void deleteReminder(ServiceReminder r) {
        ReminderScheduler.cancel(this, r.id);
        items.removeIf(x -> x.id.equals(r.id));
        saveAll(); adapter.notifyDataSetChanged(); refreshEmpty();
    }

    private void markAsPaidAndCreateNext(ServiceReminder r) {
        long now = System.currentTimeMillis();
        long daysBefore = TimeUnit.MILLISECONDS.toDays(r.dueDateMillis - now);
        history.add(new PaymentRecord(r.id, r.name, r.amount, now, (int) daysBefore));

        ServiceReminder.Periodicity p = r.periodicity;
        if (p == ServiceReminder.Periodicity.UNA_VEZ) {
            deleteReminder(r);
        } else {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(r.dueDateMillis);
            switch (p) {
                case MENSUAL:     c.add(Calendar.MONTH, 1); break;
                case BIMESTRAL:   c.add(Calendar.MONTH, 2); break;
                case TRIMESTRAL:  c.add(Calendar.MONTH, 3); break;
                case ANUAL:       c.add(Calendar.YEAR, 1);  break;
            }
            r.dueDateMillis = c.getTimeInMillis();
            r.paid = false;
            upsertReminder(r);
            Toast.makeText(this, "Pagado y reprogramado", Toast.LENGTH_SHORT).show();
        }
        saveAll();
    }

    // ===== Menú =====
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_history) {
            startActivity(new Intent(this, com.example.lab5_iot_20197115.ui.HistoryActivity.class));
            return true;

        } else if (id == R.id.action_test_notify) {
            // Una sola notificación de prueba en 5 s
            ReminderScheduler.scheduleTest(
                    this,
                    "TEST_ONE",
                    "Servicio de prueba",
                    15.90,
                    ServiceReminder.Importance.ALTA.name(),
                    5000L
            );
            Toast.makeText(this, "Notificación de prueba en 5 s", Toast.LENGTH_SHORT).show();
            return true;

        } else if (id == R.id.action_test_all) {
            // Probar con TODOS los servicios creados (escalonadas cada 3 s)
            if (items.isEmpty()) {
                Toast.makeText(this, "No hay servicios para probar", Toast.LENGTH_SHORT).show();
                return true;
            }
            long delay = 3000L; // 3 s entre cada una
            long acc = 0L;
            for (ServiceReminder r : items) {
                ReminderScheduler.scheduleTest(
                        this,
                        r.id,
                        r.name == null ? "Servicio" : r.name,
                        r.amount,
                        r.importance.name(),
                        acc + delay
                );
                acc += delay;
            }
            Toast.makeText(this, "Notificaciones de prueba programadas", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}