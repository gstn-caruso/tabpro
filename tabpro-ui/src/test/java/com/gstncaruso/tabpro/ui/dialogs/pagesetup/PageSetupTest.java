package com.gstncaruso.tabpro.ui.dialogs.pagesetup;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PageSetupTest {

    @Test
    void rejectsAScorePercentBelowTheMinimum() {
        assertThrows(IllegalArgumentException.class, () ->
                new PageSetup(PaperFormat.A4, Orientation.PORTRAIT, 0, 0, 0, 0, PageSetup.MIN_SCORE_PERCENT - 1, "", ""));
    }

    @Test
    void rejectsAScorePercentAboveTheMaximum() {
        assertThrows(IllegalArgumentException.class, () ->
                new PageSetup(PaperFormat.A4, Orientation.PORTRAIT, 0, 0, 0, 0, PageSetup.MAX_SCORE_PERCENT + 1, "", ""));
    }
}
