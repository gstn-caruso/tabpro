package com.gstncaruso.tabpro.ui.dialogs.ascii;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.NoteValue;
import org.junit.jupiter.api.Test;

class AsciiImportPanelTest {

    @Test
    void startsWithEmptyText() {
        AsciiImportPanel panel = new AsciiImportPanel();

        assertTrue(panel.text().isEmpty());
    }

    @Test
    void setTextReplacesTheContent() {
        AsciiImportPanel panel = new AsciiImportPanel();

        panel.setText("|-5-|");

        assertEquals("|-5-|", panel.text());
    }

    @Test
    void defaultsToAFixedEighthNoteRhythm() {
        AsciiImportPanel panel = new AsciiImportPanel();

        assertTrue(panel.fixedRhythm().isPresent());
        assertEquals(NoteValue.EIGHTH, panel.fixedRhythm().get());
    }

    @Test
    void choosingAnotherFigureChangesTheFixedRhythm() {
        AsciiImportPanel panel = new AsciiImportPanel();

        panel.chooseFixedRhythm(NoteValue.QUARTER);

        assertEquals(NoteValue.QUARTER, panel.fixedRhythm().get());
    }

    @Test
    void choosingVariableMeansTheRhythmIsInferredFromSpacing() {
        AsciiImportPanel panel = new AsciiImportPanel();

        panel.chooseVariableRhythm();

        assertFalse(panel.fixedRhythm().isPresent());
    }
}
