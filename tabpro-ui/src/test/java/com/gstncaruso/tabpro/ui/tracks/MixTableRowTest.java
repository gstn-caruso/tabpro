package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import java.awt.event.MouseEvent;
import org.junit.jupiter.api.Test;

class MixTableRowTest {

    @Test
    void clickingTheNumberSelectsThatTrack() {
        Editor editor = twoTrackEditor();
        MixTableRow row = new MixTableRow(editor, new MixTableModel(), 1);

        row.numberLabel().dispatchEvent(pressOn(row.numberLabel()));

        assertEquals(1, editor.cursor().track());
    }

    @Test
    void clickingTheNameSelectsThatTrack() {
        Editor editor = twoTrackEditor();
        MixTableRow row = new MixTableRow(editor, new MixTableModel(), 1);

        row.nameLabel().dispatchEvent(pressOn(row.nameLabel()));

        assertEquals(1, editor.cursor().track());
    }

    @Test
    void everyTrackStartsVisibleInTheMultitrackView() {
        Editor editor = twoTrackEditor();
        MixTableRow row = new MixTableRow(editor, new MixTableModel(), 0);

        assertTrue(row.visibleCheckbox().isSelected());
    }

    @Test
    void togglingTheCheckboxHidesTheTrackFromTheMultitrackView() {
        Editor editor = twoTrackEditor();
        MixTableModel model = new MixTableModel();
        MixTableRow row = new MixTableRow(editor, model, 0);

        row.visibleCheckbox().doClick();

        assertFalse(model.isVisibleInMultitrackView(0));
        assertFalse(row.visibleCheckbox().isSelected());
    }

    @Test
    void changingThePortPushesItToTheEditor() {
        Editor editor = twoTrackEditor();
        MixTableRow row = new MixTableRow(editor, new MixTableModel(), 0);

        row.portField().setValue(3);

        assertEquals(3, editor.score().track(0).channel().port());
    }

    @Test
    void changingTheChannelPushesItToTheEditor() {
        Editor editor = twoTrackEditor();
        MixTableRow row = new MixTableRow(editor, new MixTableModel(), 0);

        row.channelField().setValue(7);

        assertEquals(7, editor.score().track(0).channel().number());
    }

    @Test
    void reducingAllParametersHidesTheKnobColumnsOnly() {
        Editor editor = twoTrackEditor();
        MixTableModel model = new MixTableModel();
        MixTableRow row = new MixTableRow(editor, model, 0);

        model.reduceAllParameters();
        row.refresh();

        assertTrue(row.parameterCells().stream().noneMatch(java.awt.Component::isVisible));
        assertTrue(row.nameLabel().isVisible(), "el nombre sigue visible al reducir");
        assertTrue(row.portField().isVisible(), "el puerto no es un parametro de sonido");
    }

    @Test
    void restoringShowsTheParametersAgain() {
        Editor editor = twoTrackEditor();
        MixTableModel model = new MixTableModel();
        MixTableRow row = new MixTableRow(editor, model, 0);
        model.reduceAllParameters();
        row.refresh();

        model.restoreAllParameters();
        row.refresh();

        assertTrue(row.parameterCells().stream().allMatch(java.awt.Component::isVisible));
    }

    private static Editor twoTrackEditor() {
        Editor editor = new Editor(Score.blank());
        editor.addTrack(Track.standardBass("Bajo"));
        return editor;
    }

    private static MouseEvent pressOn(java.awt.Component target) {
        return new MouseEvent(target, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, 2, 2, 1, false);
    }
}
