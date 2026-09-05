package com.gstncaruso.tabpro.ui.tab;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FretDigitsTest {

    private final long[] now = {0L};
    private final FretDigits digits = new FretDigits(() -> now[0]);

    @Test
    void aSingleDigitIsThatFret() {
        assertEquals(1, digits.fretFor('1'));
    }

    @Test
    void twoDigitsWithinTheWindowCombine() {
        assertEquals(1, digits.fretFor('1'));
        now[0] += 100;
        assertEquals(12, digits.fretFor('2'));
    }

    @Test
    void twoDigitsAfterTheWindowStartANewFret() {
        assertEquals(1, digits.fretFor('1'));
        now[0] += FretDigits.DEFAULT_WINDOW_MILLIS;
        assertEquals(2, digits.fretFor('2'));
    }

    @Test
    void aCombinationAboveTheHighestFretStartsANewFret() {
        assertEquals(3, digits.fretFor('3'));
        now[0] += 100;
        assertEquals(5, digits.fretFor('5'));
    }

    @Test
    void aThirdDigitStartsANewFret() {
        assertEquals(1, digits.fretFor('1'));
        now[0] += 100;
        assertEquals(12, digits.fretFor('2'));
        now[0] += 100;
        assertEquals(3, digits.fretFor('3'));
    }

    @Test
    void resetForgetsThePreviousDigit() {
        assertEquals(1, digits.fretFor('1'));
        digits.reset();
        now[0] += 100;
        assertEquals(2, digits.fretFor('2'));
    }
}
