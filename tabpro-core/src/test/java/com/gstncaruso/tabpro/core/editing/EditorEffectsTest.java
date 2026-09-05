package com.gstncaruso.tabpro.core.editing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Tuplet;
import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendType;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.Finger;
import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.model.effects.SlideType;
import com.gstncaruso.tabpro.core.model.effects.Stroke;
import com.gstncaruso.tabpro.core.model.effects.StrokeDirection;
import com.gstncaruso.tabpro.core.model.effects.Trill;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class EditorEffectsTest {

    private final Editor editor = new Editor(Score.blank());

    @Test
    void anOrnamentGoesOnTheNoteUnderTheCursor() {
        editor.setFret(5);

        editor.toggleOrnament(Ornament.PALM_MUTE);

        assertTrue(editor.currentNote().orElseThrow().has(Ornament.PALM_MUTE));
    }

    @Test
    void theSameOrnamentTwiceTakesItOff() {
        editor.setFret(5);

        editor.toggleOrnament(Ornament.LET_RING);
        editor.toggleOrnament(Ornament.LET_RING);

        assertFalse(editor.currentNote().orElseThrow().has(Ornament.LET_RING));
    }

    @Test
    void anEmptyBeatIgnoresTheOrnament() {
        editor.toggleOrnament(Ornament.GHOST);

        assertTrue(editor.currentBeat().isRest());
        assertFalse(editor.canUndo());
    }

    @Test
    void aGhostNoteCannotAlsoBeAccented() {
        editor.setFret(5);

        editor.toggleOrnament(Ornament.GHOST);
        editor.toggleOrnament(Ornament.ACCENTED);

        assertTrue(editor.currentNote().orElseThrow().has(Ornament.ACCENTED));
        assertFalse(editor.currentNote().orElseThrow().has(Ornament.GHOST));
    }

    @Test
    void theDynamicOfTheChordReachesEveryNote() {
        editor.setFret(5);
        editor.moveDown();
        editor.setFret(7);

        editor.setChordDynamic(Dynamic.FORTISSIMO);

        assertTrue(editor.currentBeat().notes().stream()
                .allMatch(note -> note.effects().dynamic() == Dynamic.FORTISSIMO));
    }

    @Test
    void theParameterisedEffectsLandOnTheNote() {
        editor.setFret(5);

        editor.setBend(Bend.of(BendType.BEND, 4));
        editor.setSlide(SlideType.LEGATO);
        editor.setHarmonic(HarmonicType.NATURAL);
        editor.setTrill(Trill.to(7));
        editor.setLeftHandFinger(Finger.MIDDLE);

        var effects = editor.currentNote().orElseThrow().effects();
        assertTrue(effects.bend().isPresent());
        assertEquals(Optional.of(SlideType.LEGATO), effects.slide());
        assertEquals(Optional.of(HarmonicType.NATURAL), effects.harmonic());
        assertEquals(Optional.of(Trill.to(7)), effects.trill());
        assertEquals(Optional.of(Finger.MIDDLE), effects.leftHand());
    }

    @Test
    void passingNothingTakesTheEffectOff() {
        editor.setFret(5);
        editor.setSlide(SlideType.LEGATO);

        editor.setSlide(null);

        assertEquals(Optional.empty(), editor.currentNote().orElseThrow().effects().slide());
    }

    @Test
    void theEffectsOfTheBeatDoNotNeedANote() {
        editor.setStroke(Stroke.of(StrokeDirection.DOWN));
        editor.toggleFadeIn();
        editor.setText("Intro");

        assertTrue(editor.currentBeat().effects().stroke().isPresent());
        assertTrue(editor.currentBeat().effects().fadeIn());
        assertEquals(Optional.of("Intro"), editor.currentBeat().effects().text());
    }

    @Test
    void aTiedNoteIsNotPlayedAgain() {
        editor.setFret(5);

        editor.toggleTie();

        assertTrue(editor.currentNote().orElseThrow().tied());
    }

    @Test
    void tyingTheBeatTiesEveryNote() {
        editor.setFret(5);
        editor.moveDown();
        editor.setFret(7);

        editor.tieWholeBeat();

        assertTrue(editor.currentBeat().notes().stream().allMatch(note -> note.tied()));
    }

    @Test
    void aTripletMakesThreeNotesFitInTheTimeOfTwo() {
        editor.setNoteValue(NoteValue.EIGHTH);

        editor.toggleTriplet();

        assertEquals(Tuplet.of(3), editor.currentBeat().duration().tuplet());
        assertEquals(320, editor.currentBeat().duration().ticks());
    }

    @Test
    void theSameTripletTwiceGoesBackToAPlainFigure() {
        editor.toggleTriplet();
        editor.toggleTriplet();

        assertTrue(editor.currentBeat().duration().tuplet().isPlain());
    }

    @Test
    void movingANoteToAnotherStringKeepsItsPitch() {
        editor.moveDown();
        editor.setFret(5);
        int pitch = editor.currentTrack().pitchOf(editor.currentNote().orElseThrow()).midiNumber();

        editor.moveNoteUpOneString();

        assertEquals(1, editor.cursor().string());
        assertEquals(pitch, editor.currentTrack().pitchOf(editor.currentNote().orElseThrow()).midiNumber());
    }

    @Test
    void repeatingTheBeatFillsTheRestOfTheMeasure() {
        editor.setNoteValue(NoteValue.QUARTER);
        editor.setFret(3);

        editor.repeatBeatToTheEndOfTheMeasure();

        assertEquals(4, editor.currentMeasure().beats().size());
        assertTrue(editor.currentMeasure().isComplete());
    }
}
