package com.gstncaruso.tabpro.ui.theme;

import javax.swing.UIManager;

public final class UiSizing {

    private UiSizing() {
    }

    public static int scaled(int nominalSize) {
        return Math.round(nominalSize * scaleFactor());
    }

    public static float scaleFactor() {
        Object scale = UIManager.get("laf.scaleFactor");
        return scale instanceof Number number ? number.floatValue() : 1f;
    }
}
