package com.parkingsystem.model;

import java.time.LocalDateTime;

/**
 * Representasi kendaraan jenis Truk.
 */
public class Truck extends Vehicle {

    public static final String TYPE = "Truk";
    public static final long RATE_PER_HOUR = 10_000L;

    public Truck(String licensePlate, LocalDateTime entryTime) {
        super(licensePlate, entryTime);
    }

    @Override
    public long getRatePerHour() {
        return RATE_PER_HOUR;
    }

    @Override
    public String getVehicleType() {
        return TYPE;
    }
}

