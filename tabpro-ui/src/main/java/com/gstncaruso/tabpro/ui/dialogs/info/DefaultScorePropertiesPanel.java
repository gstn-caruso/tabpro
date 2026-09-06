package com.gstncaruso.tabpro.ui.dialogs.info;

import com.gstncaruso.tabpro.ui.dialogs.measure.KeySignaturePanel;
import com.gstncaruso.tabpro.ui.dialogs.measure.TimeSignaturePanel;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * La solapa "Propiedades por defecto" de Informacion de la partitura: lo que usa la proxima
 * partitura nueva, tal como describe el manual en New Score.
 */
public final class DefaultScorePropertiesPanel extends FormPanel {

    private final JSpinner tempo = new JSpinner(new SpinnerNumberModel(120, 20, 400, 1));
    private final TimeSignaturePanel timeSignaturePanel;
    private final KeySignaturePanel keySignaturePanel;
    private final JTextField title = new JTextField(DialogStyle.TEXT_FIELD_COLUMNS);
    private final JTextField artist = new JTextField(DialogStyle.TEXT_FIELD_COLUMNS);

    public DefaultScorePropertiesPanel(NewScoreDefaults initial) {
        addRow("Tempo", tempo);
        timeSignaturePanel = new TimeSignaturePanel(initial.timeSignature());
        addFullWidthRow(timeSignaturePanel);
        keySignaturePanel = new KeySignaturePanel(initial.keySignature());
        addFullWidthRow(keySignaturePanel);
        addRow("Título", title);
        addRow("Artista", artist);
        apply(initial);
    }

    public void apply(NewScoreDefaults defaults) {
        tempo.setValue(defaults.tempo());
        timeSignaturePanel.apply(defaults.timeSignature());
        keySignaturePanel.apply(defaults.keySignature());
        title.setText(defaults.title());
        artist.setText(defaults.artist());
    }

    public NewScoreDefaults toDefaults() {
        return new NewScoreDefaults(
                (Integer) tempo.getValue(),
                timeSignaturePanel.toTimeSignature(),
                keySignaturePanel.toKeySignature(),
                title.getText(),
                artist.getText());
    }
}
