package com.gstncaruso.tabpro.ui.instruments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FretboardDisplayModeTest {

    @Test
    void onlyTheBeatMarksJustItsOwnNotesAsPrimary() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(3);
        BeatLocation location = new BeatLocation(editor.currentTrack(), 0, VoicePart.LEAD, 0);

        FretMarks marks = FretboardDisplayMode.ONLY_BEAT.marks(location, 24, Optional.empty());

        assertEquals(set(new FretPosition(1, 3)), marks.primary());
        assertTrue(marks.secondary().isEmpty());
    }

    @Test
    void theMeasureModeAddsTheOtherBeatsAsContext() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(3);
        editor.moveRight();
        editor.setFret(5);
        editor.moveLeft();
        BeatLocation location = new BeatLocation(editor.currentTrack(), 0, VoicePart.LEAD, 0);

        FretMarks marks = FretboardDisplayMode.BEAT_AND_MEASURE.marks(location, 24, Optional.empty());

        assertEquals(set(new FretPosition(1, 3)), marks.primary());
        assertEquals(set(new FretPosition(1, 5)), marks.secondary());
    }

    @Test
    void theChordModeMarksOnlyThePlayedStringsOfTheDiagram() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(0);
        editor.setChord(new ChordDiagram("Do", 1,
                List.of(0, 1, 0, 2, 3, ChordDiagram.MUTED), List.of(), true));
        BeatLocation location = new BeatLocation(editor.currentTrack(), 0, VoicePart.LEAD, 0);

        FretMarks marks = FretboardDisplayMode.BEAT_AND_CHORD.marks(location, 24, Optional.empty());

        // el traste 1,0 ya esta en el beat: queda como primario, no se duplica en el contexto.
        assertEquals(set(new FretPosition(1, 0)), marks.primary());
        assertEquals(
                set(new FretPosition(2, 1), new FretPosition(3, 0),
                        new FretPosition(4, 2), new FretPosition(5, 3)),
                marks.secondary());
    }

    @Test
    void theChordModeWithoutAChordAddsNoContext() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(3);
        BeatLocation location = new BeatLocation(editor.currentTrack(), 0, VoicePart.LEAD, 0);

        FretMarks marks = FretboardDisplayMode.BEAT_AND_CHORD.marks(location, 24, Optional.empty());

        assertTrue(marks.secondary().isEmpty());
    }

    @Test
    void theNextBeatModeAddsWhatComesRightAfter() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(3);
        editor.moveRight();
        editor.setFret(7);
        editor.moveLeft();
        BeatLocation location = new BeatLocation(editor.currentTrack(), 0, VoicePart.LEAD, 0);

        FretMarks marks = FretboardDisplayMode.BEAT_AND_NEXT_BEAT.marks(location, 24, Optional.empty());

        assertEquals(set(new FretPosition(1, 7)), marks.secondary());
    }

    @Test
    void theNextBeatModeAddsNothingAtTheVeryEnd() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(3);
        BeatLocation location = new BeatLocation(editor.currentTrack(), 0, VoicePart.LEAD, 0);

        FretMarks marks = FretboardDisplayMode.BEAT_AND_NEXT_BEAT.marks(location, 24, Optional.empty());

        assertTrue(marks.secondary().isEmpty());
    }

    @Test
    void theScaleModeMarksEveryFretWhosePitchIsInTheScale() {
        Track track = Track.standardGuitar("g").withTuning(com.gstncaruso.tabpro.core.model.Tuning.of("C", 60));
        Editor editor = new Editor(new Score("t", 120, List.of(track)));
        BeatLocation location = new BeatLocation(editor.currentTrack(), 0, VoicePart.LEAD, 0);

        FretMarks marks =
                FretboardDisplayMode.BEAT_AND_SCALE.marks(location, 4, Optional.of(Scale.cMajor()));

        // cuerda al aire en Do: los trastes de Do mayor son el 0 (Do), el 2 (Re) y el 4 (Mi).
        assertEquals(
                set(new FretPosition(1, 0), new FretPosition(1, 2), new FretPosition(1, 4)),
                marks.secondary());
    }

    @Test
    void theScaleModeAddsNothingWithoutAChosenScale() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(3);
        BeatLocation location = new BeatLocation(editor.currentTrack(), 0, VoicePart.LEAD, 0);

        FretMarks marks = FretboardDisplayMode.BEAT_AND_SCALE.marks(location, 24, Optional.empty());

        assertTrue(marks.secondary().isEmpty());
    }

    private static java.util.Set<FretPosition> set(FretPosition... positions) {
        return java.util.Set.of(positions);
    }
}
