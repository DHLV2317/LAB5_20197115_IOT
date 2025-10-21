package com.example.lab5_iot_20197115.data;

public class PaymentRecord {
    public String reminderId;
    public String serviceName;
    public double paidAmount;
    public long paidAtMillis; // cuándo pagó
    public int daysBefore;    // anticipación respecto al vencimiento

    public PaymentRecord() { }

    public PaymentRecord(String id, String name, double amount, long paidAt, int daysBefore) {
        this.reminderId = id; this.serviceName = name; this.paidAmount = amount;
        this.paidAtMillis = paidAt; this.daysBefore = daysBefore;
    }
}