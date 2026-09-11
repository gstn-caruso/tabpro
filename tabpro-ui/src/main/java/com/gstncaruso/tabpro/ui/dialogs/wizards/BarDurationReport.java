package com.gstncaruso.tabpro.ui.dialogs.wizards;

import com.gstncaruso.tabpro.core.editing.wizards.BarDurationCheck;
import com.gstncaruso.tabpro.ui.i18n.Texts;

public final class BarDurationReport {

    private BarDurationReport() {
    }

    public static String describe(BarDurationCheck.Finding finding) {
        String problem = finding.tooShort()
                ? Texts.get("score_dialogs.BarDurationReport.tooShort")
                : Texts.get("score_dialogs.BarDurationReport.tooLong");
        return Texts.get(
                "score_dialogs.BarDurationReport.finding", finding.trackIndex() + 1, finding.measureIndex() + 1, problem);
    }
}
