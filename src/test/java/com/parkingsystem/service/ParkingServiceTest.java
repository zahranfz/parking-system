package com.parkingsystem.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.parkingsystem.model.Car;
import com.parkingsystem.model.Motorcycle;
import com.parkingsystem.model.ParkingSlot;
import com.parkingsystem.model.Transaction;
import com.parkingsystem.model.Truck;
import com.parkingsystem.repository.CsvTransactionRepository;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ParkingServiceTest {

    private static final class MutableClock extends Clock {
        private Instant instant;
        private final ZoneId zone;

        private MutableClock(Instant initialInstant, ZoneId zone) {
            this.instant = initialInstant;
            this.zone = zone;
        }

        private void advance(Duration duration) {
            this.instant = this.instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }

    @TempDir
    Path tempDir;

    @Test
    void defaultQuotasMatchBusinessRuleBr02() {
        ParkingLot lot = new ParkingLot();
        assertEquals(10, lot.getTotalCapacity(Motorcycle.TYPE));
        assertEquals(5, lot.getTotalCapacity(Car.TYPE));
        assertEquals(2, lot.getTotalCapacity(Truck.TYPE));
    }

    @Test
    void checkInAndCheckOutLessThanOneHourChargesMinimumOneHour() {
        MutableClock clock = new MutableClock(Instant.parse("2026-11-12T08:00:00Z"), ZoneId.of("UTC"));
        CsvTransactionRepository repo = new CsvTransactionRepository(tempDir.resolve("transactions.csv"));
        ParkingService service = new ParkingService(new ParkingLot(), repo, clock);

        ParkingSlot slot = service.checkIn("b 1234 abc", Motorcycle.TYPE);
        assertEquals("MTR-01", slot.getSlotId());
        assertEquals(9, service.getParkingLot().getAvailableCount(Motorcycle.TYPE));

        clock.advance(Duration.ofMinutes(25));
        Transaction tx = service.checkOut("B 1234 ABC");

        assertEquals("B 1234 ABC", tx.getLicensePlate());
        assertEquals(Motorcycle.TYPE, tx.getVehicleType());
        assertEquals(1L, tx.getDurationHours());
        assertEquals(2_000L, tx.getFee());
        assertEquals(10, service.getParkingLot().getAvailableCount(Motorcycle.TYPE));
        assertFalse(slot.isOccupied());

        List<Transaction> saved = service.getTransactionHistory();
        assertEquals(1, saved.size());
        assertEquals(2_000L, saved.get(0).getFee());
    }

    @Test
    void checkOutRoundsUpPartialHoursPerBr04() {
        MutableClock clock = new MutableClock(Instant.parse("2026-11-12T08:00:00Z"), ZoneId.of("UTC"));
        CsvTransactionRepository repo = new CsvTransactionRepository(tempDir.resolve("transactions.csv"));
        ParkingService service = new ParkingService(new ParkingLot(), repo, clock);

        service.checkIn("D 9999 XYZ", Car.TYPE);
        clock.advance(Duration.ofHours(1).plusMinutes(5));

        Transaction tx = service.checkOut("D 9999 XYZ");
        assertEquals(2L, tx.getDurationHours());
        assertEquals(10_000L, tx.getFee());
    }

    @Test
    void exactHourDurationDoesNotRoundUp() {
        MutableClock clock = new MutableClock(Instant.parse("2026-11-12T08:00:00Z"), ZoneId.of("UTC"));
        CsvTransactionRepository repo = new CsvTransactionRepository(tempDir.resolve("transactions.csv"));
        ParkingService service = new ParkingService(new ParkingLot(), repo, clock);

        service.checkIn("B 8888 TRK", Truck.TYPE);
        clock.advance(Duration.ofHours(2));

        Transaction tx = service.checkOut("B 8888 TRK");
        assertEquals(2L, tx.getDurationHours());
        assertEquals(20_000L, tx.getFee());
    }

    @Test
    void checkInRejectedWhenSlotFullPerBr06() {
        MutableClock clock = new MutableClock(Instant.parse("2026-11-12T08:00:00Z"), ZoneId.of("UTC"));
        CsvTransactionRepository repo = new CsvTransactionRepository(tempDir.resolve("transactions.csv"));
        ParkingService service = new ParkingService(new ParkingLot(1, 1, 1), repo, clock);

        service.checkIn("B 1111 AA", Truck.TYPE);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.checkIn("B 2222 BB", Truck.TYPE));
        assertTrue(ex.getMessage().toLowerCase().contains("slot penuh"));
    }

    @Test
    void duplicateCheckInRejectedWhileParkedPerBr07() {
        MutableClock clock = new MutableClock(Instant.parse("2026-11-12T08:00:00Z"), ZoneId.of("UTC"));
        CsvTransactionRepository repo = new CsvTransactionRepository(tempDir.resolve("transactions.csv"));
        ParkingService service = new ParkingService(new ParkingLot(), repo, clock);

        service.checkIn("B 1010 AA", Car.TYPE);

        assertThrows(IllegalStateException.class, () -> service.checkIn("b 1010 aa", Car.TYPE));

        service.checkOut("B 1010 AA");
        ParkingSlot reEntered = service.checkIn("B 1010 AA", Car.TYPE);
        assertTrue(reEntered.isOccupied());
    }

    @Test
    void checkOutUnknownVehicleRejectedWithoutSideEffectsPerBr08() {
        MutableClock clock = new MutableClock(Instant.parse("2026-11-12T08:00:00Z"), ZoneId.of("UTC"));
        CsvTransactionRepository repo = new CsvTransactionRepository(tempDir.resolve("transactions.csv"));
        ParkingService service = new ParkingService(new ParkingLot(), repo, clock);

        service.checkIn("B 1234 XX", Motorcycle.TYPE);
        int occupiedBefore = service.getParkingLot().getOccupiedCount(Motorcycle.TYPE);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.checkOut("Z 9999 ZZ"));
        assertTrue(ex.getMessage().toLowerCase().contains("kendaraan tidak ditemukan"));
        assertEquals(occupiedBefore, service.getParkingLot().getOccupiedCount(Motorcycle.TYPE));
        assertTrue(service.getTransactionHistory().isEmpty());
    }
}
