package com.gstncaruso.tabpro.ui.dialogs.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.gstncaruso.tabpro.core.files.MidiTrackInfo;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.playback.Timeline;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class MidiImportPanelTest {

    private final RecordingPlayer player = new RecordingPlayer();

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(panel(List.of(track(0, "Guitarra"))));
    }

    @Test
    void theTransposeCheckboxAndPercussionSuffixAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Transpose Down One Octave", english.text("score_dialogs.MidiImportPanel.transpose"));
        assertEquals("Drums (percussion)", english.text("score_dialogs.MidiImportPanel.percussionTrack", "Drums"));
    }

    @Test
    void startsWithNoTracksSelected() {
        MidiImportPanel panel = panel(List.of(track(0, "Guitarra"), track(1, "Bajo")));

        assertEquals(List.of(), panel.selectedTrackIndices());
    }

    @Test
    void reportsTheMidiIndicesOfTheSelectedTracksInListOrder() {
        MidiImportPanel panel = panel(List.of(track(3, "Guitarra"), track(7, "Bajo")));

        panel.trackList().setSelectedIndices(new int[] {0, 1});

        assertEquals(List.of(3, 7), panel.selectedTrackIndices());
    }

    @Test
    void selectingOnlyOneTrackReportsOnlyItsIndex() {
        MidiImportPanel panel = panel(List.of(track(3, "Guitarra"), track(7, "Bajo")));

        panel.trackList().setSelectedIndex(1);

        assertEquals(List.of(7), panel.selectedTrackIndices());
    }

    @Test
    void defaultsToNotTransposingDownAnOctave() {
        MidiImportPanel panel = panel(List.of());

        assertFalse(panel.transposeDownOneOctave());
    }

    @Test
    void defaultsToUsingTwoChannelsPerTrack() {
        MidiImportPanel panel = panel(List.of());

        assertTrue(panel.useTwoChannelsPerTrack());
    }

    @Test
    void defaultsToThirtySecondChordPositionQuantize() {
        MidiImportPanel panel = panel(List.of());

        assertEquals(NoteValue.THIRTY_SECOND, panel.chordPositionQuantize());
    }

    @Test
    void choosingAnotherChordPositionQuantizeChangesIt() {
        MidiImportPanel panel = panel(List.of());

        panel.chooseChordPositionQuantize(NoteValue.SIXTY_FOURTH);

        assertEquals(NoteValue.SIXTY_FOURTH, panel.chordPositionQuantize());
    }

    @Test
    void defaultsToThirtySecondNoteDurationQuantize() {
        MidiImportPanel panel = panel(List.of());

        assertEquals(NoteValue.THIRTY_SECOND, panel.noteDurationQuantize());
    }

    @Test
    void choosingAnotherNoteDurationQuantizeChangesIt() {
        MidiImportPanel panel = panel(List.of());

        panel.chooseNoteDurationQuantize(NoteValue.EIGHTH);

        assertEquals(NoteValue.EIGHTH, panel.noteDurationQuantize());
    }

    @Test
    void selectingAllTracksSelectsEveryTrackInTheList() {
        MidiImportPanel panel = panel(List.of(track(3, "Guitarra"), track(7, "Bajo")));

        panel.selectAllTracks();

        assertEquals(List.of(3, 7), panel.selectedTrackIndices());
    }

    @Test
    void listeningWithNoTrackSelectedDoesNotAskForATimelineNorPlayAnything() {
        MidiImportPanel panel = new MidiImportPanel(
                List.of(track(3, "Guitarra")), player, indices -> fail("should not request the timeline without a selection"));

        panel.listen();

        assertEquals(List.of(), player.played());
    }

    @Test
    void listeningPlaysTheTimelineOfTheSelectedTrackThroughThePlayer() {
        Timeline timeline = new Timeline(120, 480, List.of());
        MidiImportPanel panel = new MidiImportPanel(
                List.of(track(3, "Guitarra"), track(7, "Bajo")), player,
                indices -> indices.equals(List.of(3)) ? timeline : fail("unexpected indices: " + indices));
        panel.trackList().setSelectedIndex(0);

        panel.listen();

        assertEquals(List.of(timeline), player.played());
    }

    @Test
    void listeningWithSeveralTracksSelectedAsksForTheirCombinedTimeline() {
        Timeline timeline = new Timeline(120, 480, List.of());
        MidiImportPanel panel = new MidiImportPanel(
                List.of(track(3, "Guitarra"), track(7, "Bajo")), player,
                indices -> indices.equals(List.of(3, 7)) ? timeline : fail("unexpected indices: " + indices));
        panel.selectAllTracks();

        panel.listen();

        assertEquals(List.of(timeline), player.played());
    }

    @Test
    void stoppingCallsThePlayerStop() {
        MidiImportPanel panel = panel(List.of(track(0, "Guitarra")));

        panel.stopListening();

        assertTrue(player.wasStopped());
    }

    @Test
    void reloadingTheTrackListReplacesItsContent() {
        MidiImportPanel panel = panel(List.of(track(0, "Guitarra")));

        panel.showTracks(List.of(track(5, "Bajo nuevo")));

        assertEquals(1, panel.trackList().getModel().getSize());
        assertEquals(5, panel.trackList().getModel().getElementAt(0).index());
        assertTrue(panel.selectedTrackIndices().isEmpty());
    }

    private MidiImportPanel panel(List<MidiTrackInfo> tracks) {
        return new MidiImportPanel(tracks, player, indices -> fail("this test does not listen to any track"));
    }

    private static MidiTrackInfo track(int index, String name) {
        return new MidiTrackInfo(index, name, false, 25, 1, 4);
    }
}
