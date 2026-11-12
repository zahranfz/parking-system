package com.parkingsystem.service;

import com.parkingsystem.model.Car;
import com.parkingsystem.model.Motorcycle;
import com.parkingsystem.model.ParkingSlot;
import com.parkingsystem.model.Transaction;
import com.parkingsystem.model.Truck;
import com.parkingsystem.model.Vehicle;
import com.parkingsystem.repository.TransactionRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Layanan orkestrasi operasi check-in, check-out, status slot, dan riwayat parkir.
 */
public class ParkingService {

    private static final Map<String, BiFunction<String, LocalDateTime, Vehicle>> VEHICLE_CREATORS =
            new LinkedHashMap<>();

    static {
        VEHICLE_CREATORS.put(Motorcycle.TYPE.toLowerCase(), Motorcycle::new);
        VEHICLE_CREATORS.put(Car.TYPE.toLowerCase(), Car::new);
        VEHICLE_CREATORS.put(Truck.TYPE.toLowerCase(), Truck::new);
    }

    private final ParkingLot parkingLot;
    private final TransactionRepository transactionRepository;
    private final Clock clock;

    public ParkingService(TransactionRepository transactionRepository) {
        this(new ParkingLot(), transactionRepository, Clock.systemDefaultZone());
    }

    public ParkingService(ParkingLot parkingLot, TransactionRepository transactionRepository) {
        this(parkingLot, transactionRepository, Clock.systemDefaultZone());
    }

    public ParkingService(
            ParkingLot parkingLot,
            TransactionRepository transactionRepository,
            Clock clock) {
        this.parkingLot = Objects.requireNonNull(parkingLot, "ParkingLot tidak boleh null.");
        this.transactionRepository = Objects.requireNonNull(
                transactionRepository, "TransactionRepository tidak boleh null.");
        this.clock = Objects.requireNonNull(clock, "Clock tidak boleh null.");
    }

    public ParkingSlot checkIn(String licensePlate, String vehicleType) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("Plat nomor tidak boleh kosong.");
        }
        String normalizedPlate = Vehicle.normalizePlate(licensePlate);
        if (parkingLot.findOccupiedSlotByPlate(normalizedPlate) != null) {
            throw new IllegalStateException(
                    "Kendaraan dengan plat " + normalizedPlate + " sudah sedang parkir dan belum check-out.");
        }

        LocalDateTime entryTime = LocalDateTime.now(clock);
        Vehicle vehicle = createVehicle(vehicleType, normalizedPlate, entryTime);
        return parkingLot.assignSlot(vehicle);
    }

    public Transaction checkOut(String licensePlate) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("Plat nomor tidak boleh kosong.");
        }
        String normalizedPlate = Vehicle.normalizePlate(licensePlate);
        ParkingSlot occupiedSlot = parkingLot.findOccupiedSlotByPlate(normalizedPlate);
        if (occupiedSlot == null) {
            throw new IllegalStateException("Kendaraan tidak ditemukan.");
        }

        Vehicle vehicle = occupiedSlot.getParkedVehicle();
        LocalDateTime exitTime = LocalDateTime.now(clock);
        long durationHours = calculateDurationHours(vehicle.getEntryTime(), exitTime);
        long fee = durationHours * vehicle.getRatePerHour();

        Transaction transaction = new Transaction(
                vehicle.getLicensePlate(),
                vehicle.getVehicleType(),
                vehicle.getEntryTime(),
                exitTime,
                durationHours,
                fee);

        transactionRepository.save(transaction);
        parkingLot.releaseSlot(normalizedPlate);
        return transaction;
    }

    public long calculateDurationHours(LocalDateTime entryTime, LocalDateTime exitTime) {
        long seconds = Duration.between(entryTime, exitTime).getSeconds();
        if (seconds <= 0) {
            return 1L;
        }
        return Math.max(1L, (seconds + 3599L) / 3600L);
    }

    public List<Transaction> getTransactionHistory() {
        return transactionRepository.findAll();
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }

    public long getRatePerHour(String vehicleType) {
        Vehicle sample = createVehicle(vehicleType, "SAMPLE", LocalDateTime.now(clock));
        return sample.getRatePerHour();
    }

    private Vehicle createVehicle(String vehicleType, String licensePlate, LocalDateTime entryTime) {
        if (vehicleType == null || vehicleType.trim().isEmpty()) {
            throw new IllegalArgumentException("Jenis kendaraan harus dipilih.");
        }
        BiFunction<String, LocalDateTime, Vehicle> creator =
                VEHICLE_CREATORS.get(vehicleType.trim().toLowerCase());
        if (creator == null) {
            throw new IllegalArgumentException("Jenis kendaraan tidak dikenal: " + vehicleType);
        }
        return creator.apply(licensePlate, entryTime);
    }
}
