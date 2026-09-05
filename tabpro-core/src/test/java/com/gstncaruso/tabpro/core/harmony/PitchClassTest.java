package com.gstncaruso.tabpro.core.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PitchClassTest {

    @Test
    void reconoceLasSieteNaturales() {
        assertEquals(0, PitchClass.of("Do").semitone());
        assertEquals(2, PitchClass.of("Re").semitone());
        assertEquals(4, PitchClass.of("Mi").semitone());
        assertEquals(5, PitchClass.of("Fa").semitone());
        assertEquals(7, PitchClass.of("Sol").semitone());
        assertEquals(9, PitchClass.of("La").semitone());
        assertEquals(11, PitchClass.of("Si").semitone());
    }

    @Test
    void distingueSolDeSiPorElPrefijo() {
        assertEquals("Sol", PitchClass.of("Sol").name());
        assertEquals("Si", PitchClass.of("Si").name());
    }

    @Test
    void reconoceSostenidosYBemoles() {
        assertEquals(1, PitchClass.of("Do#").semitone());
        assertEquals("Do#", PitchClass.of("Do#").name());
        assertEquals(1, PitchClass.of("Reb").semitone());
        assertEquals("Reb", PitchClass.of("Reb").name());
    }

    @Test
    void doSostenidoYReBemolSuenanIgualPeroNoSonElMismoNombre() {
        assertEquals(PitchClass.of("Do#").semitone(), PitchClass.of("Reb").semitone());
        assertEquals(PitchClass.of("Do#"), PitchClass.of("Do#"));
        assertEquals(false, PitchClass.of("Do#").equals(PitchClass.of("Reb")));
    }

    @Test
    void rechazaUnNombreDesconocido() {
        assertThrows(IllegalArgumentException.class, () -> PitchClass.of("Xa"));
    }

    @Test
    void rechazaUnaAlteracionMezclada() {
        assertThrows(IllegalArgumentException.class, () -> PitchClass.of("Do#b"));
    }

    @Test
    void unaTerceraMayorDesdeDoEsMi() {
        assertEquals(PitchClass.of("Mi"), PitchClass.of("Do").steppedBy(2, 4));
    }

    @Test
    void unaTerceraMayorDesdeReEsFaSostenido() {
        assertEquals(PitchClass.of("Fa#"), PitchClass.of("Re").steppedBy(2, 4));
    }

    @Test
    void unaQuintaJustaDesdeFaEsDo() {
        assertEquals(PitchClass.of("Do"), PitchClass.of("Fa").steppedBy(4, 7));
    }

    @Test
    void seDeletreaSiempreConSostenidosPorDefecto() {
        assertEquals("Do#", PitchClass.fromSemitone(1).name());
        assertEquals("Re#", PitchClass.fromSemitone(3).name());
        assertEquals("Do", PitchClass.fromSemitone(0).name());
    }

    @Test
    void elSemitonoSeNormalizaModuloDoce() {
        assertEquals(PitchClass.of("Do").semitone(), PitchClass.of("Si").steppedBy(1, 1).semitone());
    }
}
