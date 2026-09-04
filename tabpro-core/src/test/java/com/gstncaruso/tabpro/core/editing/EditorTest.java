package com.gstncaruso.tabpro.core.editing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.gstncaruso.tabpro.core.model.Score;
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
}
