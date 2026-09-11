package com.gstncaruso.tabpro.ui.status;

import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.ui.i18n.Texts;

public enum MeasureCompleteness {
    COMPLETE,
    TOO_SHORT,
    TOO_LONG;

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
        return Texts.get("views.MeasureCompleteness." + name());
    }
}
