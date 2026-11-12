package com.parkingsystem.repository;

import com.parkingsystem.model.Transaction;
import java.util.List;

/**
 * Kontrak abstraksi penyimpanan riwayat transaksi parkir.
 */
public interface TransactionRepository {

    /**
     * Menyimpan satu transaksi selesai ke media penyimpanan.
     */
    void save(Transaction transaction);

    /**
     * Membaca seluruh transaksi yang tersimpan.
     */
    List<Transaction> findAll();
}

