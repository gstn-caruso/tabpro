package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Channel;
import java.awt.CardLayout;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * Una celda de la mesa de mezcla para un {@link MixParameter}: se ve como potenciometro o como
 * numero, segun lo que diga el {@link MixTableModel} para esa columna.
 */
public final class ParameterCell extends JPanel {

    private static final String KNOB_CARD = "knob";
    private static final String NUMBER_CARD = "number";

    private final Editor editor;
    private final MixTableModel model;
    private final MixParameter parameter;
    private final int trackIndex;
    private final CardLayout cards = new CardLayout();
    private final Potentiometer knob = new Potentiometer(0, Channel.MAX, 0);
    private final JSpinner numberField = new JSpinner(new SpinnerNumberModel(0, 0, Channel.MAX, 1));
    private boolean syncing;

    public ParameterCell(Editor editor, MixTableModel model, MixParameter parameter, int trackIndex) {
        this.editor = editor;
        this.model = model;
        this.parameter = parameter;
        this.trackIndex = trackIndex;
        setOpaque(false);
        setLayout(cards);

        knob.onUserChange(() -> pushIfNotSyncing(knob.getValue()));
        numberField.setFocusable(true);
        numberField.addChangeListener(e -> pushIfNotSyncing((Integer) numberField.getValue()));

        add(knob, KNOB_CARD);
        add(numberField, NUMBER_CARD);
        refresh();
    }

    public void refresh() {
        syncing = true;
        int value = parameter.valueOf(editor.score().track(trackIndex));
        knob.setValue(value);
        numberField.setValue(value);
        cards.show(this, model.displayModeOf(parameter) == DisplayMode.KNOB ? KNOB_CARD : NUMBER_CARD);
        syncing = false;
    }

    public int currentValue() {
        return (Integer) numberField.getValue();
    }

    public boolean isShowingKnob() {
        return knob.isVisible();
    }

    public boolean isShowingNumber() {
        return numberField.isVisible();
    }

    Potentiometer knob() {
        return knob;
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
