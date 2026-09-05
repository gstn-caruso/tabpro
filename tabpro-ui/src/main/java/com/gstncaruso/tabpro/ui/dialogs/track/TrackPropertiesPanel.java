package com.gstncaruso.tabpro.ui.dialogs.track;

import com.gstncaruso.tabpro.core.model.DiagramPlacement;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.TrackDisplay;
import com.gstncaruso.tabpro.core.model.TrackSettings;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * Todo lo que define una pista: nombre, color, afinacion, trastes, cejilla y que
 * partes de la partitura dibuja. Se puede leer sin mostrarse.
 */
public final class TrackPropertiesPanel extends FormPanel {

    private final JTextField name = new JTextField();
    private final ColorSwatchButton color;
    private final TuningEditorPanel tuningEditor;
    private final JSpinner fretCount = new JSpinner(new SpinnerNumberModel(TrackSettings.DEFAULT_FRET_COUNT, 1, Tuning.MAX_FRET, 1));
    private final JSpinner capo = new JSpinner(new SpinnerNumberModel(0, 0, Tuning.MAX_FRET, 1));
    private final JCheckBox twelveString = new JCheckBox("Doce cuerdas");
    private final JCheckBox banjoFifthString = new JCheckBox("Banjo de 5ta cuerda");

    private final JCheckBox standardNotation = new JCheckBox("Pentagrama");
    private final JCheckBox tablature = new JCheckBox("Tablatura");
    private final JCheckBox tuningLegend = new JCheckBox("Afinacion");
    private final JCheckBox rhythmOnTablature = new JCheckBox("Ritmo sobre la tablatura");
    private final JComboBox<DiagramPlacement> diagramPlacement = new JComboBox<>(DiagramPlacement.values());

    private final boolean initialPercussion;

    public TrackPropertiesPanel(Track track, Player player) {
        this.initialPercussion = track.settings().percussion();
        this.color = new ColorSwatchButton(track.color());
        this.tuningEditor = new TuningEditorPanel(track.tuning(), track.channel().program(), player);

        name.setText(track.name());
        fretCount.setValue(track.settings().fretCount());
        capo.setValue(track.settings().capo());
        twelveString.setSelected(track.settings().twelveString());
        banjoFifthString.setSelected(track.settings().banjoFifthString());

        TrackDisplay display = track.settings().display();
        standardNotation.setSelected(display.standardNotation());
        tablature.setSelected(display.tablature());
        tuningLegend.setSelected(display.tuningLegend());
        rhythmOnTablature.setSelected(display.rhythmOnTablature());
        diagramPlacement.setSelectedItem(display.diagrams());
        diagramPlacement.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean hasFocus) {
                Object label = value instanceof DiagramPlacement placement ? placement.label() : value;
                return super.getListCellRendererComponent(list, label, index, isSelected, hasFocus);
            }
        });
        keepAtLeastOneStaffVisible();

        addRow("Nombre", name);
        addRow("Color", color);
        addSection("Afinacion");
        addFullWidthRow(tuningEditor);
        addSection("Diapason");
        addRow("Trastes", fretCount);
        addRow("Cejilla", capo);
        addFullWidthRow(twelveString);
        addFullWidthRow(banjoFifthString);
        addSection("Que se dibuja");
        addFullWidthRow(standardNotation);
        addFullWidthRow(tablature);
        addFullWidthRow(tuningLegend);
        addFullWidthRow(rhythmOnTablature);
        addRow("Diagramas de acordes", diagramPlacement);
    }

    /** El pentagrama y la tablatura no pueden estar los dos apagados a la vez. */
    private void keepAtLeastOneStaffVisible() {
        standardNotation.addItemListener(event -> {
            if (!standardNotation.isSelected() && !tablature.isSelected()) {
                tablature.setSelected(true);
            }
        });
        tablature.addItemListener(event -> {
            if (!tablature.isSelected() && !standardNotation.isSelected()) {
                standardNotation.setSelected(true);
            }
        });
    }

    public String trackName() {
        return name.getText();
    }

    public Tuning toTuning() {
        return tuningEditor.toTuning();
    }

    public TrackSettings toTrackSettings() {
        TrackDisplay display = new TrackDisplay(
                standardNotation.isSelected(),
                tablature.isSelected(),
                tuningLegend.isSelected(),
                rhythmOnTablature.isSelected(),
                (DiagramPlacement) diagramPlacement.getSelectedItem());
        return new TrackSettings(
                color.toScoreColor(),
                (Integer) capo.getValue(),
                (Integer) fretCount.getValue(),
                initialPercussion,
                twelveString.isSelected(),
                banjoFifthString.isSelected(),
                display);
    }
}
