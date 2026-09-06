package com.gstncaruso.tabpro.ui;

import java.awt.Component;
import javax.swing.JOptionPane;

/**
 * Avisa que una parte del manual todavia no esta hecha, en vez de no hacer nada
 * y dejar al usuario sin saber si el programa lo escucho.
 */
public final class PendingFeature {

    private PendingFeature() {
    }

    public static void announce(Component parent, String what) {
        JOptionPane.showMessageDialog(
                parent,
                what + " todavía no está implementado.",
                "tabpro",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
