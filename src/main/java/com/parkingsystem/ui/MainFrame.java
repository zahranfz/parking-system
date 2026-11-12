package com.parkingsystem.ui;

import com.parkingsystem.service.ParkingService;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

/**
 * Window utama aplikasi sistem parkir dengan 4 tab utama.
 */
public class MainFrame extends JFrame {

    private final SlotStatusPanel slotStatusPanel;
    private final HistoryPanel historyPanel;

    public MainFrame(ParkingService parkingService) {
        setTitle("Aplikasi Sistem Parkir");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(720, 520));
        setLayout(new BorderLayout());

        slotStatusPanel = new SlotStatusPanel(parkingService);
        historyPanel = new HistoryPanel(parkingService);

        CheckInPanel checkInPanel = new CheckInPanel(parkingService, slotStatusPanel::refresh);
        CheckOutPanel checkOutPanel = new CheckOutPanel(parkingService, () -> {
            slotStatusPanel.refresh();
            historyPanel.refresh();
        });

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Check-In", checkInPanel);
        tabbedPane.addTab("Check-Out", checkOutPanel);
        tabbedPane.addTab("Status Slot", slotStatusPanel);
        tabbedPane.addTab("Riwayat", historyPanel);

        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 2) {
                slotStatusPanel.refresh();
            } else if (selectedIndex == 3) {
                historyPanel.refresh();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }
}

