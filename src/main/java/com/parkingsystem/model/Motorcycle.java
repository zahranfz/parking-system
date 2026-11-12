package com.parkingsystem.model;

import java.time.LocalDateTime;

public class Motorcycle extends Vehicle {

    public static final String TYPE = "Motor";
    public static final long RATE_PER_HOUR = 2_000L;

    public Motorcycle(String licensePlate, LocalDateTime entryTime) {
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

