package com.gstncaruso.tabpro.core.notation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import org.junit.jupiter.api.Test;

class KeySignatureAccidentalsTest {

    private static final Pitch F_NATURAL = new Pitch(41);
    private static final Pitch F_SHARP = new Pitch(42);

    private static KeySignature gMajor() {
        return new KeySignature(1, com.gstncaruso.tabpro.core.model.bars.Mode.MAJOR);
    }

    @Test
    void aNaturalNoteInCMajorNeedsNoAccidental() {
        KeySignatureAccidentals accidentals = new KeySignatureAccidentals(Clef.TREBLE, KeySignature.cMajor());
        StaffPosition position = StaffPosition.of(F_NATURAL, Clef.TREBLE);

        assertEquals(AccidentalGlyph.NONE, accidentals.glyphFor(position));
    }

    @Test
    void aSharpNoteInCMajorNeedsASharp() {
        KeySignatureAccidentals accidentals = new KeySignatureAccidentals(Clef.TREBLE, KeySignature.cMajor());
        StaffPosition position = StaffPosition.of(F_SHARP, Clef.TREBLE);

        assertEquals(AccidentalGlyph.SHARP, accidentals.glyphFor(position));
    }

    @Test
    void theKeysOwnSharpNeedsNoAccidental() {
        KeySignatureAccidentals accidentals = new KeySignatureAccidentals(Clef.TREBLE, gMajor());
        StaffPosition position = StaffPosition.of(F_SHARP, Clef.TREBLE);

        assertEquals(AccidentalGlyph.NONE, accidentals.glyphFor(position));
    }

    @Test
    void aNaturalCancelsTheKeysSharp() {
        KeySignatureAccidentals accidentals = new KeySignatureAccidentals(Clef.TREBLE, gMajor());
        StaffPosition position = StaffPosition.of(F_NATURAL, Clef.TREBLE);

        assertEquals(AccidentalGlyph.NATURAL, accidentals.glyphFor(position));
    }

    @Test
    void theSharpComesBackAfterANaturalInTheSameMeasure() {
        KeySignatureAccidentals accidentals = new KeySignatureAccidentals(Clef.TREBLE, gMajor());
        StaffPosition sharp = StaffPosition.of(F_SHARP, Clef.TREBLE);
        StaffPosition natural = StaffPosition.of(F_NATURAL, Clef.TREBLE);

        assertEquals(AccidentalGlyph.NONE, accidentals.glyphFor(sharp));
        assertEquals(AccidentalGlyph.NATURAL, accidentals.glyphFor(natural));
        assertEquals(AccidentalGlyph.SHARP, accidentals.glyphFor(sharp));
    }

    @Test
    void aFreshMeasureForgetsThePreviousOne() {
        KeySignatureAccidentals firstMeasure = new KeySignatureAccidentals(Clef.TREBLE, gMajor());
        firstMeasure.glyphFor(StaffPosition.of(F_NATURAL, Clef.TREBLE));

        KeySignatureAccidentals secondMeasure = new KeySignatureAccidentals(Clef.TREBLE, gMajor());
        AccidentalGlyph glyph = secondMeasure.glyphFor(StaffPosition.of(F_SHARP, Clef.TREBLE));

        assertEquals(AccidentalGlyph.NONE, glyph, "la armadura vuelve a regir sola al empezar el compas");
    }
}
