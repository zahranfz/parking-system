package com.parkingsystem.ui;

import com.parkingsystem.model.ParkingSlot;
import com.parkingsystem.model.Transaction;
import com.parkingsystem.service.ParkingService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Panel tab Check-In kendaraan masuk.
 */
public class CheckInPanel extends JPanel {

    private final ParkingService parkingService;
    private final Runnable onCheckInSuccess;
    private final JTextField plateField;
    private final JComboBox<String> vehicleTypeCombo;
    private final JTextArea resultArea;

    public CheckInPanel(ParkingService parkingService, Runnable onCheckInSuccess) {
        this.parkingService = parkingService;
        this.onCheckInSuccess = onCheckInSuccess;

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel titleLabel = new JLabel("Check-In Kendaraan");
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

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Jenis Kendaraan:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        List<String> types = parkingService.getParkingLot().getSupportedVehicleTypes();
        vehicleTypeCombo = new JComboBox<>(types.toArray(new String[0]));
        formPanel.add(vehicleTypeCombo, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        JButton submitButton = new JButton("Masuk");
        formPanel.add(submitButton, gbc);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.add(formPanel, BorderLayout.NORTH);

        resultArea = new JTextArea(6, 30);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        resultArea.setBorder(BorderFactory.createTitledBorder("Status Check-In"));

        add(topWrapper, BorderLayout.CENTER);
        add(new JScrollPane(resultArea), BorderLayout.SOUTH);

        submitButton.addActionListener(e -> handleCheckIn());
        plateField.addActionListener(e -> handleCheckIn());
    }

    private void handleCheckIn() {
        String plate = plateField.getText();
        String vehicleType = (String) vehicleTypeCombo.getSelectedItem();
        try {
            ParkingSlot slot = parkingService.checkIn(plate, vehicleType);
            long rate = slot.getParkedVehicle().getRatePerHour();
            NumberFormat currency = NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID"));
            String message = String.format(
                    "Check-In Berhasil!%n"
                            + "Plat Nomor      : %s%n"
                            + "Jenis Kendaraan : %s%n"
                            + "Slot Parkir     : %s%n"
                            + "Waktu Masuk     : %s%n"
                            + "Tarif per Jam   : Rp%s",
                    slot.getParkedVehicle().getLicensePlate(),
                    slot.getParkedVehicle().getVehicleType(),
                    slot.getSlotId(),
                    slot.getParkedVehicle().getEntryTime().format(Transaction.DATE_TIME_FORMATTER),
                    currency.format(rate));

            resultArea.setForeground(new Color(0, 100, 0));
            resultArea.setText(message);
            plateField.setText("");
            plateField.requestFocusInWindow();

            if (onCheckInSuccess != null) {
                onCheckInSuccess.run();
            }
        } catch (RuntimeException ex) {
            resultArea.setForeground(Color.RED);
            resultArea.setText("Check-In Gagal: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Check-In Gagal", JOptionPane.WARNING_MESSAGE);
        }
    }
}

