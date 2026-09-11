package com.gstncaruso.tabpro.ui.dialogs.wizards;

import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public final class TranspositionPanel extends FormPanel {

    private final JSpinner semitones = new JSpinner(new SpinnerNumberModel(0, -48, 48, 1));
    private final TrackScopePanel scope = new TrackScopePanel();

    public TranspositionPanel() {
        addRow(Texts.get("score_dialogs.TranspositionPanel.semitones"), semitones);
        addFullWidthRow(scope);
    }

    public int semitones() {
        return (Integer) semitones.getValue();
    }

    public boolean everyTrack() {
        return scope.everyTrackSelected();
    }
}
