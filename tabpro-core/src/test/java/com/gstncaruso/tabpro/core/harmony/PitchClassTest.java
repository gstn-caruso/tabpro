package com.gstncaruso.tabpro.core.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PitchClassTest {

    @Test
    void recognizesTheSevenNaturals() {
        assertEquals(0, PitchClass.of("Do").semitone());
        assertEquals(2, PitchClass.of("Re").semitone());
        assertEquals(4, PitchClass.of("Mi").semitone());
        assertEquals(5, PitchClass.of("Fa").semitone());
        assertEquals(7, PitchClass.of("Sol").semitone());
        assertEquals(9, PitchClass.of("La").semitone());
        assertEquals(11, PitchClass.of("Si").semitone());
    }

    @Test
    void distinguishesSolFromSiByThePrefix() {
        assertEquals("G", PitchClass.of("Sol").name());
        assertEquals("B", PitchClass.of("Si").name());
    }

    @Test
    void isWrittenWithALetterAndSungWithSolfege() {
        assertEquals("A", PitchClass.of("La").name());
        assertEquals("La", PitchClass.of("La").solfegeName());
        assertEquals("Bb", PitchClass.of("Sib").name());
        assertEquals("Sib", PitchClass.of("Sib").solfegeName());
    }

    @Test
    void alsoUnderstandsLetterNames() {
        assertEquals(PitchClass.of("La"), PitchClass.of("A"));
        assertEquals(PitchClass.of("Do#"), PitchClass.of("C#"));
        assertEquals(PitchClass.of("Sib"), PitchClass.of("Bb"));
    }

    @Test
    void aLetterNameIsNotConfusedWithASolfegeOne() {
        assertEquals(PitchClass.of("Re"), PitchClass.of("D"));
        assertEquals(PitchClass.of("Mi"), PitchClass.of("E"));
        assertEquals(PitchClass.of("Fa"), PitchClass.of("F"));
    }

    @Test
    void recognizesSharpsAndFlats() {
        assertEquals(1, PitchClass.of("Do#").semitone());
        assertEquals("C#", PitchClass.of("Do#").name());
        assertEquals(1, PitchClass.of("Reb").semitone());
        assertEquals("Db", PitchClass.of("Reb").name());
    }

    @Test
    void cSharpAndDFlatSoundTheSameButAreNotTheSameName() {
        assertEquals(PitchClass.of("Do#").semitone(), PitchClass.of("Reb").semitone());
        assertEquals(PitchClass.of("Do#"), PitchClass.of("Do#"));
        assertEquals(false, PitchClass.of("Do#").equals(PitchClass.of("Reb")));
    }

    @Test
    void rejectsAnUnknownName() {
        assertThrows(IllegalArgumentException.class, () -> PitchClass.of("Xa"));
    }

    @Test
    void rejectsAMixedAccidental() {
        assertThrows(IllegalArgumentException.class, () -> PitchClass.of("Do#b"));
    }

    @Test
    void aMajorThirdFromCIsE() {
        assertEquals(PitchClass.of("Mi"), PitchClass.of("Do").steppedBy(2, 4));
    }

    @Test
    void aMajorThirdFromDIsFSharp() {
        assertEquals(PitchClass.of("Fa#"), PitchClass.of("Re").steppedBy(2, 4));
    }

    @Test
    void aPerfectFifthFromFIsC() {
        assertEquals(PitchClass.of("Do"), PitchClass.of("Fa").steppedBy(4, 7));
    }

    @Test
    void isAlwaysSpelledWithSharpsByDefault() {
        assertEquals("C#", PitchClass.fromSemitone(1).name());
        assertEquals("D#", PitchClass.fromSemitone(3).name());
        assertEquals("C", PitchClass.fromSemitone(0).name());
    }

    @Test
    void theSemitoneIsNormalizedModuloTwelve() {
        assertEquals(PitchClass.of("Do").semitone(), PitchClass.of("Si").steppedBy(1, 1).semitone());
    }
}
