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
import com.gstncaruso.tabpro.core.model.bars.LineBreak;
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

    /**
     * El manual dice que el salto de linea, a diferencia del resto de los atributos del compas,
     * vale solo para la pista activa -salvo que se este en la vista multipista, donde vale para
     * esa vista compartida por todas las pistas.
     */
    @Test
    void aLineBreakOutsideTheMultitrackViewOnlyReachesTheActiveTrack() {
        editor.setLineBreak(LineBreak.FORCED, false);

        assertEquals(LineBreak.FORCED, editor.score().track(0).measure(0).attributes().lineBreak());
        assertEquals(LineBreak.AUTOMATIC, editor.score().track(1).measure(0).attributes().lineBreak());
    }

    @Test
    void aLineBreakOutsideTheMultitrackViewFollowsTheCursorToWhicheverTrackIsActive() {
        editor.selectTrack(1);

        editor.setLineBreak(LineBreak.FORCED, false);

        assertEquals(LineBreak.AUTOMATIC, editor.score().track(0).measure(0).attributes().lineBreak());
        assertEquals(LineBreak.FORCED, editor.score().track(1).measure(0).attributes().lineBreak());
    }

    @Test
    void aLineBreakInTheMultitrackViewReachesEveryTrack() {
        editor.setLineBreak(LineBreak.FORCED, true);

        assertEquals(LineBreak.FORCED, editor.score().track(0).measure(0).attributes().lineBreak());
        assertEquals(LineBreak.FORCED, editor.score().track(1).measure(0).attributes().lineBreak());
    }

    @Test
    void resettingTheLineBreakOnTheActiveTrackDoesNotTouchTheOthers() {
        editor.setLineBreak(LineBreak.FORCED, true);

        editor.setLineBreak(LineBreak.PREVENTED, false);

        assertEquals(LineBreak.PREVENTED, editor.score().track(0).measure(0).attributes().lineBreak());
        assertEquals(LineBreak.FORCED, editor.score().track(1).measure(0).attributes().lineBreak());
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
    void aKeySignatureChangeReachesTheBarsThatFollow() {
        insertMeasuresUpTo(5);

        editor.moveTo(1, 0, 1);
        editor.setKeySignature(new KeySignature(2, Mode.MAJOR));

        assertEquals(KeySignature.cMajor(), editor.score().attributesOf(0).keySignature());
        assertEquals(new KeySignature(2, Mode.MAJOR), editor.score().attributesOf(1).keySignature());
        assertEquals(new KeySignature(2, Mode.MAJOR), editor.score().attributesOf(2).keySignature());
        assertEquals(new KeySignature(2, Mode.MAJOR), editor.score().attributesOf(3).keySignature());
        assertEquals(new KeySignature(2, Mode.MAJOR), editor.score().attributesOf(4).keySignature());
    }

    @Test
    void aSecondKeySignatureChangeCutsThePreviousOne() {
        insertMeasuresUpTo(5);
        editor.moveTo(1, 0, 1);
        editor.setKeySignature(new KeySignature(2, Mode.MAJOR));

        editor.moveTo(3, 0, 1);
        editor.setKeySignature(new KeySignature(-3, Mode.MINOR));

        assertEquals(KeySignature.cMajor(), editor.score().attributesOf(0).keySignature());
        assertEquals(new KeySignature(2, Mode.MAJOR), editor.score().attributesOf(1).keySignature());
        assertEquals(new KeySignature(2, Mode.MAJOR), editor.score().attributesOf(2).keySignature());
        assertEquals(new KeySignature(-3, Mode.MINOR), editor.score().attributesOf(3).keySignature());
        assertEquals(new KeySignature(-3, Mode.MINOR), editor.score().attributesOf(4).keySignature());
    }

    @Test
    void aTripletFeelChangeReachesTheBarsThatFollow() {
        insertMeasuresUpTo(5);

        editor.moveTo(1, 0, 1);
        editor.setTripletFeel(TripletFeel.EIGHTH);

        assertEquals(TripletFeel.NONE, editor.score().attributesOf(0).tripletFeel());
        assertEquals(TripletFeel.EIGHTH, editor.score().attributesOf(1).tripletFeel());
        assertEquals(TripletFeel.EIGHTH, editor.score().attributesOf(2).tripletFeel());
        assertEquals(TripletFeel.EIGHTH, editor.score().attributesOf(3).tripletFeel());
        assertEquals(TripletFeel.EIGHTH, editor.score().attributesOf(4).tripletFeel());
    }

    @Test
    void aSecondTripletFeelChangeCutsThePreviousOne() {
        insertMeasuresUpTo(5);
        editor.moveTo(1, 0, 1);
        editor.setTripletFeel(TripletFeel.EIGHTH);

        editor.moveTo(3, 0, 1);
        editor.setTripletFeel(TripletFeel.SIXTEENTH);

        assertEquals(TripletFeel.NONE, editor.score().attributesOf(0).tripletFeel());
        assertEquals(TripletFeel.EIGHTH, editor.score().attributesOf(1).tripletFeel());
        assertEquals(TripletFeel.EIGHTH, editor.score().attributesOf(2).tripletFeel());
        assertEquals(TripletFeel.SIXTEENTH, editor.score().attributesOf(3).tripletFeel());
        assertEquals(TripletFeel.SIXTEENTH, editor.score().attributesOf(4).tripletFeel());
    }

    @Test
    void theKeySignaturePropagationReachesEveryTrackNotJustTheActiveOne() {
        insertMeasuresUpTo(5);

        editor.moveTo(1, 0, 1);
        editor.setKeySignature(new KeySignature(2, Mode.MAJOR));

        assertEquals(new KeySignature(2, Mode.MAJOR), editor.score().track(1).measure(2).attributes().keySignature());
        assertEquals(new KeySignature(2, Mode.MAJOR), editor.score().track(1).measure(4).attributes().keySignature());
    }

    @Test
    void anAttributeThatDoesNotPropagateStaysOnlyOnItsBar() {
        insertMeasuresUpTo(5);

        editor.moveTo(1, 0, 1);
        editor.toggleRepeatOpen();

        assertTrue(editor.score().attributesOf(1).repeatOpen());
        assertFalse(editor.score().attributesOf(2).repeatOpen());
        assertFalse(editor.score().attributesOf(0).repeatOpen());
    }

    private void insertMeasuresUpTo(int measureCount) {
        while (editor.currentTrack().measureCount() < measureCount) {
            editor.insertMeasure();
        }
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
