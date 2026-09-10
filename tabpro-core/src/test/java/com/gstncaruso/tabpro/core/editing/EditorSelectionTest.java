package com.gstncaruso.tabpro.core.editing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import java.util.List;
import org.junit.jupiter.api.Test;

class EditorSelectionTest {

    private final Editor editor = new Editor(
            new Score("Prueba", 120, List.of(Track.standardGuitar("Guitarra"), Track.standardBass("Bajo"))));

    @Test
    void movingTheCursorWithoutExtendingClearsAnyActiveSelection() {
        editor.startSelection(false);

        editor.moveRight();

        assertTrue(editor.selection().isEmpty());
    }

    @Test
    void thereIsNoSelectionBeforeAnythingSelectsAnything() {
        assertTrue(editor.selection().isEmpty());
    }

    @Test
    void startingASelectionCoversOnlyTheCurrentBeat() {
        editor.startSelection(false);

        Selection selection = editor.selection().orElseThrow();

        assertEquals(0, selection.fromBeat());
        assertEquals(0, selection.toBeat());
    }

    @Test
    void extendingSelectionSeveralTimesCoversEveryBeatInBetween() {
        editor.whileExtendingSelection(editor::moveRight);
        editor.whileExtendingSelection(editor::moveRight);
        editor.whileExtendingSelection(editor::moveRight);

        Selection selection = editor.selection().orElseThrow();

        assertEquals(0, selection.fromBeat());
        assertEquals(3, selection.toBeat());
    }

    @Test
    void movingRightPastTheLastMeasureStillClearsTheSelection() {
        editor.moveRight();
        editor.moveRight();
        editor.moveRight();
        editor.startSelection(false);

        editor.moveRight();

        assertTrue(editor.selection().isEmpty());
    }

    @Test
    void changingTrackClearsAnyActiveSelection() {
        editor.startSelection(false);

        editor.selectTrack(1);

        assertTrue(editor.selection().isEmpty());
    }

    @Test
    void undoRestoresTheSelectionThatWasActiveBeforeTheEdit() {
        editor.whileExtendingSelection(editor::moveRight);
        Selection selectionBeforeTheEdit = editor.selection().orElseThrow();

        editor.deleteBeat();

        editor.undo();

        assertEquals(selectionBeforeTheEdit, editor.selection().orElseThrow());
    }
}
