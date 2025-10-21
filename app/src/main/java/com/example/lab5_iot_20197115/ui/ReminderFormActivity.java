package com.example.lab5_iot_20197115.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab5_iot_20197115.R;
import com.example.lab5_iot_20197115.data.ServiceReminder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class ReminderFormActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT = "edit_json";
    public static final String EXTRA_RESULT = "result_json";

    private EditText inName, inAmount, inDate;
    private Spinner spPeriod, spImportance;
    private long selectedMillis = 0;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_reminder_form);

        // Toolbar con título dinámico y back
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String json = getIntent().getStringExtra(EXTRA_EDIT);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setTitle(json == null ? "Agregar servicio" : "Editar servicio");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        inName = findViewById(R.id.inputName);
        inAmount = findViewById(R.id.inputAmount);
        inDate = findViewById(R.id.inputDate);
        spPeriod = findViewById(R.id.spPeriod);
        spImportance = findViewById(R.id.spImportance);

        spPeriod.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, ServiceReminder.Periodicity.values()));
        spImportance.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, ServiceReminder.Importance.values()));

        inDate.setOnClickListener(v -> showDatePicker());

        final com.google.gson.Gson gson = new com.google.gson.Gson();
        if (json != null) {
            ServiceReminder r = gson.fromJson(json, ServiceReminder.class);
            inName.setText(r.name);
            inAmount.setText(String.valueOf(r.amount));
            selectedMillis = r.dueDateMillis;
            inDate.setText(android.text.format.DateFormat.getDateFormat(this).format(r.dueDateMillis));
            spPeriod.setSelection(r.periodicity.ordinal());
            spImportance.setSelection(r.importance.ordinal());
        }

        Button btn = findViewById(R.id.btnSave);
        btn.setOnClickListener(v -> {
            if (inName.getText().toString().trim().isEmpty()) { inName.setError("Requerido"); return; }
            if (inAmount.getText().toString().trim().isEmpty()) { inAmount.setError("Requerido"); return; }
            if (selectedMillis == 0) { inDate.setError("Requerido"); return; }

            ServiceReminder r = (json != null) ? gson.fromJson(json, ServiceReminder.class) : new ServiceReminder();
            r.name = inName.getText().toString().trim();
            try { r.amount = Double.parseDouble(inAmount.getText().toString().trim()); } catch(Exception e){ r.amount = 0; }
            r.dueDateMillis = selectedMillis;
            r.periodicity = (ServiceReminder.Periodicity) spPeriod.getSelectedItem();
            r.importance = (ServiceReminder.Importance) spImportance.getSelectedItem();
            r.paid = false;

            getIntent().putExtra(EXTRA_RESULT, gson.toJson(r));
            setResult(RESULT_OK, getIntent());
            finish();
        });
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> {
            Calendar sel = Calendar.getInstance();
            sel.set(y, m, d, 9, 0, 0);
            selectedMillis = sel.getTimeInMillis();
            inDate.setText(android.text.format.DateFormat.getDateFormat(this).format(sel.getTime()));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}