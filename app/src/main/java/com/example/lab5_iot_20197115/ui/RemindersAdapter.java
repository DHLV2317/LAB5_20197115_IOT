package com.example.lab5_iot_20197115.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab5_iot_20197115.R;
import com.example.lab5_iot_20197115.data.ServiceReminder;

import java.text.DateFormat;
import java.util.List;

public class RemindersAdapter extends RecyclerView.Adapter<RemindersAdapter.VH> {

    public interface Listener {
        void onPay(ServiceReminder r);
        void onEdit(ServiceReminder r);
        void onDelete(ServiceReminder r);
    }

    private final List<ServiceReminder> items;
    private final Listener li;

    public RemindersAdapter(List<ServiceReminder> items, Listener li) {
        this.items = items; this.li = li;
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int viewType) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_reminder, p, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        ServiceReminder r = items.get(pos);
        h.title.setText(r.name + "  •  S/ " + String.format("%.2f", r.amount));
        String due = DateFormat.getDateInstance().format(r.dueDateMillis);
        h.subtitle.setText("Vence: " + due + "  •  " + r.importance.name());

        h.btnPay.setOnClickListener(v -> li.onPay(r));
        h.btnEdit.setOnClickListener(v -> li.onEdit(r));
        h.btnDelete.setOnClickListener(v -> li.onDelete(r));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, subtitle; Button btnPay, btnEdit, btnDelete;
        VH(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            subtitle = v.findViewById(R.id.subtitle);
            btnPay = v.findViewById(R.id.btnPay);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}