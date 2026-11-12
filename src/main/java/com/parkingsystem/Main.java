package com.parkingsystem;

import com.parkingsystem.repository.CsvTransactionRepository;
import com.parkingsystem.repository.TransactionRepository;
import com.parkingsystem.service.ParkingLot;
import com.parkingsystem.service.ParkingService;
import com.parkingsystem.ui.MainFrame;
import javax.swing.SwingUtilities;

/**
 * Entry point aplikasi sistem parkir sederhana.
 */
public class Main {

    public static void main(String[] args) {
        TransactionRepository repository = new CsvTransactionRepository();
        ParkingLot parkingLot = new ParkingLot();
        ParkingService parkingService = new ParkingService(parkingLot, repository);

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(parkingService);
            frame.setVisible(true);
        });
    }
}

