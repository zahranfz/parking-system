package com.parkingsystem.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Abstract class dasar untuk seluruh jenis kendaraan yang parkir.
 */
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

    /**
     * Mengembalikan tarif parkir per jam untuk jenis kendaraan ini (dalam Rupiah).
     */
    public abstract long getRatePerHour();

    /**
     * Mengembalikan nama jenis kendaraan (Motor, Mobil, atau Truk).
     */
    public abstract String getVehicleType();
}
