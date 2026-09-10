package com.gstncaruso.tabpro.ui.dialogs.info;

import com.gstncaruso.tabpro.core.model.LyricLine;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

final class LyricLineRow extends FormPanel {

    private static final int TEXT_ROWS = 12;

    private final JSpinner startingMeasure = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
    private final JTextArea text = new JTextArea(TEXT_ROWS, 0);

    LyricLineRow(LyricLine initial, int lineNumber) {
        setOpaque(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        addRow("Compás inicial de la línea " + lineNumber, startingMeasure);
        addRow("Línea " + lineNumber, new JScrollPane(text));
        apply(initial);
    }

    void apply(LyricLine line) {
        startingMeasure.setValue(line.startingMeasure());
        text.setText(line.text());
    }

    LyricLine toLyricLine() {
        return new LyricLine((Integer) startingMeasure.getValue(), text.getText());
    }

    JTextArea textArea() {
        return text;
    }
}
