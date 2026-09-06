package com.gstncaruso.tabpro.ui.dialogs.wizards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class TranspositionPanelTest {

    @Test
    void defaultsToNoTranspositionOnTheCurrentTrack() {
        TranspositionPanel panel = new TranspositionPanel();

        assertEquals(0, panel.semitones());
        assertFalse(panel.everyTrack());
    }
}
