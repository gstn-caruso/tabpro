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
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.GridLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public final class TrackPropertiesPanel extends JPanel {

    private final JTextField name = new JTextField();
    private final ColorSwatchButton color;
    private final TuningEditorPanel tuningEditor;
    private final JSpinner fretCount = new JSpinner(new SpinnerNumberModel(TrackSettings.DEFAULT_FRET_COUNT, 1, Tuning.MAX_FRET, 1));
    private final JSpinner capo = new JSpinner(new SpinnerNumberModel(0, 0, Tuning.MAX_FRET, 1));
    private final JCheckBox twelveString = new JCheckBox(Texts.get("score_dialogs.TrackPropertiesPanel.twelveString"));
    private final JCheckBox banjoFifthString =
            new JCheckBox(Texts.get("score_dialogs.TrackPropertiesPanel.banjoFifthString"));

    private final JCheckBox standardNotation =
            new JCheckBox(Texts.get("score_dialogs.TrackPropertiesPanel.standardNotation"));
    private final JCheckBox tablature = new JCheckBox(Texts.get("score_dialogs.TrackPropertiesPanel.tablature"));
    private final JCheckBox tuningLegend = new JCheckBox(Texts.get("score_dialogs.shared.tuning"));
    private final JCheckBox rhythmOnTablature =
            new JCheckBox(Texts.get("score_dialogs.TrackPropertiesPanel.rhythmOnTablature"));
    private final JCheckBox diagramsOnTheScore =
            new JCheckBox(Texts.get("score_dialogs.TrackPropertiesPanel.diagramsOnTheScore"));
    private final JCheckBox diagramsUnderTheTitle =
            new JCheckBox(Texts.get("score_dialogs.TrackPropertiesPanel.diagramsUnderTheTitle"));
    private final JCheckBox diagramsBelowStandardNotation =
            new JCheckBox(Texts.get("score_dialogs.TrackPropertiesPanel.diagramsBelowStandardNotation"));
    private final JCheckBox forceHorizontalBeams =
            new JCheckBox(Texts.get("score_dialogs.TrackPropertiesPanel.forceHorizontalBeams"));
    private final JCheckBox forceChannels11to16 =
            new JCheckBox(Texts.get("score_dialogs.TrackPropertiesPanel.forceChannels11to16"));

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
        diagramsOnTheScore.setSelected(display.diagrams().showsOnTheScore());
        diagramsUnderTheTitle.setSelected(display.diagrams().showsUnderTheTitle());
        diagramsBelowStandardNotation.setSelected(display.diagramsBelowStandardNotation());
        forceHorizontalBeams.setSelected(display.forceHorizontalBeams());
        forceChannels11to16.setSelected(track.settings().forceChannels11to16());
        keepAtLeastOneStaffVisible();

        setLayout(new GridLayout(1, 2, DialogStyle.GAP_M, 0));
        add(leftColumn());
        add(rightColumn());
    }

    private FormPanel leftColumn() {
        FormPanel column = new FormPanel();
        column.addRow(Texts.get("score_dialogs.shared.name"), name);
        column.addRow(Texts.get("score_dialogs.TrackPropertiesPanel.color"), color);
        column.addSection(Texts.get("score_dialogs.shared.tuning"));
        column.addFullWidthRow(tuningEditor);
        column.addSection(Texts.get("score_dialogs.TrackPropertiesPanel.fretboard"));
        column.addRow(Texts.get("score_dialogs.TrackPropertiesPanel.frets"), fretCount);
        column.addRow(Texts.get("score_dialogs.TrackPropertiesPanel.capo"), capo);
        column.addFullWidthRow(twelveString);
        column.addFullWidthRow(banjoFifthString);
        return column;
    }

    private FormPanel rightColumn() {
        FormPanel column = new FormPanel();
        column.addSection(Texts.get("score_dialogs.TrackPropertiesPanel.notation"));
        column.addFullWidthRow(standardNotation);
        column.addFullWidthRow(tablature);
        column.addSection(Texts.get("score_dialogs.TrackPropertiesPanel.style"));
        column.addFullWidthRow(tuningLegend);
        column.addFullWidthRow(rhythmOnTablature);
        column.addFullWidthRow(diagramsOnTheScore);
        column.addFullWidthRow(diagramsUnderTheTitle);
        column.addFullWidthRow(diagramsBelowStandardNotation);
        column.addFullWidthRow(forceHorizontalBeams);
        column.addSection(Texts.get("score_dialogs.TrackPropertiesPanel.channels"));
        column.addFullWidthRow(forceChannels11to16);
        return column;
    }

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
                DiagramPlacement.of(diagramsOnTheScore.isSelected(), diagramsUnderTheTitle.isSelected()),
                diagramsBelowStandardNotation.isSelected(),
                forceHorizontalBeams.isSelected());
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
