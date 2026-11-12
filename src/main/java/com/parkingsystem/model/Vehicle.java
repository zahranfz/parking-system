package com.parkingsystem.model;

import java.time.LocalDateTime;
import java.util.Objects;


public abstract class Vehicle {

    private final String licensePlate;
    private final LocalDateTime entryTime;

    protected Vehicle(String licensePlate, LocalDateTime entryTime) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("Plat nomor tidak boleh kosong.");
        }
        this.licensePlate = normalizePlate(licensePlate);
        this.entryTime = Objects.requireNonNull(entryTime, "Waktu masuk tidak boleh null.");
    }

    public static String normalizePlate(String licensePlate) {
        return licensePlate == null ? "" : licensePlate.trim().toUpperCase();
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public abstract long getRatePerHour();
    public abstract String getVehicleType();
}
