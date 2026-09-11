package com.gstncaruso.tabpro.core.editing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TestDefaultNames;
import org.junit.jupiter.api.Test;

class EditorHistoryTest {

    @Test
    void cannotUndoInitially() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
        assertFalse(editor.canUndo());
    }

    @Test
    void cannotRedoInitially() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
        assertFalse(editor.canRedo());
    }

    @Test
    void undoRestoresTheScoreBeforeTheLastChange() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
        Score before = editor.score();
        editor.setFret(5);
        editor.undo();
        assertEquals(before, editor.score());
    }

    @Test
    void undoRestoresTheCursorOfTheChange() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
        editor.moveRight();
        editor.undo();
        assertEquals(new Cursor(0, 0, 0, 1), editor.cursor());
    }

    @Test
    void redoReappliesAnUndoneChange() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
        editor.setFret(5);
        Score afterEdit = editor.score();
        editor.undo();
        editor.redo();
        assertEquals(afterEdit, editor.score());
    }

    @Test
    void aNewChangeClearsTheRedoHistory() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
        editor.setFret(5);
        editor.undo();
        editor.setFret(3);
        assertFalse(editor.canRedo());
    }

    @Test
    void cursorMovesAreNotUndoSteps() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
        editor.moveDown();
        assertFalse(editor.canUndo());
    }

    @Test
    void movingRightThatCreatesABeatIsAnUndoStep() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
        editor.moveRight();
        assertTrue(editor.canUndo());
    }

    @Test
    void undoWithNothingToUndoDoesNothing() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
        Score before = editor.score();
        Cursor cursorBefore = editor.cursor();
        editor.undo();
        assertEquals(before, editor.score());
        assertEquals(cursorBefore, editor.cursor());
    }

    @Test
    void notifiesTheListenerAfterAChange() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
        int[] notifications = new int[1];
        editor.addListener(() -> notifications[0]++);
        editor.setFret(5);
        assertEquals(1, notifications[0]);
    }

    @Test
    void notifiesTheListenerAfterACursorMove() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
        int[] notifications = new int[1];
        editor.addListener(() -> notifications[0]++);
        editor.moveDown();
        assertEquals(1, notifications[0]);
    }

    @Test
    void notifiesTheListenerAfterUndo() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
        editor.setFret(5);
        int[] notifications = new int[1];
        editor.addListener(() -> notifications[0]++);
        editor.undo();
        assertEquals(1, notifications[0]);
    }

    @Test
    void undoIsEnabledByDefault() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
        assertTrue(editor.isUndoEnabled());
    }

    @Test
    void disablingUndoForgetsThePastAndStopsRecordingNewChanges() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
        editor.setFret(5);

        editor.setUndoEnabled(false);

        assertFalse(editor.canUndo());
        editor.setFret(3);
        assertFalse(editor.canUndo());
    }

    @Test
    void reEnablingUndoRecordsChangesAgain() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
        editor.setUndoEnabled(false);
        editor.setUndoEnabled(true);

        editor.setFret(5);

        assertTrue(editor.canUndo());
    }

    @Test
    void replacingTheScoreResetsCursorAndHistory() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
        editor.setFret(5);
        editor.moveDown();
        int[] notifications = new int[1];
        editor.addListener(() -> notifications[0]++);
        Score newScore = Score.blank(new TestDefaultNames()).withTitle("Nueva");
        editor.replaceScore(newScore);
        assertEquals(newScore, editor.score());
        assertEquals(new Cursor(0, 0, 0, 1), editor.cursor());
        assertFalse(editor.canUndo());
        assertFalse(editor.canRedo());
        assertEquals(1, notifications[0]);
    }
}
