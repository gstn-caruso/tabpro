package com.gstncaruso.tabpro.ui.instruments;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NoteNameModeTest {

    @Test
    void noneNeverShowsAName() {
        assertFalse(NoteNameMode.NONE.shows(MarkKind.PRIMARY));
        assertFalse(NoteNameMode.NONE.shows(MarkKind.SECONDARY));
    }

    @Test
    void beatOnlyShowsJustThePrimaryOnes() {
        assertTrue(NoteNameMode.BEAT_ONLY.shows(MarkKind.PRIMARY));
        assertFalse(NoteNameMode.BEAT_ONLY.shows(MarkKind.SECONDARY));
    }

    @Test
    void allShowsBothKinds() {
        assertTrue(NoteNameMode.ALL.shows(MarkKind.PRIMARY));
        assertTrue(NoteNameMode.ALL.shows(MarkKind.SECONDARY));
    }
}
