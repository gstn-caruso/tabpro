package com.gstncaruso.tabpro.core.editing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import java.util.List;
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

    @Test
    void lengthensTheCursorBeat() {
        Editor editor = new Editor(Score.blank());
        editor.lengthenDuration();
        assertEquals(Duration.quarter().longer(), editor.currentBeat().duration());
    }

    @Test
    void shortensTheCursorBeat() {
        Editor editor = new Editor(Score.blank());
        editor.shortenDuration();
        assertEquals(Duration.quarter().shorter(), editor.currentBeat().duration());
    }

    @Test
    void togglesTheDotOfTheCursorBeat() {
        Editor editor = new Editor(Score.blank());
        editor.toggleDot();
        assertTrue(editor.currentBeat().duration().dotted());
        editor.toggleDot();
        assertFalse(editor.currentBeat().duration().dotted());
    }

    @Test
    void insertsARestBeforeTheCursorWithTheSameDuration() {
        Beat first = Beat.of(Duration.quarter().shorter(), new Note(1, 3));
        Editor editor = editorWithMeasure(new Measure(TimeSignature.fourFour(), List.of(first)));
        editor.insertBeat();
        assertTrue(editor.currentBeat().isRest());
        assertEquals(Duration.quarter().shorter(), editor.currentBeat().duration());
        assertEquals(first, editor.score().track(0).measure(0).beat(1));
    }

    @Test
    void deletesTheCursorBeat() {
        Beat first = Beat.of(Duration.quarter(), new Note(1, 0));
        Beat second = Beat.of(Duration.quarter(), new Note(1, 1));
        Editor editor = editorWithMeasure(new Measure(TimeSignature.fourFour(), List.of(first, second)));
        editor.deleteBeat();
        assertEquals(List.of(second), editor.score().track(0).measure(0).beats());
        assertEquals(new Cursor(0, 0, 0, 1), editor.cursor());
    }

    @Test
    void deletingTheOnlyBeatLeavesARest() {
        Beat onlyBeat = Beat.of(Duration.quarter().shorter(), new Note(1, 5));
        Editor editor = editorWithMeasure(new Measure(TimeSignature.fourFour(), List.of(onlyBeat)));
        editor.deleteBeat();
        assertTrue(editor.currentBeat().isRest());
        assertEquals(Duration.quarter().shorter(), editor.currentBeat().duration());
        assertEquals(new Cursor(0, 0, 0, 1), editor.cursor());
    }

    @Test
    void deletingTheLastBeatMovesTheCursorToThePreviousOne() {
        Beat first = Beat.of(Duration.quarter(), new Note(1, 0));
        Beat second = Beat.of(Duration.quarter(), new Note(1, 1));
        Editor editor = editorWithMeasure(new Measure(TimeSignature.fourFour(), List.of(first, second)));
        editor.moveTo(0, 1, 1);
        editor.deleteBeat();
        assertEquals(List.of(first), editor.score().track(0).measure(0).beats());
        assertEquals(new Cursor(0, 0, 0, 1), editor.cursor());
    }

    private Editor editorWithMeasure(Measure measure) {
        Track track = Track.standardGuitar("Test").withMeasure(0, measure);
        return new Editor(Score.blank().withTrack(0, track));
    }
}
