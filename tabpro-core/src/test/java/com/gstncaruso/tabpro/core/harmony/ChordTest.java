package com.gstncaruso.tabpro.core.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
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
        assertEquals("C", Chord.of(PitchClass.of("Do"), ChordType.MAJOR).name());
        assertEquals("Am", Chord.of(PitchClass.of("La"), ChordType.MINOR).name());
        assertEquals("G7", Chord.of(PitchClass.of("Sol"), ChordType.SEVENTH).name());
    }

    @Test
    void unaInversionSeEscribeConBarra() {
        Chord doConMiEnElBajo = Chord.inverted(PitchClass.of("Do"), ChordType.MAJOR, PitchClass.of("Mi"));
        assertEquals("C/E", doConMiEnElBajo.name());
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

    @Test
    void laPreferenciaPuedeEscribirLaInversionSinElBajo() {
        Chord doConMiEnElBajo = Chord.inverted(PitchClass.of("Do"), ChordType.MAJOR, PitchClass.of("Mi"));
        assertEquals("C", doConMiEnElBajo.name(false));
        assertEquals("C/E", doConMiEnElBajo.name(true));
    }

    @Test
    void unAcordeSinInversionSeEscribeIgualConCualquierPreferencia() {
        Chord doMayor = Chord.of(PitchClass.of("Do"), ChordType.MAJOR);
        assertEquals("C", doMayor.name(false));
        assertEquals("C", doMayor.name(true));
    }

    @Test
    void omitirUnTonoLoSacaDeLosSemitonosImprescindibles() {
        Chord doMayor = Chord.of(PitchClass.of("Do"), ChordType.MAJOR);
        assertFalse(doMayor.essentialSemitones(Set.of(Interval.PERFECT_FIFTH)).contains(PitchClass.of("Sol").semitone()));
        assertTrue(doMayor.essentialSemitones(Set.of()).contains(PitchClass.of("Sol").semitone()));
    }

    @Test
    void elBajoSigueSiendoImprescindibleAunqueSeOmitanTonosDelAcorde() {
        Chord doConReEnElBajo = Chord.inverted(PitchClass.of("Do"), ChordType.MAJOR, PitchClass.of("Re"));
        assertTrue(doConReEnElBajo.essentialSemitones(Set.of(Interval.MAJOR_THIRD, Interval.PERFECT_FIFTH))
                .contains(PitchClass.of("Re").semitone()));
    }

    @Test
    void omitirUnIntervaloQueElAcordeNoTieneNoCambiaNada() {
        Chord doMayor = Chord.of(PitchClass.of("Do"), ChordType.MAJOR);
        assertEquals(doMayor.essentialSemitones(Set.of()), doMayor.essentialSemitones(Set.of(Interval.MINOR_SEVENTH)));
    }
}
