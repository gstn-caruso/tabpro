package com.gstncaruso.tabpro.ui.dialogs.wizards;

import com.gstncaruso.tabpro.core.editing.wizards.BarDurationCheck;

public final class BarDurationReport {

    private BarDurationReport() {
    }

    public static String describe(BarDurationCheck.Finding finding) {
        String problem = finding.tooShort() ? "le faltan pulsos" : "le sobran pulsos";
        return "Pista " + (finding.trackIndex() + 1) + ", compás " + (finding.measureIndex() + 1) + ": " + problem;
    }
}
