package com.gstncaruso.tabpro.ui.dialogs.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.GraceNote;
import com.gstncaruso.tabpro.core.model.effects.GraceTransition;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.testsupport.Combos;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class GraceNotePanelTest {

    @Test
    void theFretAndOnBeatFieldsAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Fret", english.text("edit_dialogs.GraceNotePanel.fret"));
        assertEquals("On the beat (instead of before it)", english.text("edit_dialogs.GraceNotePanel.onBeat"));
    }

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(new GraceNotePanel(GraceNote.before(0)));
    }

    @Test
    void theDurationComboShowsTheNoteValueInSpanish() {
        GraceNotePanel panel = new GraceNotePanel(GraceNote.before(0));

        String renderedText = Combos.renderedTextOf(panel, NoteValue.class, NoteValue.QUARTER);

        assertEquals("Negra", renderedText);
    }

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
