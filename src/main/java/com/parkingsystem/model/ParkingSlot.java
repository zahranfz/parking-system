package com.parkingsystem.model;

import java.util.Objects;

public class ParkingSlot {

    private final String slotId;
    private final String allowedVehicleType;
    private Vehicle parkedVehicle;

    public ParkingSlot(String slotId, String allowedVehicleType) {
        if (slotId == null || slotId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID slot tidak boleh kosong.");
        }
        if (allowedVehicleType == null || allowedVehicleType.trim().isEmpty()) {
            throw new IllegalArgumentException("Jenis kendaraan yang diizinkan tidak boleh kosong.");
        }
        this.slotId = slotId.trim();
        this.allowedVehicleType = allowedVehicleType.trim();
    }

    public String getSlotId() {
        return slotId;
    }

    public String getAllowedVehicleType() {
        return allowedVehicleType;
    }

    public boolean isOccupied() {
        return parkedVehicle != null;
    }

    public Vehicle getParkedVehicle() {
        return parkedVehicle;
    }

    public boolean canPark(Vehicle vehicle) {
        return vehicle != null
                && !isOccupied()
                && allowedVehicleType.equalsIgnoreCase(vehicle.getVehicleType());
    }

    public void assignVehicle(Vehicle vehicle) {
        Objects.requireNonNull(vehicle, "Kendaraan tidak boleh null.");
        if (isOccupied()) {
            throw new IllegalStateException("Slot " + slotId + " sudah terisi.");
        }
        if (!allowedVehicleType.equalsIgnoreCase(vehicle.getVehicleType())) {
            throw new IllegalArgumentException(
                    "Slot " + slotId + " hanya untuk " + allowedVehicleType + ", bukan " + vehicle.getVehicleType() + ".");
        }
        this.parkedVehicle = vehicle;
    }

    public Vehicle releaseVehicle() {
        if (!isOccupied()) {
            throw new IllegalStateException("Slot " + slotId + " sudah kosong.");
        }
        Vehicle released = this.parkedVehicle;
        this.parkedVehicle = null;
        return released;
    }
}

