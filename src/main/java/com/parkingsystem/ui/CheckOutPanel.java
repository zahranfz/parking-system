package com.parkingsystem.ui;

import com.parkingsystem.model.Transaction;
import com.parkingsystem.service.ParkingService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Panel tab Check-Out kendaraan keluar dan perhitungan biaya parkir.
 */
public class CheckOutPanel extends JPanel {

    private final ParkingService parkingService;
    private final Runnable onCheckOutSuccess;
    private final JTextField plateField;
    private final JTextArea resultArea;

    public CheckOutPanel(ParkingService parkingService, Runnable onCheckOutSuccess) {
        this.parkingService = parkingService;
        this.onCheckOutSuccess = onCheckOutSuccess;

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel titleLabel = new JLabel("Check-Out Kendaraan");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Plat Nomor:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        plateField = new JTextField(20);
        formPanel.add(plateField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        JButton submitButton = new JButton("Keluar");
        formPanel.add(submitButton, gbc);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.add(formPanel, BorderLayout.NORTH);

        resultArea = new JTextArea(7, 30);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        resultArea.setBorder(BorderFactory.createTitledBorder("Rincian Biaya Parkir"));

        add(topWrapper, BorderLayout.CENTER);
        add(new JScrollPane(resultArea), BorderLayout.SOUTH);

        submitButton.addActionListener(e -> handleCheckOut());
        plateField.addActionListener(e -> handleCheckOut());
    }

    private void handleCheckOut() {
        String plate = plateField.getText();
        try {
            Transaction tx = parkingService.checkOut(plate);
            NumberFormat currency = NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID"));
            String message = String.format(
                    "Check-Out Berhasil!%n"
                            + "Plat Nomor      : %s%n"
                            + "Jenis Kendaraan : %s%n"
                            + "Waktu Masuk     : %s%n"
                            + "Waktu Keluar    : %s%n"
                            + "Durasi Parkir   : %d jam%n"
                            + "Total Biaya     : Rp%s",
                    tx.getLicensePlate(),
                    tx.getVehicleType(),
                    tx.getEntryTime().format(Transaction.DATE_TIME_FORMATTER),
                    tx.getExitTime().format(Transaction.DATE_TIME_FORMATTER),
                    tx.getDurationHours(),
                    currency.format(tx.getFee()));

            resultArea.setForeground(new Color(0, 100, 0));
            resultArea.setText(message);
            plateField.setText("");
            plateField.requestFocusInWindow();

            if (onCheckOutSuccess != null) {
                onCheckOutSuccess.run();
            }
        } catch (RuntimeException ex) {
            resultArea.setForeground(Color.RED);
            resultArea.setText("Check-Out Gagal: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Check-Out Gagal", JOptionPane.WARNING_MESSAGE);
        }
    }
}

