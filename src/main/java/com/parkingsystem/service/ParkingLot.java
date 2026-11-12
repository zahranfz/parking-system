package com.parkingsystem.service;

import com.parkingsystem.model.Car;
import com.parkingsystem.model.Motorcycle;
import com.parkingsystem.model.ParkingSlot;
import com.parkingsystem.model.Truck;
import com.parkingsystem.model.Vehicle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Mengelola kumpulan ParkingSlot untuk setiap jenis kendaraan.
 */
public class ParkingLot {

    public static final int DEFAULT_MOTOR_SLOTS = 10;
    public static final int DEFAULT_CAR_SLOTS = 5;
    public static final int DEFAULT_TRUCK_SLOTS = 2;

    private final List<ParkingSlot> slots;

    public ParkingLot() {
        this(DEFAULT_MOTOR_SLOTS, DEFAULT_CAR_SLOTS, DEFAULT_TRUCK_SLOTS);
    }

    public ParkingLot(int motorQuota, int carQuota, int truckQuota) {
        if (motorQuota < 0 || carQuota < 0 || truckQuota < 0) {
            throw new IllegalArgumentException("Kuota slot tidak boleh negatif.");
        }
        this.slots = new ArrayList<>();
        initSlots("MTR", Motorcycle.TYPE, motorQuota);
        initSlots("MBL", Car.TYPE, carQuota);
        initSlots("TRK", Truck.TYPE, truckQuota);
    }

    private void initSlots(String prefix, String vehicleType, int count) {
        for (int i = 1; i <= count; i++) {
            String slotId = String.format("%s-%02d", prefix, i);
            slots.add(new ParkingSlot(slotId, vehicleType));
        }
    }

    public ParkingSlot findAvailableSlot(Vehicle vehicle) {
        Objects.requireNonNull(vehicle, "Kendaraan tidak boleh null.");
        for (ParkingSlot slot : slots) {
            if (slot.canPark(vehicle)) {
                return slot;
            }
        }
        return null;
    }

    public ParkingSlot assignSlot(Vehicle vehicle) {
        ParkingSlot available = findAvailableSlot(vehicle);
        if (available == null) {
            throw new IllegalStateException("Slot penuh untuk jenis kendaraan " + vehicle.getVehicleType() + ".");
        }
        available.assignVehicle(vehicle);
        return available;
    }

    public ParkingSlot findOccupiedSlotByPlate(String licensePlate) {
        String normalized = Vehicle.normalizePlate(licensePlate);
        if (normalized.isEmpty()) {
            return null;
        }
        for (ParkingSlot slot : slots) {
            Vehicle parked = slot.getParkedVehicle();
            if (parked != null && parked.getLicensePlate().equals(normalized)) {
                return slot;
            }
        }
        return null;
    }

    public Vehicle releaseSlot(String licensePlate) {
        ParkingSlot occupiedSlot = findOccupiedSlotByPlate(licensePlate);
        if (occupiedSlot == null) {
            throw new IllegalStateException("Kendaraan tidak ditemukan.");
        }
        return occupiedSlot.releaseVehicle();
    }

    public int getTotalCapacity(String vehicleType) {
        int count = 0;
        for (ParkingSlot slot : slots) {
            if (slot.getAllowedVehicleType().equalsIgnoreCase(vehicleType)) {
                count++;
            }
        }
        return count;
    }

    public int getOccupiedCount(String vehicleType) {
        int count = 0;
        for (ParkingSlot slot : slots) {
            if (slot.getAllowedVehicleType().equalsIgnoreCase(vehicleType) && slot.isOccupied()) {
                count++;
            }
        }
        return count;
    }

    public int getAvailableCount(String vehicleType) {
        return getTotalCapacity(vehicleType) - getOccupiedCount(vehicleType);
    }

    public List<ParkingSlot> getSlots() {
        return Collections.unmodifiableList(slots);
    }

    public List<String> getSupportedVehicleTypes() {
        return List.of(Motorcycle.TYPE, Car.TYPE, Truck.TYPE);
    }
}

