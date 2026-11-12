package com.parkingsystem.ui;

import com.parkingsystem.model.ParkingSlot;
import com.parkingsystem.model.Transaction;
import com.parkingsystem.model.Vehicle;
import com.parkingsystem.service.ParkingLot;
import com.parkingsystem.service.ParkingService;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Panel tab Status Slot untuk menampilkan sisa kapasitas dan daftar slot secara real-time.
 */
public class SlotStatusPanel extends JPanel {

    private final ParkingService parkingService;
    private final DefaultTableModel summaryTableModel;
    private final DefaultTableModel detailTableModel;

    public SlotStatusPanel(ParkingService parkingService) {
        this.parkingService = parkingService;

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel titleLabel = new JLabel("Status Slot Parkir (Real-Time)");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);

        summaryTableModel = new DefaultTableModel(
                new Object[] {"Jenis Kendaraan", "Tarif / Jam", "Total Slot", "Terisi", "Tersedia"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable summaryTable = new JTable(summaryTableModel);
        summaryTable.setRowHeight(24);
        JScrollPane summaryScroll = new JScrollPane(summaryTable);
        summaryScroll.setPreferredSize(new Dimension(500, 110));
        summaryScroll.setBorder(BorderFactory.createTitledBorder("Ringkasan Sisa Slot per Jenis Kendaraan"));

        detailTableModel = new DefaultTableModel(
                new Object[] {"ID Slot", "Jenis Kendaraan", "Status", "Plat Nomor", "Waktu Masuk"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable detailTable = new JTable(detailTableModel);
        detailTable.setRowHeight(22);
        JScrollPane detailScroll = new JScrollPane(detailTable);
        detailScroll.setBorder(BorderFactory.createTitledBorder("Daftar Detail Slot"));

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.add(summaryScroll, BorderLayout.NORTH);
        contentPanel.add(detailScroll, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        refresh();
    }

    public final void refresh() {
        ParkingLot lot = parkingService.getParkingLot();
        NumberFormat currency = NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID"));

        summaryTableModel.setRowCount(0);
        for (String type : lot.getSupportedVehicleTypes()) {
            long rate = parkingService.getRatePerHour(type);
            int total = lot.getTotalCapacity(type);
            int occupied = lot.getOccupiedCount(type);
            int available = lot.getAvailableCount(type);
            summaryTableModel.addRow(new Object[] {
                type,
                "Rp" + currency.format(rate),
                total,
                occupied,
                available
            });
        }

        detailTableModel.setRowCount(0);
        for (ParkingSlot slot : lot.getSlots()) {
            Vehicle parked = slot.getParkedVehicle();
            detailTableModel.addRow(new Object[] {
                slot.getSlotId(),
                slot.getAllowedVehicleType(),
                slot.isOccupied() ? "Terisi" : "Kosong",
                parked != null ? parked.getLicensePlate() : "-",
                parked != null ? parked.getEntryTime().format(Transaction.DATE_TIME_FORMATTER) : "-"
            });
        }
    }
}

