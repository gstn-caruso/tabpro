package com.gstncaruso.tabpro.ui.dialogs.wizards;

import com.gstncaruso.tabpro.core.editing.wizards.MeasureRange;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public final class MeasureRangePanel extends FormPanel {

    private final JSpinner from;
    private final JSpinner to;

    public MeasureRangePanel(int measureCount) {
        this(MeasureRange.wholeScore(measureCount), measureCount);
    }

    public MeasureRangePanel(MeasureRange initial, int measureCount) {
        from = new JSpinner(new SpinnerNumberModel(initial.from(), 1, Math.max(1, measureCount), 1));
        to = new JSpinner(new SpinnerNumberModel(initial.to(), 1, Math.max(1, measureCount), 1));
        addRow("Desde el compás", from);
        addRow("Hasta el compás", to);
    }

    public MeasureRange toMeasureRange() {
        int start = (Integer) from.getValue();
        int end = (Integer) to.getValue();
        return new MeasureRange(Math.min(start, end), Math.max(start, end));
    }
}
