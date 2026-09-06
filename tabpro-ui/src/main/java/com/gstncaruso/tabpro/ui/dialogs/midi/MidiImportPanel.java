package com.gstncaruso.tabpro.ui.dialogs.midi;

import com.gstncaruso.tabpro.core.files.MidiTrackInfo;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * Lo que las dos ventanas de import de MIDI del manual comparten: la lista de pistas del
 * archivo (con seleccion multiple, para poder fusionarlas) y si hay que transportar una octava
 * para abajo lo que se importe.
 */
public final class MidiImportPanel extends JPanel {

    private final JList<MidiTrackInfo> trackList = new JList<>();
    private final JCheckBox transpose = new JCheckBox("Transportar una octava para abajo");

    public MidiImportPanel(List<MidiTrackInfo> tracks) {
        super(new BorderLayout(0, DialogStyle.GAP_S));
        DialogStyle.padded(this);
        trackList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        trackList.setCellRenderer(trackLabels());
        showTracks(tracks);

        add(new JScrollPane(trackList), BorderLayout.CENTER);
        add(transpose, BorderLayout.SOUTH);
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
