package com.example.lab5_iot_20197115.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab5_iot_20197115.R;
import com.example.lab5_iot_20197115.data.LocalStore;
import com.example.lab5_iot_20197115.data.PaymentRecord;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_history);

        // Toolbar con back
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setTitle("Historial de Pagos");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView rv = findViewById(R.id.recycler);
        TextView tvEmpty = findViewById(R.id.tvEmpty);
        rv.setLayoutManager(new LinearLayoutManager(this));

        List<PaymentRecord> items = new LocalStore(this).loadHistory();

        if (items.isEmpty()) {
            tvEmpty.setVisibility(android.view.View.VISIBLE);
        } else {
            tvEmpty.setVisibility(android.view.View.GONE);
            rv.setAdapter(new RecyclerView.Adapter<VH>() {
                @Override public VH onCreateViewHolder(ViewGroup p, int v) {
                    return new VH(LayoutInflater.from(p.getContext())
                            .inflate(R.layout.item_history, p, false));
                }
                @Override public void onBindViewHolder(VH h, int pos) {
                    PaymentRecord pr = items.get(pos);
                    h.title.setText(pr.serviceName + "  •  S/ " +
                            String.format(Locale.getDefault(),"%.2f", pr.paidAmount));
                    String when = DateFormat.getDateInstance().format(new Date(pr.paidAtMillis));
                    h.subtitle.setText("Pagado el " + when +
                            "  (anticipación: " + pr.daysBefore + " días)");
                }
                @Override public int getItemCount() { return items.size(); }
            });
        }
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, subtitle;
        VH(android.view.View v){ super(v);
            title = v.findViewById(R.id.title);
            subtitle = v.findViewById(R.id.subtitle);
        }
    }
}