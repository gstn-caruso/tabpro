package com.gstncaruso.tabpro.ui.dialogs.instrument;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Instruments;
import com.gstncaruso.tabpro.core.model.InstrumentPatch;
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

    @Test
    void withoutAPatchSearchingUsesTheGeneralMidiNames() {
        assertTrue(InstrumentSearch.matching("banjo", InstrumentPatch.generalMidi()).contains(105));
    }

    @Test
    void aPatchReplacesTheNameUsedToSearchThatProgram() {
        InstrumentPatch patch = patchNaming(105, "Charango");

        assertTrue(InstrumentSearch.matching("charango", patch).contains(105));
        assertFalse(InstrumentSearch.matching("banjo", patch).contains(105));
    }

    private static InstrumentPatch patchNaming(int program, String name) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i <= program; i++) {
            if (i > 0) {
                text.append('\n');
            }
            text.append(i == program ? name : "");
        }
        return InstrumentPatch.parse(text.toString());
    }
}
