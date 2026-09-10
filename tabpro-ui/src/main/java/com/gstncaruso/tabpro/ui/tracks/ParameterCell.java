package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Channel;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * Una celda de la mesa de mezcla para un {@link MixParameter}: un numero plano editable, como
 * chorus, reverb, phaser o tremolo en Guitar Pro.
 */
public final class ParameterCell extends JPanel {

    private final Editor editor;
    private final MixParameter parameter;
    private final int trackIndex;
    private final JSpinner numberField = new JSpinner(new SpinnerNumberModel(0, 0, Channel.MAX, 1));
    private boolean syncing;

    public ParameterCell(Editor editor, MixParameter parameter, int trackIndex) {
        this.editor = editor;
        this.parameter = parameter;
        this.trackIndex = trackIndex;
        setOpaque(false);
        setLayout(new BorderLayout());

        numberField.setFocusable(true);
        numberField.addChangeListener(e -> pushIfNotSyncing((Integer) numberField.getValue()));

        add(numberField);
        refresh();
    }

    public void refresh() {
        syncing = true;
        int value = parameter.valueOf(editor.score().track(trackIndex));
        numberField.setValue(value);
        refreshAccessibleName();
        syncing = false;
    }

    private void refreshAccessibleName() {
        String name = parameter.label() + " de " + editor.score().track(trackIndex).name();
        numberField.getAccessibleContext().setAccessibleName(name);
        numberField.setToolTipText(name);
    }

    public int currentValue() {
        return (Integer) numberField.getValue();
    }

    JSpinner numberField() {
        return numberField;
    }

    private void pushIfNotSyncing(int value) {
        if (!syncing) {
            parameter.applyTo(editor, trackIndex, value);
        }
    }
}
