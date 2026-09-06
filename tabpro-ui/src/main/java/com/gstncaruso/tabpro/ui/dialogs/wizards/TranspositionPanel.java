package com.gstncaruso.tabpro.ui.dialogs.wizards;

import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/** Cuantos semitonos transponer, y si es sobre la pista activa o todas. */
public final class TranspositionPanel extends FormPanel {

    private final JSpinner semitones = new JSpinner(new SpinnerNumberModel(0, -48, 48, 1));
    private final TrackScopePanel scope = new TrackScopePanel();

    public TranspositionPanel() {
        addRow("Semitonos", semitones);
        addFullWidthRow(scope);
    }

    public int semitones() {
        return (Integer) semitones.getValue();
    }

    public boolean everyTrack() {
        return scope.everyTrackSelected();
    }
}
