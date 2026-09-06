package com.gstncaruso.tabpro.ui.dialogs.midi;

import com.gstncaruso.tabpro.core.files.MidiTrackInfo;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * Lo que las dos ventanas de import de MIDI del manual comparten: la lista de pistas del
 * archivo (con seleccion multiple, para poder fusionarlas), si hay que transportar una octava
 * para abajo lo que se importe, si cada pista usa dos canales de MIDI o uno solo -- el manual:
 * "handy if you plan on adding bend or slide effects to the tablature" -- y con que precision
 * se cuantiza la posicion y la duracion de las notas -- el manual: "Guitar Pro allows you to
 * precisely define the way it selects the position as well as the duration of the notes".
 */
public final class MidiImportPanel extends JPanel {

    private static final NoteValue[] PRECISION_CHOICES = {
        NoteValue.QUARTER, NoteValue.EIGHTH, NoteValue.SIXTEENTH, NoteValue.THIRTY_SECOND, NoteValue.SIXTY_FOURTH
    };

    private final JList<MidiTrackInfo> trackList = new JList<>();
    private final JCheckBox transpose = new JCheckBox("Transportar una octava para abajo");
    private final JCheckBox twoChannelsPerTrack = new JCheckBox("Usar 2 canales por pista", true);
    private final JComboBox<String> precisionChoice = new JComboBox<>(precisionLabels());

    public MidiImportPanel(List<MidiTrackInfo> tracks) {
        super(new BorderLayout(0, DialogStyle.GAP_S));
        DialogStyle.padded(this);
        trackList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        trackList.setCellRenderer(trackLabels());
        showTracks(tracks);
        precisionChoice.setSelectedItem(figureName(NoteValue.SIXTEENTH));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, DialogStyle.GAP_S, DialogStyle.GAP_S));
        bottom.add(transpose);
        bottom.add(twoChannelsPerTrack);
        bottom.add(new JLabel("Precisión"));
        bottom.add(precisionChoice);

        add(new JScrollPane(trackList), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    /** Cambia el archivo elegido: "abrir otro archivo" del manual. */
    public void showTracks(List<MidiTrackInfo> tracks) {
        DefaultListModel<MidiTrackInfo> model = new DefaultListModel<>();
        tracks.forEach(model::addElement);
        trackList.setModel(model);
    }

    public JList<MidiTrackInfo> trackList() {
        return trackList;
    }

    /** Los indices (dentro del archivo MIDI) de las pistas marcadas en la lista, en su orden. */
    public List<Integer> selectedTrackIndices() {
        return trackList.getSelectedValuesList().stream().map(MidiTrackInfo::index).toList();
    }

    public boolean transposeDownOneOctave() {
        return transpose.isSelected();
    }

    /** El manual: dos canales por pista deja agregarle bend o slide sin correr las demas notas. */
    public boolean useTwoChannelsPerTrack() {
        return twoChannelsPerTrack.isSelected();
    }

    /** La precision elegida para cuantizar la posicion y la duracion de las notas al importar. */
    public NoteValue precision() {
        String label = (String) precisionChoice.getSelectedItem();
        for (NoteValue value : PRECISION_CHOICES) {
            if (figureName(value).equals(label)) {
                return value;
            }
        }
        throw new IllegalStateException("precision desconocida: " + label);
    }

    public void choosePrecision(NoteValue value) {
        precisionChoice.setSelectedItem(figureName(value));
    }

    private static String[] precisionLabels() {
        String[] labels = new String[PRECISION_CHOICES.length];
        for (int index = 0; index < PRECISION_CHOICES.length; index++) {
            labels[index] = figureName(PRECISION_CHOICES[index]);
        }
        return labels;
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

    private static DefaultListCellRenderer trackLabels() {
        return new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean selected, boolean focused) {
                super.getListCellRendererComponent(list, value, index, selected, focused);
                if (value instanceof MidiTrackInfo summary) {
                    setText(summary.percussion() ? summary.name() + " (percusión)" : summary.name());
                }
                return this;
            }
        };
    }
}
