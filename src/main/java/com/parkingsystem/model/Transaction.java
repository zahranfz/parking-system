package com.parkingsystem.model;

import com.parkingsystem.common.CsvWritable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Transaction implements CsvWritable {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String licensePlate;
    private final String vehicleType;
    private final LocalDateTime entryTime;
    private final LocalDateTime exitTime;
    private final long durationHours;
    private final long fee;

    public Transaction(
            String licensePlate,
            String vehicleType,
            LocalDateTime entryTime,
            LocalDateTime exitTime,
            long durationHours,
            long fee) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("Plat nomor tidak boleh kosong.");
        }
        if (vehicleType == null || vehicleType.trim().isEmpty()) {
            throw new IllegalArgumentException("Jenis kendaraan tidak boleh kosong.");
        }
        if (durationHours < 1) {
            throw new IllegalArgumentException("Durasi minimal 1 jam.");
        }
        if (fee < 0) {
            throw new IllegalArgumentException("Biaya tidak boleh negatif.");
        }
        this.licensePlate = Vehicle.normalizePlate(licensePlate);
        this.vehicleType = vehicleType.trim();
        this.entryTime = Objects.requireNonNull(entryTime, "Waktu masuk tidak boleh null.");
        this.exitTime = Objects.requireNonNull(exitTime, "Waktu keluar tidak boleh null.");
        this.durationHours = durationHours;
        this.fee = fee;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public long getDurationHours() {
        return durationHours;
    }

    public long getFee() {
        return fee;
    }

    @Override
    public String toCsvRow() {
        return String.join(",",
                escapeCsv(licensePlate),
                escapeCsv(vehicleType),
                escapeCsv(entryTime.format(DATE_TIME_FORMATTER)),
                escapeCsv(exitTime.format(DATE_TIME_FORMATTER)),
                String.valueOf(durationHours),
                String.valueOf(fee));
    }

    private String escapeCsv(String value) {
        String sanitized = value.replace("\r", " ").replace("\n", " ");
        if (sanitized.contains(",") || sanitized.contains("\"")) {
            return "\"" + sanitized.replace("\"", "\"\"") + "\"";
        }
        return sanitized;
    }
}

