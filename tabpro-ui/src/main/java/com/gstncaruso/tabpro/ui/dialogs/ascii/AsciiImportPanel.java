package com.gstncaruso.tabpro.ui.dialogs.ascii;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import com.gstncaruso.tabpro.ui.dialogs.style.Labels;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public final class AsciiImportPanel extends JPanel {

    private static final Integer[] INTERVAL_CHOICES = {2, 3, 4, 6, 8, 12, 16};
    private static final int DEFAULT_INTERVALS_PER_QUARTER_NOTE = 4;

    private final JTextArea text = new JTextArea(18, 60);
    private final JComboBox<String> rhythmChoice = new JComboBox<>(rhythmLabels());
    private final JComboBox<Integer> intervalsChoice = new JComboBox<>(INTERVAL_CHOICES);
    private final JButton openButton = DialogStyle.flatButton(Texts.get("score_dialogs.AsciiImportPanel.openFile"));
    private final JButton printButton = DialogStyle.flatButton(Texts.get("score_dialogs.shared.print"));

    public AsciiImportPanel() {
        super(new BorderLayout(0, DialogStyle.GAP_S));
        DialogStyle.padded(this);
        rhythmChoice.setSelectedItem(Labels.of(NoteValue.EIGHTH));
        intervalsChoice.setSelectedItem(DEFAULT_INTERVALS_PER_QUARTER_NOTE);
        intervalsChoice.setEnabled(fixedRhythm().isEmpty());
        rhythmChoice.addActionListener(event -> intervalsChoice.setEnabled(fixedRhythm().isEmpty()));

        JLabel rhythmLabel = new JLabel(Texts.get("score_dialogs.AsciiImportPanel.importWith"));
        rhythmLabel.setLabelFor(rhythmChoice);
        JLabel intervalsLabel = new JLabel(Texts.get("score_dialogs.AsciiImportPanel.intervalsPerQuarterNote"));
        intervalsLabel.setLabelFor(intervalsChoice);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, DialogStyle.GAP_S, DialogStyle.GAP_S));
        toolbar.add(openButton);
        toolbar.add(printButton);
        toolbar.add(rhythmLabel);
        toolbar.add(rhythmChoice);
        toolbar.add(intervalsLabel);
        toolbar.add(intervalsChoice);

        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(text), BorderLayout.CENTER);
    }

    public String text() {
        return text.getText();
    }

    public void setText(String value) {
        text.setText(value);
    }

    public JButton openButton() {
        return openButton;
    }

    public JButton printButton() {
        return printButton;
    }

    public Optional<NoteValue> fixedRhythm() {
        String choice = (String) rhythmChoice.getSelectedItem();
        return variableRhythmLabel().equals(choice) ? Optional.empty() : Optional.of(noteValueOf(choice));
    }

    public void chooseFixedRhythm(NoteValue value) {
        rhythmChoice.setSelectedItem(Labels.of(value));
    }

    public void chooseVariableRhythm() {
        rhythmChoice.setSelectedItem(variableRhythmLabel());
    }

    public int intervalsPerQuarterNote() {
        return (Integer) intervalsChoice.getSelectedItem();
    }

    public void chooseIntervalsPerQuarterNote(int value) {
        intervalsChoice.setSelectedItem(value);
    }

    public boolean intervalsPerQuarterNoteEditable() {
        return intervalsChoice.isEnabled();
    }

    private static String[] rhythmLabels() {
        NoteValue[] values = NoteValue.values();
        String[] labels = new String[values.length + 1];
        for (int index = 0; index < values.length; index++) {
            labels[index] = Labels.of(values[index]);
        }
        labels[values.length] = variableRhythmLabel();
        return labels;
    }

    private static String variableRhythmLabel() {
        return Texts.get("score_dialogs.AsciiImportPanel.variableRhythm");
    }

    private static NoteValue noteValueOf(String label) {
        for (NoteValue value : NoteValue.values()) {
            if (Labels.of(value).equals(label)) {
                return value;
            }
        }
        throw new IllegalStateException("unknown figure: " + label);
    }
}
