package com.parkingsystem.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.parkingsystem.model.Car;
import com.parkingsystem.model.Motorcycle;
import com.parkingsystem.model.Transaction;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvTransactionRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void saveAndFindAllRoundTripsTransactions() {
        Path csvPath = tempDir.resolve("transactions.csv");
        CsvTransactionRepository repo = new CsvTransactionRepository(csvPath);

        Transaction t1 = new Transaction(
                "B 1234 AA",
                Motorcycle.TYPE,
                LocalDateTime.of(2026, 11, 12, 8, 0, 0),
                LocalDateTime.of(2026, 11, 12, 9, 15, 0),
                2L,
                4_000L);
        Transaction t2 = new Transaction(
                "D 5678 BB",
                Car.TYPE,
                LocalDateTime.of(2026, 11, 12, 10, 0, 0),
                LocalDateTime.of(2026, 11, 12, 10, 30, 0),
                1L,
                5_000L);

        repo.save(t1);
        repo.save(t2);

        List<Transaction> loaded = repo.findAll();
        assertEquals(2, loaded.size());
        assertEquals("B 1234 AA", loaded.get(0).getLicensePlate());
        assertEquals(4_000L, loaded.get(0).getFee());
        assertEquals("D 5678 BB", loaded.get(1).getLicensePlate());
        assertEquals(5_000L, loaded.get(1).getFee());
    }

    @Test
    void findAllSkipsCorruptedRowsWithoutThrowing() throws IOException {
        Path csvPath = tempDir.resolve("transactions.csv");
        String content = String.join(System.lineSeparator(),
                "plat_nomor,jenis_kendaraan,waktu_masuk,waktu_keluar,durasi_jam,biaya",
                "baris-rusak-hanya-satu-kolom",
                "B 1111 CC,Motor,tanggal-salah,2026-11-12 09:00:00,1,2000",
                "B 2222 DD,Mobil,2026-11-12 08:00:00,2026-11-12 09:00:00,0,5000",
                "B 3333 EE,Truk,2026-11-12 08:00:00,2026-11-12 09:00:00,1,10000");
        Files.writeString(csvPath, content, StandardCharsets.UTF_8);

        CsvTransactionRepository repo = new CsvTransactionRepository(csvPath);
        List<Transaction> loaded = repo.findAll();

        assertEquals(1, loaded.size());
        assertEquals("B 3333 EE", loaded.get(0).getLicensePlate());
        assertEquals(10_000L, loaded.get(0).getFee());
    }

    @Test
    void findAllReturnsEmptyWhenFileMissing() {
        CsvTransactionRepository repo = new CsvTransactionRepository(tempDir.resolve("missing.csv"));
        assertTrue(repo.findAll().isEmpty());
    }
}

