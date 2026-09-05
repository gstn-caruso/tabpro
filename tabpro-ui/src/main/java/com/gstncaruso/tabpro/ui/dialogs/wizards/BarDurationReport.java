package com.gstncaruso.tabpro.ui.dialogs.wizards;

import com.gstncaruso.tabpro.core.editing.wizards.BarDurationCheck;

/** Como se lee un compas que no cierra, para la lista del asistente. */
public final class BarDurationReport {

    private BarDurationReport() {
    }

    public static String describe(BarDurationCheck.Finding finding) {
        String problem = finding.tooShort() ? "le faltan pulsos" : "le sobran pulsos";
        return "Pista " + (finding.trackIndex() + 1) + ", compas " + (finding.measureIndex() + 1) + ": " + problem;
    }
}
