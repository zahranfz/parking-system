package com.parkingsystem.repository;

import com.parkingsystem.common.CsvWritable;
import com.parkingsystem.model.Transaction;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CsvTransactionRepository implements TransactionRepository {

    public static final String DEFAULT_FILE_NAME = "transactions.csv";
    private static final String HEADER = "plat_nomor,jenis_kendaraan,waktu_masuk,waktu_keluar,durasi_jam,biaya";
    private static final Logger LOGGER = Logger.getLogger(CsvTransactionRepository.class.getName());

    private final Path filePath;

    public CsvTransactionRepository() {
        this(Path.of(DEFAULT_FILE_NAME));
    }

    public CsvTransactionRepository(Path filePath) {
        this.filePath = Objects.requireNonNull(filePath, "Path file CSV tidak boleh null.");
    }

    public Path getFilePath() {
        return filePath;
    }

    @Override
    public void save(Transaction transaction) {
        Objects.requireNonNull(transaction, "Transaksi tidak boleh null.");
        CsvWritable writable = transaction;
        try {
            ensureFileWithHeader();
            try (BufferedWriter writer = Files.newBufferedWriter(
                    filePath,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {
                writer.write(writable.toCsvRow());
                writer.newLine();
                writer.flush();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Gagal menyimpan transaksi ke file CSV: " + filePath, e);
        }
    }

    @Override
    public List<Transaction> findAll() {
        List<Transaction> transactions = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return transactions;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                if (lineNumber == 1 && trimmed.equalsIgnoreCase(HEADER)) {
                    continue;
                }
                Transaction parsed = parseRow(trimmed, lineNumber);
                if (parsed != null) {
                    transactions.add(parsed);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Gagal membaca file CSV transaksi: " + filePath, e);
        }

        return transactions;
    }

    private void ensureFileWithHeader() throws IOException {
        Path parent = filePath.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        if (!Files.exists(filePath) || Files.size(filePath) == 0) {
            try (BufferedWriter writer = Files.newBufferedWriter(
                    filePath,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                writer.write(HEADER);
                writer.newLine();
                writer.flush();
            }
        }
    }

    private Transaction parseRow(String line, int lineNumber) {
        List<String> fields = splitCsvLine(line);
        if (fields.size() != 6) {
            LOGGER.warning("Baris CSV #" + lineNumber + " dilewati: jumlah kolom tidak valid (" + fields.size() + ").");
            return null;
        }
        try {
            String licensePlate = fields.get(0).trim();
            String vehicleType = fields.get(1).trim();
            LocalDateTime entryTime = parseDateTime(fields.get(2).trim());
            LocalDateTime exitTime = parseDateTime(fields.get(3).trim());
            long durationHours = Long.parseLong(fields.get(4).trim());
            long fee = Long.parseLong(fields.get(5).trim());

            if (licensePlate.isEmpty() || vehicleType.isEmpty() || durationHours < 1 || fee < 0) {
                LOGGER.warning("Baris CSV #" + lineNumber + " dilewati: nilai kolom tidak valid.");
                return null;
            }
            return new Transaction(licensePlate, vehicleType, entryTime, exitTime, durationHours, fee);
        } catch (DateTimeParseException | IllegalArgumentException e) {
            LOGGER.warning("Baris CSV #" + lineNumber + " dilewati karena format rusak: " + e.getMessage());
            return null;
        }
    }

    private LocalDateTime parseDateTime(String raw) {
        try {
            return LocalDateTime.parse(raw, Transaction.DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ignored) {
            return LocalDateTime.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }

    private List<String> splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields;
    }
}
