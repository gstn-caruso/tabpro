package com.gstncaruso.tabpro.core.model.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DynamicTest {

    @Test
    void louderDynamicsSoundWithMoreVelocity() {
        assertTrue(Dynamic.FORTE.velocity() > Dynamic.PIANO.velocity());
    }

    @Test
    void noneGoesOutsideTheMidiRange() {
        for (Dynamic dynamic : Dynamic.values()) {
            assertTrue(dynamic.velocity() >= 1 && dynamic.velocity() <= 127, dynamic.name());
        }
    }

    @Test
    void anAccentedNoteSoundsLouderThanItsPlainSelf() {
        assertTrue(Dynamic.MEZZO_FORTE.accented().value() > Dynamic.MEZZO_FORTE.velocity());
    }

    @Test
    void aGhostNoteSoundsSofterThanItsPlainSelf() {
        assertTrue(Dynamic.MEZZO_FORTE.ghosted().value() < Dynamic.MEZZO_FORTE.velocity());
    }

    @Test
    void theLoudestAccentedStaysInRange() {
        assertEquals(127, Dynamic.FORTE_FORTISSIMO.accented().accented().value());
    }

    @Test
    void theSoftestGhostedStaysInRange() {
        assertTrue(Dynamic.PIANO_PIANISSIMO.ghosted().ghosted().value() >= 1);
    }

    @Test
    void theDefaultIsMezzoForte() {
        assertEquals(Dynamic.MEZZO_FORTE, Dynamic.defaultDynamic());
    }
}
