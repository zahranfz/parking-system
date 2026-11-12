package com.parkingsystem.ui;

import com.parkingsystem.model.Transaction;
import com.parkingsystem.service.ParkingService;
import java.awt.BorderLayout;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Panel tab Riwayat untuk menampilkan transaksi yang dibaca dari file CSV.
 */
public class HistoryPanel extends JPanel {

    private final ParkingService parkingService;
    private final DefaultTableModel tableModel;

    public HistoryPanel(ParkingService parkingService) {
        this.parkingService = parkingService;

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Riwayat Transaksi (CSV)");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        JButton refreshButton = new JButton("Muat Ulang");
        refreshButton.addActionListener(e -> refresh());
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(refreshButton, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[] {"Plat Nomor", "Jenis Kendaraan", "Waktu Masuk", "Waktu Keluar", "Durasi (Jam)", "Biaya"},
                0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable historyTable = new JTable(tableModel);
        historyTable.setRowHeight(22);
        add(new JScrollPane(historyTable), BorderLayout.CENTER);

        refresh();
    }

    public final void refresh() {
        tableModel.setRowCount(0);
        List<Transaction> transactions = parkingService.getTransactionHistory();
        NumberFormat currency = NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID"));
        for (Transaction tx : transactions) {
            tableModel.addRow(new Object[] {
                tx.getLicensePlate(),
                tx.getVehicleType(),
                tx.getEntryTime().format(Transaction.DATE_TIME_FORMATTER),
                tx.getExitTime().format(Transaction.DATE_TIME_FORMATTER),
                tx.getDurationHours(),
                "Rp" + currency.format(tx.getFee())
            });
        }
    }
}

