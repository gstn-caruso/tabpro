package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import java.awt.event.MouseEvent;
import org.junit.jupiter.api.Test;

class MixTableTest {

    @Test
    void listsOneRowPerTrack() {
        Editor editor = new Editor(Score.blank());
        MixTable table = new MixTable(editor);

        assertEquals(1, table.rows().size());
    }

    @Test
    void followsTheEditorWhenATrackIsAdded() {
        Editor editor = new Editor(Score.blank());
        MixTable table = new MixTable(editor);

        editor.addTrack(Track.standardBass("Bajo"));
        table.refresh();

        assertEquals(2, table.rows().size());
    }

    @Test
    void reduceAllParametersButtonHidesEveryRowsKnobs() {
        Editor editor = new Editor(Score.blank());
        editor.addTrack(Track.standardBass("Bajo"));
        MixTable table = new MixTable(editor);

        table.model().reduceAllParameters();
        table.refresh();

        for (MixTableRow row : table.rows()) {
            assertTrue(row.parameterCells().stream().noneMatch(java.awt.Component::isVisible));
        }
    }

    @Test
    void restoreAllParametersButtonShowsThemAgain() {
        Editor editor = new Editor(Score.blank());
        MixTable table = new MixTable(editor);
        table.model().reduceAllParameters();
        table.refresh();

        table.model().restoreAllParameters();
        table.refresh();

        for (MixTableRow row : table.rows()) {
            assertTrue(row.parameterCells().stream().allMatch(java.awt.Component::isVisible));
        }
    }

    @Test
    void clickingAColumnTitleTogglesThatParameterEverywhere() {
        Editor editor = new Editor(Score.blank());
        editor.addTrack(Track.standardBass("Bajo"));
        MixTable table = new MixTable(editor);

        table.headerFor(MixParameter.VOLUME).dispatchEvent(pressOn(table.headerFor(MixParameter.VOLUME)));

        assertEquals(DisplayMode.NUMBER, table.model().displayModeOf(MixParameter.VOLUME));
        for (MixTableRow row : table.rows()) {
            assertTrue(row.parameterCells().get(0).isShowingNumber());
        }
    }

    @Test
    void theReduceButtonHidesEveryKnobAndTheRestoreButtonBringsThemBack() {
        Editor editor = new Editor(Score.blank());
        MixTable table = new MixTable(editor);

        table.reduceButton().doClick();
        assertTrue(table.rows().get(0).parameterCells().stream().noneMatch(java.awt.Component::isVisible));

        table.restoreButton().doClick();
        assertTrue(table.rows().get(0).parameterCells().stream().allMatch(java.awt.Component::isVisible));
    }

    private static MouseEvent pressOn(java.awt.Component target) {
        return new MouseEvent(target, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, 2, 2, 1, false);
    }

    @Test
    void aFreshTableShowsEveryParameterAsAKnob() {
        Editor editor = new Editor(Score.blank());
        MixTable table = new MixTable(editor);

        assertFalse(table.model().isReduced());
        for (MixParameter parameter : MixParameter.values()) {
            assertEquals(DisplayMode.KNOB, table.model().displayModeOf(parameter));
        }
    }
}
