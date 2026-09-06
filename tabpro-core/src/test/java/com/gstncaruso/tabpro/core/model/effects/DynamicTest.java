package com.gstncaruso.tabpro.core.model.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DynamicTest {

    @Test
    void lasMasFuertesSuenanConMasVelocidad() {
        assertTrue(Dynamic.FORTE.velocity() > Dynamic.PIANO.velocity());
    }

    @Test
    void ningunaSeVaDelRangoMidi() {
        for (Dynamic dynamic : Dynamic.values()) {
            assertTrue(dynamic.velocity() >= 1 && dynamic.velocity() <= 127, dynamic.name());
        }
    }

    @Test
    void unaNotaAcentuadaSuenaMasFuerteQueLaMisma() {
        assertTrue(Dynamic.MEZZO_FORTE.accented().value() > Dynamic.MEZZO_FORTE.velocity());
    }

    @Test
    void unaNotaFantasmaSuenaMasSuaveQueLaMisma() {
        assertTrue(Dynamic.MEZZO_FORTE.ghosted().value() < Dynamic.MEZZO_FORTE.velocity());
    }

    @Test
    void laMasFuerteAcentuadaSigueEnRango() {
        assertEquals(127, Dynamic.FORTE_FORTISSIMO.accented().accented().value());
    }

    @Test
    void laMasSuaveFantasmaSigueEnRango() {
        assertTrue(Dynamic.PIANO_PIANISSIMO.ghosted().ghosted().value() >= 1);
    }

    @Test
    void elDefaultEsMezzoForte() {
        assertEquals(Dynamic.MEZZO_FORTE, Dynamic.defaultDynamic());
    }
}
