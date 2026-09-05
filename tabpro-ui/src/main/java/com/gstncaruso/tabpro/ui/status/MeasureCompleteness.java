package com.gstncaruso.tabpro.ui.status;

import com.gstncaruso.tabpro.core.model.Measure;

/** Si un compas tiene exactamente las notas que pide su medida, o le faltan o le sobran. */
public enum MeasureCompleteness {
    COMPLETE("completo"),
    TOO_SHORT("corto"),
    TOO_LONG("largo");

    private final String label;

    MeasureCompleteness(String label) {
        this.label = label;
    }

    public static MeasureCompleteness of(Measure measure) {
        if (measure.isTooShort()) {
            return TOO_SHORT;
        }
        if (measure.isTooLong()) {
            return TOO_LONG;
        }
        return COMPLETE;
    }

    public String label() {
        return label;
    }
}
