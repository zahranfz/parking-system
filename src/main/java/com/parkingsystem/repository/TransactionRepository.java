package com.parkingsystem.repository;

import com.parkingsystem.model.Transaction;
import java.util.List;

public interface TransactionRepository {

    void save(Transaction transaction);

    List<Transaction> findAll();
}

