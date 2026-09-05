package com.gstncaruso.tabpro.core.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class ScaleTest {

    private static final Scale MAJOR = new Scale("Mayor", List.of(0, 2, 4, 5, 7, 9, 11), List.of(0, 1, 2, 3, 4, 5, 6));

    @Test
    void doMayorSonLasSieteNaturales() {
        assertEquals(
                List.of("Do", "Re", "Mi", "Fa", "Sol", "La", "Si"),
                MAJOR.notesFrom(PitchClass.of("Do")).stream().map(nota -> nota.pitchClass().name()).toList());
    }

    @Test
    void elGradoEsLaPosicionEnLaEscala() {
        List<ScaleTone> notas = MAJOR.notesFrom(PitchClass.of("Do"));
        assertEquals(1, notas.get(0).degree());
        assertEquals(5, notas.get(4).degree());
        assertEquals(7, notas.get(6).degree());
    }

    @Test
    void elIntervaloEsRelativoALaTonica() {
        List<ScaleTone> notas = MAJOR.notesFrom(PitchClass.of("Do"));
        assertEquals(Interval.MAJOR_THIRD, notas.get(2).interval());
        assertEquals(Interval.PERFECT_FIFTH, notas.get(4).interval());
    }

    @Test
    void reMayorLlevaSostenidos() {
        assertEquals(
                List.of("Re", "Mi", "Fa#", "Sol", "La", "Si", "Do#"),
                MAJOR.notesFrom(PitchClass.of("Re")).stream().map(nota -> nota.pitchClass().name()).toList());
    }

    @Test
    void rechazaListasDeDistintoTamanio() {
        assertThrows(IllegalArgumentException.class, () -> new Scale("Mala", List.of(0, 2, 4), List.of(0, 1)));
    }

    @Test
    void rechazaUnaEscalaVacia() {
        assertThrows(IllegalArgumentException.class, () -> new Scale("Vacia", List.of(), List.of()));
    }

    @Test
    void expresaLosSemitonosComoClasesDeAltura() {
        assertEquals(List.of(0, 2, 4, 5, 7, 9, 11), MAJOR.semitones());
        assertEquals(7, MAJOR.degreeCount());
    }
}
