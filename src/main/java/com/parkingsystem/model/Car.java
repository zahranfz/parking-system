package com.parkingsystem.model;

import java.time.LocalDateTime;

/**
 * Representasi kendaraan jenis Mobil.
 */
public class Car extends Vehicle {

    public static final String TYPE = "Mobil";
    public static final long RATE_PER_HOUR = 5_000L;

    public Car(String licensePlate, LocalDateTime entryTime) {
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

