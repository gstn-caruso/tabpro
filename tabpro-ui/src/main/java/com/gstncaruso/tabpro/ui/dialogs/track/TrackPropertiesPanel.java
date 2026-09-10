package com.gstncaruso.tabpro.ui.dialogs.track;

import com.gstncaruso.tabpro.core.model.DiagramPlacement;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.TrackDisplay;
import com.gstncaruso.tabpro.core.model.TrackSettings;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.ui.dialogs.style.ColorSwatchButton;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import java.awt.GridLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * Todo lo que define una pista: nombre, color, afinacion, trastes, cejilla y que
 * partes de la partitura dibuja. Se puede leer sin mostrarse. Dos columnas anchas,
 * como "Properties of the track" del manual: afinacion y diapason a la izquierda,
 * notacion, estilo y canales a la derecha.
 */
public final class TrackPropertiesPanel extends JPanel {

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
    private final JCheckBox forceChannels11to16 = new JCheckBox("Forzar canales 11 a 16");

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
        forceChannels11to16.setSelected(track.settings().forceChannels11to16());
        diagramPlacement.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean hasFocus) {
                Object label = value instanceof DiagramPlacement placement ? placement.label() : value;
                return super.getListCellRendererComponent(list, label, index, isSelected, hasFocus);
            }
        });
        keepAtLeastOneStaffVisible();

        setLayout(new GridLayout(1, 2, DialogStyle.GAP_M, 0));
        add(leftColumn());
        add(rightColumn());
    }

    private FormPanel leftColumn() {
        FormPanel column = new FormPanel();
        column.addRow("Nombre", name);
        column.addRow("Color", color);
        column.addSection("Afinacion");
        column.addFullWidthRow(tuningEditor);
        column.addSection("Diapason");
        column.addRow("Trastes", fretCount);
        column.addRow("Cejilla", capo);
        column.addFullWidthRow(twelveString);
        column.addFullWidthRow(banjoFifthString);
        return column;
    }

    private FormPanel rightColumn() {
        FormPanel column = new FormPanel();
        column.addSection("Notacion");
        column.addFullWidthRow(standardNotation);
        column.addFullWidthRow(tablature);
        column.addSection("Estilo");
        column.addFullWidthRow(tuningLegend);
        column.addFullWidthRow(rhythmOnTablature);
        column.addRow("Diagramas de acordes", diagramPlacement);
        column.addSection("Canales");
        column.addFullWidthRow(forceChannels11to16);
        return column;
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
                (DiagramPlacement) diagramPlacement.getSelectedItem(),
                false);
        return new TrackSettings(
                color.toScoreColor(),
                (Integer) capo.getValue(),
                (Integer) fretCount.getValue(),
                initialPercussion,
                twelveString.isSelected(),
                banjoFifthString.isSelected(),
                display,
                forceChannels11to16.isSelected());
    }
}
