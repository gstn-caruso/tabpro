package com.gstncaruso.tabpro.ui.dialogs.instrument;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Instruments;
import org.junit.jupiter.api.Test;

class InstrumentSearchTest {

    @Test
    void anEmptyQueryMatchesEverything() {
        assertEquals(Instruments.COUNT, InstrumentSearch.matching("").size());
    }

    @Test
    void matchesByPartialNameCaseInsensitively() {
        assertTrue(InstrumentSearch.matching("banjo").contains(105));
        assertTrue(InstrumentSearch.matching("BANJO").contains(105));
    }

    @Test
    void anUnknownWordMatchesNothing() {
        assertTrue(InstrumentSearch.matching("zzzznope").isEmpty());
    }

    @Test
    void matchesAnywhereInTheName() {
        assertTrue(InstrumentSearch.matching("guitar").contains(24));
        assertTrue(InstrumentSearch.matching("guitar").contains(25));
    }
}
