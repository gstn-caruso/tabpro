package com.gstncaruso.tabpro.ui.instruments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.VoicePart;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class KeyboardDisplayModeTest {

    @Test
    void onlyTheBeatMarksJustItsOwnKeysAsPrimary() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(0);
        BeatLocation location = new BeatLocation(editor.currentTrack(), 0, VoicePart.LEAD, 0);

        KeyMarks marks = KeyboardDisplayMode.ONLY_BEAT.marks(location, Optional.empty());

        assertEquals(Set.of(64), marks.primary());
        assertTrue(marks.secondary().isEmpty());
    }

    @Test
    void theMeasureModeAddsTheOtherBeatsAsContext() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(0);
        editor.moveRight();
        editor.setFret(3);
        editor.moveLeft();
        BeatLocation location = new BeatLocation(editor.currentTrack(), 0, VoicePart.LEAD, 0);

        KeyMarks marks = KeyboardDisplayMode.BEAT_AND_MEASURE.marks(location, Optional.empty());

        assertEquals(Set.of(64), marks.primary());
        assertEquals(Set.of(67), marks.secondary());
    }

    @Test
    void theNextBeatModeAddsWhatComesRightAfter() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(0);
        editor.moveRight();
        editor.setFret(5);
        editor.moveLeft();
        BeatLocation location = new BeatLocation(editor.currentTrack(), 0, VoicePart.LEAD, 0);

        KeyMarks marks = KeyboardDisplayMode.BEAT_AND_NEXT_BEAT.marks(location, Optional.empty());

        assertEquals(Set.of(69), marks.secondary());
    }

    @Test
    void theNextBeatModeAddsNothingAtTheVeryEnd() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(0);
        BeatLocation location = new BeatLocation(editor.currentTrack(), 0, VoicePart.LEAD, 0);

        KeyMarks marks = KeyboardDisplayMode.BEAT_AND_NEXT_BEAT.marks(location, Optional.empty());

        assertTrue(marks.secondary().isEmpty());
    }

    @Test
    void theScaleModeMarksEveryKeyInTheChosenScale() {
        Editor editor = new Editor(Score.blank());
        BeatLocation location = new BeatLocation(editor.currentTrack(), 0, VoicePart.LEAD, 0);

        KeyMarks marks = KeyboardDisplayMode.BEAT_AND_SCALE.marks(location, Optional.of(Scale.cMajor()));

        assertTrue(marks.secondary().contains(60), "Do central es de Do mayor");
        assertTrue(marks.secondary().contains(62), "Re central es de Do mayor");
        assertTrue(!marks.secondary().contains(61), "Do sostenido no es de Do mayor");
    }

    @Test
    void theScaleModeAddsNothingWithoutAChosenScale() {
        Editor editor = new Editor(Score.blank());
        BeatLocation location = new BeatLocation(editor.currentTrack(), 0, VoicePart.LEAD, 0);

        KeyMarks marks = KeyboardDisplayMode.BEAT_AND_SCALE.marks(location, Optional.empty());

        assertTrue(marks.secondary().isEmpty());
    }
}
