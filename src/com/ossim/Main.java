package com.ossim;

import com.ossim.ui.MainWindow;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow win = new MainWindow();
            win.setVisible(true);
        });
    }
}
