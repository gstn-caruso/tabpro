package com.gstncaruso.tabpro.core.playback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** El rango de compases que se repite en un loop de practica. */
class LoopRangeTest {

    @Test
    void contieneLosCompasesEntreSusExtremosInclusive() {
        LoopRange loop = new LoopRange(2, 5);

        assertFalse(loop.contains(1));
        assertTrue(loop.contains(2));
        assertTrue(loop.contains(5));
        assertFalse(loop.contains(6));
    }

    @Test
    void cuentaCuantosCompasesTiene() {
        assertEquals(4, new LoopRange(2, 5).measureCount());
        assertEquals(1, new LoopRange(3, 3).measureCount());
    }

    @Test
    void rechazaUnRangoInvertido() {
        assertThrows(IllegalArgumentException.class, () -> new LoopRange(5, 2));
    }

    @Test
    void seConvierteEnUnPlayOrderQueSeRepite() {
        LoopRange loop = new LoopRange(1, 2);

        PlayOrder order = loop.asPlayOrder(3);

        assertEquals(java.util.List.of(1, 2, 1, 2, 1, 2), order.measureIndexes());
    }
}
