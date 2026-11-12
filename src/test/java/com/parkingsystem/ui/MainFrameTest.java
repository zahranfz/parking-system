package com.parkingsystem.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.parkingsystem.repository.CsvTransactionRepository;
import com.parkingsystem.service.ParkingLot;
import com.parkingsystem.service.ParkingService;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.nio.file.Path;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainFrameTest {

    @TempDir
    Path tempDir;

    @Test
    void mainFrameInitializesAllFourTabs() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Membutuhkan environment GUI non-headless");

        ParkingService service = new ParkingService(
                new ParkingLot(),
                new CsvTransactionRepository(tempDir.resolve("transactions.csv")));

        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame(service);
            try {
                JTabbedPane tabbedPane = findTabbedPane(frame);
                assertEquals(4, tabbedPane.getTabCount());
                assertEquals("Check-In", tabbedPane.getTitleAt(0));
                assertEquals("Check-Out", tabbedPane.getTitleAt(1));
                assertEquals("Status Slot", tabbedPane.getTitleAt(2));
                assertEquals("Riwayat", tabbedPane.getTitleAt(3));
            } finally {
                frame.dispose();
            }
        });
    }

    private JTabbedPane findTabbedPane(MainFrame frame) {
        for (Component c : frame.getContentPane().getComponents()) {
            if (c instanceof JTabbedPane) {
                return (JTabbedPane) c;
            }
        }
        throw new AssertionError("JTabbedPane tidak ditemukan di MainFrame");
    }
}

