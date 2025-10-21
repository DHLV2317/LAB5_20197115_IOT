package com.example.lab5_iot_20197115.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LocalStore {
    private static final String PREFS = "lab5_prefs";
    private static final String KEY_LIST = "reminders_json";
    private static final String KEY_HIST = "history_json";

    private final SharedPreferences sp;
    private final Gson gson = new Gson();

    public LocalStore(Context ctx) {
        sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<ServiceReminder> loadReminders() {
        String json = sp.getString(KEY_LIST, "");
        if (json == null || json.isEmpty()) return new ArrayList<>();
        Type t = new TypeToken<ArrayList<ServiceReminder>>(){}.getType();
        return gson.fromJson(json, t);
    }

    public void saveReminders(List<ServiceReminder> list) {
        sp.edit().putString(KEY_LIST, gson.toJson(list)).apply();
    }

    public List<PaymentRecord> loadHistory() {
        String json = sp.getString(KEY_HIST, "");
        if (json == null || json.isEmpty()) return new ArrayList<>();
        Type t = new TypeToken<ArrayList<PaymentRecord>>(){}.getType();
        return gson.fromJson(json, t);
    }

    public void saveHistory(List<PaymentRecord> list) {
        sp.edit().putString(KEY_HIST, gson.toJson(list)).apply();
    }
}