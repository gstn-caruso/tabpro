package com.gstncaruso.tabpro.ui.dialogs.wizards;

import com.gstncaruso.tabpro.core.editing.wizards.MeasureRange;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/** Desde que compas hasta que compas trabaja un asistente, contando desde 1. */
public final class MeasureRangePanel extends FormPanel {

    private final JSpinner from;
    private final JSpinner to;

    public MeasureRangePanel(int measureCount) {
        this(MeasureRange.wholeScore(measureCount), measureCount);
    }

    public MeasureRangePanel(MeasureRange initial, int measureCount) {
        from = new JSpinner(new SpinnerNumberModel(initial.from(), 1, Math.max(1, measureCount), 1));
        to = new JSpinner(new SpinnerNumberModel(initial.to(), 1, Math.max(1, measureCount), 1));
        addRow("Desde el compas", from);
        addRow("Hasta el compas", to);
    }

    public MeasureRange toMeasureRange() {
        int start = (Integer) from.getValue();
        int end = (Integer) to.getValue();
        return new MeasureRange(Math.min(start, end), Math.max(start, end));
    }
}
