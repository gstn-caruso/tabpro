package com.gstncaruso.tabpro.core.editing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class EditorTest {

    @Test
    void startsAtTheFirstBeatOfTheFirstMeasureOnStringOne() {
        Editor editor = new Editor(Score.blank());
        assertEquals(new Cursor(0, 0, 0, 1), editor.cursor());
    }

    @Test
    void startsWithTheGivenScore() {
        Score score = Score.blank().withTitle("Mi cancion");
        Editor editor = new Editor(score);
        assertEquals(score, editor.score());
    }

    @Test
    void cannotUndoInitially() {
        Editor editor = new Editor(Score.blank());
        assertFalse(editor.canUndo());
    }

    @Test
    void cannotRedoInitially() {
        Editor editor = new Editor(Score.blank());
        assertFalse(editor.canRedo());
    }

    @Test
    void writesAFretOnTheCursorString() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(5);
        Optional<Note> note = editor.currentBeat().noteOn(1);
        assertEquals(Optional.of(new Note(1, 5)), note);
    }

    @Test
    void overwritesTheFretOnTheSameString() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(5);
        editor.setFret(7);
        assertEquals(Optional.of(new Note(1, 7)), editor.currentBeat().noteOn(1));
    }

    @Test
    void keepsNotesOnOtherStringsOfTheBeat() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(5);
        editor.moveDown();
        editor.setFret(3);
        assertEquals(Optional.of(new Note(1, 5)), editor.currentBeat().noteOn(1));
        assertEquals(Optional.of(new Note(2, 3)), editor.currentBeat().noteOn(2));
    }

    @Test
    void clearsTheNoteUnderTheCursor() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(5);
        editor.clearNote();
        assertTrue(editor.currentBeat().noteOn(1).isEmpty());
    }

    @Test
    void clearingAnEmptyCellDoesNotCreateAnUndoStep() {
        Editor editor = new Editor(Score.blank());
        editor.clearNote();
        assertFalse(editor.canUndo());
    }

    @Test
    void turnsTheBeatIntoARest() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(5);
        editor.clearBeat();
        assertTrue(editor.currentBeat().isRest());
        assertEquals(editor.score().track(0).measure(0).beat(0).duration(), editor.currentBeat().duration());
    }
}
