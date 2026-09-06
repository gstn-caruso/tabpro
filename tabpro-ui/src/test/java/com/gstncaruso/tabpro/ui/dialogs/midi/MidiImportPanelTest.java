package com.gstncaruso.tabpro.ui.dialogs.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.files.MidiTrackInfo;
import java.util.List;
import org.junit.jupiter.api.Test;

class MidiImportPanelTest {

    @Test
    void startsWithNoTracksSelected() {
        MidiImportPanel panel = new MidiImportPanel(List.of(track(0, "Guitarra"), track(1, "Bajo")));

        assertEquals(List.of(), panel.selectedTrackIndices());
    }

    @Test
    void reportsTheMidiIndicesOfTheSelectedTracksInListOrder() {
        MidiImportPanel panel = new MidiImportPanel(List.of(track(3, "Guitarra"), track(7, "Bajo")));

        panel.trackList().setSelectedIndices(new int[] {0, 1});

        assertEquals(List.of(3, 7), panel.selectedTrackIndices());
    }

    @Test
    void selectingOnlyOneTrackReportsOnlyItsIndex() {
        MidiImportPanel panel = new MidiImportPanel(List.of(track(3, "Guitarra"), track(7, "Bajo")));

        panel.trackList().setSelectedIndex(1);

        assertEquals(List.of(7), panel.selectedTrackIndices());
    }

    @Test
    void defaultsToNotTransposingDownAnOctave() {
        MidiImportPanel panel = new MidiImportPanel(List.of());

        assertFalse(panel.transposeDownOneOctave());
    }

    @Test
    void reloadingTheTrackListReplacesItsContent() {
        MidiImportPanel panel = new MidiImportPanel(List.of(track(0, "Guitarra")));

        panel.showTracks(List.of(track(5, "Bajo nuevo")));

        assertEquals(1, panel.trackList().getModel().getSize());
        assertEquals(5, panel.trackList().getModel().getElementAt(0).index());
        assertTrue(panel.selectedTrackIndices().isEmpty());
    }

    private static MidiTrackInfo track(int index, String name) {
        return new MidiTrackInfo(index, name, false, 25, 1, 4);
    }
}
