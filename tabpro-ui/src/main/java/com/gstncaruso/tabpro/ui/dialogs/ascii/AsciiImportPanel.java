package com.gstncaruso.tabpro.ui.dialogs.ascii;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * La ventana de import de ASCII del manual: la zona de texto donde pegar o corregir la
 * tablatura, el desplegable "importar con" que elige el ritmo por defecto -- una figura fija, o
 * {@code <variable>}, que lo deduce del espaciado entre columnas -- y la segunda lista que fija
 * cuantos intervalos hay entre dos negras cuando el ritmo es {@code <variable>}.
 */
public final class AsciiImportPanel extends JPanel {

    private static final String VARIABLE_LABEL = "<variable>";
    private static final Integer[] INTERVAL_CHOICES = {2, 3, 4, 6, 8, 12, 16};
    private static final int DEFAULT_INTERVALS_PER_QUARTER_NOTE = 4;

    private final JTextArea text = new JTextArea(18, 60);
    private final JComboBox<String> rhythmChoice = new JComboBox<>(rhythmLabels());
    private final JComboBox<Integer> intervalsChoice = new JComboBox<>(INTERVAL_CHOICES);
    private final JButton openButton = DialogStyle.flatButton("Abrir archivo…");
    private final JButton printButton = DialogStyle.flatButton("Imprimir");

    public AsciiImportPanel() {
        super(new BorderLayout(0, DialogStyle.GAP_S));
        DialogStyle.padded(this);
        rhythmChoice.setSelectedItem(figureName(NoteValue.EIGHTH));
        intervalsChoice.setSelectedItem(DEFAULT_INTERVALS_PER_QUARTER_NOTE);
        intervalsChoice.setEnabled(fixedRhythm().isEmpty());
        rhythmChoice.addActionListener(event -> intervalsChoice.setEnabled(fixedRhythm().isEmpty()));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, DialogStyle.GAP_S, DialogStyle.GAP_S));
        toolbar.add(openButton);
        toolbar.add(printButton);
        toolbar.add(new JLabel("Importar con"));
        toolbar.add(rhythmChoice);
        toolbar.add(new JLabel("Intervalos por negra"));
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

    /** Vacio es el {@code <variable>} del manual: el ritmo se deduce del espaciado entre columnas. */
    public Optional<NoteValue> fixedRhythm() {
        String choice = (String) rhythmChoice.getSelectedItem();
        return VARIABLE_LABEL.equals(choice) ? Optional.empty() : Optional.of(noteValueOf(choice));
    }

    public void chooseFixedRhythm(NoteValue value) {
        rhythmChoice.setSelectedItem(figureName(value));
    }

    public void chooseVariableRhythm() {
        rhythmChoice.setSelectedItem(VARIABLE_LABEL);
    }

    /**
     * La "segunda lista" del manual: cuantos intervalos (columnas) hay entre dos negras, para el
     * ritmo {@code <variable>}. Solo importa cuando el ritmo elegido es variable.
     */
    public int intervalsPerQuarterNote() {
        return (Integer) intervalsChoice.getSelectedItem();
    }

    public void chooseIntervalsPerQuarterNote(int value) {
        intervalsChoice.setSelectedItem(value);
    }

    /** Si la segunda lista es relevante con el ritmo elegido ahora mismo. */
    public boolean intervalsPerQuarterNoteEditable() {
        return intervalsChoice.isEnabled();
    }

    private static String[] rhythmLabels() {
        NoteValue[] values = NoteValue.values();
        String[] labels = new String[values.length + 1];
        for (int index = 0; index < values.length; index++) {
            labels[index] = figureName(values[index]);
        }
        labels[values.length] = VARIABLE_LABEL;
        return labels;
    }

    private static NoteValue noteValueOf(String label) {
        for (NoteValue value : NoteValue.values()) {
            if (figureName(value).equals(label)) {
                return value;
            }
        }
        throw new IllegalStateException("figura desconocida: " + label);
    }

    private static String figureName(NoteValue value) {
        return switch (value) {
            case WHOLE -> "Redonda";
            case HALF -> "Blanca";
            case QUARTER -> "Negra";
            case EIGHTH -> "Corchea";
            case SIXTEENTH -> "Semicorchea";
            case THIRTY_SECOND -> "Fusa";
            case SIXTY_FOURTH -> "Semifusa";
        };
    }
}
