package com.gstncaruso.tabpro.app;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.gstncaruso.tabpro.ui.MainFrame;
import javax.swing.SwingUtilities;

public class App {

    public String greeting() {
        return "tabpro — editor de tablaturas";
    }

    public static void main(String[] args) {
        FlatDarculaLaf.setup();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
