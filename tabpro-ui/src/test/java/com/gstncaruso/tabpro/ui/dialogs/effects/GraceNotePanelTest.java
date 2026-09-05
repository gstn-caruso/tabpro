package com.gstncaruso.tabpro.ui.dialogs.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.GraceNote;
import com.gstncaruso.tabpro.core.model.effects.GraceTransition;
import org.junit.jupiter.api.Test;

class GraceNotePanelTest {

    @Test
    void startsWithTheGivenGraceNote() {
        GraceNote grace = new GraceNote(5, NoteValue.SIXTEENTH, Dynamic.FORTE, GraceTransition.HAMMER, true, false);

        GraceNotePanel panel = new GraceNotePanel(grace);

        assertEquals(grace, panel.toGraceNote());
    }

    @Test
    void reflectsWhateverIsLoadedAfterwards() {
        GraceNotePanel panel = new GraceNotePanel(GraceNote.before(0));

        panel.apply(new GraceNote(9, NoteValue.EIGHTH, Dynamic.PIANO, GraceTransition.SLIDE, false, false));

        GraceNote result = panel.toGraceNote();
        assertEquals(9, result.fret());
        assertEquals(NoteValue.EIGHTH, result.duration());
        assertEquals(Dynamic.PIANO, result.dynamic());
        assertEquals(GraceTransition.SLIDE, result.transition());
        assertEquals(false, result.onBeat());
    }
}
