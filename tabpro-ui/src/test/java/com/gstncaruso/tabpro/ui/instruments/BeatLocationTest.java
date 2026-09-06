package com.gstncaruso.tabpro.ui.instruments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.VoicePart;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class BeatLocationTest {

    @Test
    void pointsToTheBeatAtItsIndex() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(3);

        BeatLocation location = new BeatLocation(editor.currentTrack(), 0, VoicePart.LEAD, 0);

        assertEquals(editor.currentBeat(), location.beat());
    }

    @Test
    void listsEveryBeatOfItsMeasure() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(3);
        editor.moveRight();
        editor.setFret(5);

        BeatLocation location = new BeatLocation(editor.currentTrack(), 0, VoicePart.LEAD, 0);

        assertEquals(editor.currentTrack().measure(0).beats(), location.measureBeats());
    }

    @Test
    void theNextBeatIsTheOneRightAfterInTheSameMeasure() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(3);
        editor.moveRight();
        editor.setFret(5);

        BeatLocation location = new BeatLocation(editor.currentTrack(), 0, VoicePart.LEAD, 0);

        assertEquals(Optional.of(new Note(1, 5)), location.nextBeat().flatMap(beat -> beat.noteOn(1)));
    }

    @Test
    void theNextBeatCrossesIntoTheFollowingMeasure() {
        Editor editor = new Editor(Score.blank());
        editor.insertMeasure();
        editor.moveToNextMeasure();
        editor.setFret(9);
        editor.moveToPreviousMeasure();

        BeatLocation location = new BeatLocation(editor.currentTrack(), 0, VoicePart.LEAD, 0);

        assertEquals(Optional.of(new Note(1, 9)), location.nextBeat().flatMap(beat -> beat.noteOn(1)));
    }

    @Test
    void thereIsNoNextBeatPastTheLastMeasure() {
        Editor editor = new Editor(Score.blank());

        BeatLocation location = new BeatLocation(editor.currentTrack(), 0, VoicePart.LEAD, 0);

        assertTrue(location.nextBeat().isEmpty());
    }

    @Test
    void followsTheVoiceItWasGiven() {
        Editor editor = new Editor(Score.blank());
        editor.editVoice(VoicePart.BASS);
        editor.setFret(2);

        BeatLocation location = new BeatLocation(editor.currentTrack(), 0, VoicePart.BASS, 0);

        assertEquals(List.of(new Note(1, 2)), location.beat().notes());
    }
}
