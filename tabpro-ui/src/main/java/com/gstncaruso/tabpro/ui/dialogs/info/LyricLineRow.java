package com.gstncaruso.tabpro.ui.dialogs.info;

import com.gstncaruso.tabpro.core.model.LyricLine;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/** Una linea de letra: su compas inicial y el texto con la sintaxis de silabas. */
final class LyricLineRow extends JPanel {

    private final JSpinner startingMeasure = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
    private final JTextField text = new JTextField();

    LyricLineRow(LyricLine initial) {
        super(new BorderLayout(8, 0));
        setOpaque(false);
        add(startingMeasure, BorderLayout.WEST);
        add(text, BorderLayout.CENTER);
        apply(initial);
    }

    void apply(LyricLine line) {
        startingMeasure.setValue(line.startingMeasure());
        text.setText(line.text());
    }

    LyricLine toLyricLine() {
        return new LyricLine((Integer) startingMeasure.getValue(), text.getText());
    }

    JTextField textField() {
        return text;
    }
}
