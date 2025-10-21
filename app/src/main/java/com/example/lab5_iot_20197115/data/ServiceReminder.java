package com.example.lab5_iot_20197115.data;

import java.util.UUID;

public class ServiceReminder {
    public String id = UUID.randomUUID().toString();
    public String name;
    public double amount;
    public long dueDateMillis; // fecha de vencimiento (millis)
    public Periodicity periodicity = Periodicity.UNA_VEZ;
    public Importance importance = Importance.MEDIA;
    public boolean paid = false;

    public enum Periodicity { UNA_VEZ, MENSUAL, BIMESTRAL, TRIMESTRAL, ANUAL }
    public enum Importance  { ALTA, MEDIA, BAJA }

    public ServiceReminder() { }

    public ServiceReminder copy() {
        ServiceReminder r = new ServiceReminder();
        r.id = id; r.name = name; r.amount = amount; r.dueDateMillis = dueDateMillis;
        r.periodicity = periodicity; r.importance = importance; r.paid = paid;
        return r;
    }
}