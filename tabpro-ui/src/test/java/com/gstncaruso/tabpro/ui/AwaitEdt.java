package com.gstncaruso.tabpro.ui;

import javax.swing.SwingUtilities;

public final class AwaitEdt {

    private AwaitEdt() {}

    public static void flush() {
        try {
            SwingUtilities.invokeAndWait(() -> {});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
