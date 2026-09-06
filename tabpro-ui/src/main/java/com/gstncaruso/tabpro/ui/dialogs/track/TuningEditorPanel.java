package com.gstncaruso.tabpro.ui.dialogs.track;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.TuningLibrary;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;

/**
 * La cantidad de cuerdas, la afinacion elegida de la biblioteca (por familia de
 * instrumento) y la afinacion personalizada, cuerda por cuerda, con boton para
 * escuchar cada una.
 */
public final class TuningEditorPanel extends JPanel {

    private static final int MAX_STRINGS = 12;

    private final Player player;
    private final int previewProgram;

    private final JSpinner stringCount = new JSpinner(new SpinnerNumberModel(6, 1, MAX_STRINGS, 1));
    private final JComboBox<Tuning> library = new JComboBox<>();
    private final JPanel rows = new JPanel();

    private Tuning tuning;
    private List<Tuning> family;
    private boolean updating;

    public TuningEditorPanel(Tuning initial, int previewProgram, Player player) {
        super(new BorderLayout(0, DialogStyle.GAP_S));
        this.player = player;
        this.previewProgram = previewProgram;
        this.tuning = initial;
        this.family = familyContaining(initial);

        JPanel top = new JPanel(new BorderLayout(DialogStyle.GAP_S, 0));
        top.setOpaque(false);
        top.add(labeled("Cuerdas", stringCount), BorderLayout.WEST);
        top.add(familyButtons(), BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        add(library, BorderLayout.CENTER);
        rows.setLayout(new GridLayout(0, 1, 0, DialogStyle.GAP_XS));
        add(rows, BorderLayout.SOUTH);

        stringCount.setValue(initial.stringCount());
        stringCount.addChangeListener(event -> {
            if (!updating) {
                resize((Integer) stringCount.getValue());
            }
        });
        library.addActionListener(event -> {
            if (!updating && library.getSelectedItem() instanceof Tuning chosen) {
                applyTuning(chosen);
            }
        });

        rebuildLibraryOptions();
        rebuildRows();
    }

    private static JPanel labeled(String text, JSpinner spinner) {
        JPanel panel = new JPanel(new BorderLayout(DialogStyle.GAP_XS, 0));
        panel.setOpaque(false);
        panel.add(new javax.swing.JLabel(text), BorderLayout.WEST);
        panel.add(spinner, BorderLayout.CENTER);
        return panel;
    }

    private JPanel familyButtons() {
        JToggleButton guitars = new JToggleButton("Guitarras");
        JToggleButton basses = new JToggleButton("Bajos");
        JToggleButton others = new JToggleButton("Otros");
        ButtonGroup group = new ButtonGroup();
        group.add(guitars);
        group.add(basses);
        group.add(others);
        selectButtonForCurrentFamily(guitars, basses, others);

        guitars.addActionListener(event -> switchFamily(TuningLibrary.guitars()));
        basses.addActionListener(event -> switchFamily(TuningLibrary.basses()));
        others.addActionListener(event -> switchFamily(TuningLibrary.otherStringInstruments()));

        JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, DialogStyle.GAP_XS, 0));
        panel.setOpaque(false);
        panel.add(guitars);
        panel.add(basses);
        panel.add(others);
        return panel;
    }

    private void selectButtonForCurrentFamily(JToggleButton guitars, JToggleButton basses, JToggleButton others) {
        if (family.equals(TuningLibrary.basses())) {
            basses.setSelected(true);
        } else if (family.equals(TuningLibrary.otherStringInstruments())) {
            others.setSelected(true);
        } else {
            guitars.setSelected(true);
        }
    }

    private void switchFamily(List<Tuning> newFamily) {
        family = newFamily;
        rebuildLibraryOptions();
    }

    private void rebuildLibraryOptions() {
        updating = true;
        library.removeAllItems();
        for (Tuning candidate : family) {
            if (candidate.stringCount() == tuning.stringCount()) {
                library.addItem(candidate);
            }
        }
        updating = false;
    }

    private void applyTuning(Tuning chosen) {
        tuning = chosen;
        setStringCountSilently(chosen.stringCount());
        rebuildRows();
    }

    private void resize(int count) {
        tuning = tuning.withStringCount(count);
        rebuildLibraryOptions();
        rebuildRows();
    }

    private void setStringCountSilently(int count) {
        updating = true;
        stringCount.setValue(count);
        updating = false;
    }

    private void rebuildRows() {
        rows.removeAll();
        for (int string = 1; string <= tuning.stringCount(); string++) {
            int fixedString = string;
            rows.add(new TuningRow(
                    tuning.pitchOfString(string),
                    () -> listen(fixedString),
                    pitch -> tuning = tuning.withStringPitch(fixedString, pitch)));
        }
        revalidate();
        repaint();
    }

    /** Hace sonar la cuerda con el instrumento de la pista, como pide el boton Escuchar. */
    public void listen(int string) {
        player.playNote(tuning.pitchOfString(string), previewProgram);
    }

    private static List<Tuning> familyContaining(Tuning tuning) {
        if (TuningLibrary.basses().stream().anyMatch(candidate -> candidate.strings().equals(tuning.strings()))) {
            return TuningLibrary.basses();
        }
        if (TuningLibrary.otherStringInstruments().stream().anyMatch(candidate -> candidate.strings().equals(tuning.strings()))) {
            return TuningLibrary.otherStringInstruments();
        }
        return TuningLibrary.guitars();
    }

    public Tuning toTuning() {
        return tuning;
    }

    public void setStringCount(int count) {
        stringCount.setValue(count);
    }

    public int stringCount() {
        return tuning.stringCount();
    }

    public void switchToGuitars() {
        switchFamily(TuningLibrary.guitars());
    }

    public void switchToBasses() {
        switchFamily(TuningLibrary.basses());
    }

    public void switchToOtherInstruments() {
        switchFamily(TuningLibrary.otherStringInstruments());
    }

    /** Cambia el traste de una cuerda a mano, como si se editara su spinner. */
    public void setStringPitch(int string, com.gstncaruso.tabpro.core.model.Pitch pitch) {
        tuning = tuning.withStringPitch(string, pitch);
        rebuildRows();
    }

    /** Elige una afinacion de la familia actual por nombre, como si se hiciera en el combo. */
    public void selectFromLibrary(String tuningName) {
        for (int index = 0; index < library.getItemCount(); index++) {
            if (library.getItemAt(index).name().equals(tuningName)) {
                library.setSelectedIndex(index);
                return;
            }
        }
        throw new IllegalArgumentException("no esta en la familia actual: " + tuningName);
    }
}
