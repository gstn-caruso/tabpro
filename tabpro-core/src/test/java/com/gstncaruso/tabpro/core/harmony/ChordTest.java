package com.gstncaruso.tabpro.core.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ChordTest {

    @Test
    void doMayorEsDoMiSol() {
        Chord doMayor = Chord.of(PitchClass.of("Do"), ChordType.MAJOR);
        assertEquals(
                List.of(PitchClass.of("Do"), PitchClass.of("Mi"), PitchClass.of("Sol")), doMayor.pitchClasses());
    }

    @Test
    void laMenorEsLaDoMi() {
        Chord laMenor = Chord.of(PitchClass.of("La"), ChordType.MINOR);
        assertEquals(
                List.of(PitchClass.of("La"), PitchClass.of("Do"), PitchClass.of("Mi")), laMenor.pitchClasses());
    }

    @Test
    void sinInversionElBajoEsLaFundamental() {
        Chord doMayor = Chord.of(PitchClass.of("Do"), ChordType.MAJOR);
        assertFalse(doMayor.isInverted());
        assertEquals(PitchClass.of("Do"), doMayor.bass());
    }

    @Test
    void unaInversionCambiaElBajoSinCambiarLasNotas() {
        Chord doConMiEnElBajo = Chord.inverted(PitchClass.of("Do"), ChordType.MAJOR, PitchClass.of("Mi"));
        assertTrue(doConMiEnElBajo.isInverted());
        assertEquals(PitchClass.of("Mi"), doConMiEnElBajo.bass());
        assertEquals(
                List.of(PitchClass.of("Do"), PitchClass.of("Mi"), PitchClass.of("Sol")),
                doConMiEnElBajo.pitchClasses());
    }

    @Test
    void elNombreLlevaLaFundamentalYElSufijo() {
        assertEquals("Do", Chord.of(PitchClass.of("Do"), ChordType.MAJOR).name());
        assertEquals("Lam", Chord.of(PitchClass.of("La"), ChordType.MINOR).name());
        assertEquals("Sol7", Chord.of(PitchClass.of("Sol"), ChordType.SEVENTH).name());
    }

    @Test
    void unaInversionSeEscribeConBarra() {
        Chord doConMiEnElBajo = Chord.inverted(PitchClass.of("Do"), ChordType.MAJOR, PitchClass.of("Mi"));
        assertEquals("Do/Mi", doConMiEnElBajo.name());
    }

    @Test
    void lasSemitonosEsencialesIncluyenElBajoAunqueSeaAjenoAlAcorde() {
        Chord doConReEnElBajo = Chord.inverted(PitchClass.of("Do"), ChordType.MAJOR, PitchClass.of("Re"));
        assertTrue(doConReEnElBajo.essentialSemitones().contains(PitchClass.of("Re").semitone()));
    }

    @Test
    void elFormulaCompletoIncluyeLosTonosOpcionales() {
        Chord sol7 = Chord.of(PitchClass.of("Sol"), ChordType.SEVENTH);
        assertTrue(sol7.formulaSemitones().contains(PitchClass.of("Re").semitone()));
    }
}
