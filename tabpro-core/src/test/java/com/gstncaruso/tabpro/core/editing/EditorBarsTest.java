package com.gstncaruso.tabpro.core.editing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.model.bars.DirectionJump;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import com.gstncaruso.tabpro.core.model.bars.TripletFeel;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class EditorBarsTest {

    private final Editor editor = new Editor(
            new Score("Prueba", 120, List.of(Track.standardGuitar("Guitarra"), Track.standardBass("Bajo"))));

    @Test
    void theAttributesOfABarAreTheSameOnEveryTrack() {
        editor.toggleRepeatOpen();

        assertTrue(editor.score().track(0).measure(0).attributes().repeatOpen());
        assertTrue(editor.score().track(1).measure(0).attributes().repeatOpen());
    }

    @Test
    void aRepeatCloseSaysHowManyTimes() {
        editor.setRepeatCount(3);

        assertEquals(3, editor.score().attributesOf(0).repeatCount());
        assertTrue(editor.score().attributesOf(0).repeatCloses());
    }

    @Test
    void alternateEndingsSayOnWhichPassTheBarIsPlayed() {
        editor.setAlternateEndings(List.of(1, 3));

        assertTrue(editor.score().attributesOf(0).playedOnPass(1));
        assertFalse(editor.score().attributesOf(0).playedOnPass(2));
        assertTrue(editor.score().attributesOf(0).playedOnPass(3));
    }

    @Test
    void aBarCanCarryAMusicalDirection() {
        editor.setDirectionJump(DirectionJump.DA_SEGNO_AL_CODA);

        assertEquals(Optional.of(DirectionJump.DA_SEGNO_AL_CODA), editor.score().attributesOf(0).jump());
    }

    @Test
    void aBarCanCarryAMarker() {
        editor.setMarker(Marker.named("Estribillo"));

        assertEquals("Estribillo", editor.score().attributesOf(0).marker().orElseThrow().name());
    }

    @Test
    void theKeySignatureAndTheTripletFeelBelongToTheBar() {
        editor.setKeySignature(new KeySignature(-2, Mode.MINOR));
        editor.setTripletFeel(TripletFeel.EIGHTH);

        assertEquals(new KeySignature(-2, Mode.MINOR), editor.score().attributesOf(0).keySignature());
        assertEquals(TripletFeel.EIGHTH, editor.score().attributesOf(0).tripletFeel());
    }

    @Test
    void aTimeSignatureChangeReachesTheBarsThatFollow() {
        editor.insertMeasure();
        editor.insertMeasure();
        editor.moveTo(1, 0, 1);

        editor.setTimeSignature(new TimeSignature(3, 4));

        assertEquals(TimeSignature.fourFour(), editor.score().timeSignatureOf(0));
        assertEquals(new TimeSignature(3, 4), editor.score().timeSignatureOf(1));
        assertEquals(new TimeSignature(3, 4), editor.score().timeSignatureOf(2));
    }

    @Test
    void emptyingABarLeavesItsAttributes() {
        editor.setFret(5);
        editor.toggleRepeatOpen();

        editor.emptyCurrentMeasure(false);

        assertFalse(editor.currentMeasure().hasNotes());
        assertTrue(editor.currentMeasure().attributes().repeatOpen());
    }

    @Test
    void theSecondVoiceStartsEmptyAndReadyToWrite() {
        editor.editVoice(VoicePart.BASS);
        editor.setFret(3);

        assertEquals(VoicePart.BASS, editor.cursor().voice());
        assertTrue(editor.currentMeasure().usesTwoVoices());
        assertFalse(editor.currentMeasure().voice(VoicePart.LEAD).hasNotes());
        assertTrue(editor.currentMeasure().voice(VoicePart.BASS).hasNotes());
    }

    @Test
    void aNewTrackGetsTheAttributesOfTheBarsItJoins() {
        editor.toggleRepeatOpen();

        editor.addTrack(Track.standardGuitar("Otra"));

        assertTrue(editor.score().track(2).measure(0).attributes().repeatOpen());
    }
}
