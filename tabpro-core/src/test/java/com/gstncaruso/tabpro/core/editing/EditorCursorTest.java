package com.gstncaruso.tabpro.core.editing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import java.util.List;
import org.junit.jupiter.api.Test;

class EditorCursorTest {

    @Test
    void movesRightToTheNextBeat() {
        Beat first = Beat.of(Duration.quarter(), new Note(1, 0));
        Beat second = Beat.of(Duration.quarter(), new Note(1, 1));
        Editor editor = editorWithMeasure(new Measure(TimeSignature.fourFour(), List.of(first, second)));
        editor.moveRight();
        assertEquals(new Cursor(0, 0, 1, 1), editor.cursor());
        assertFalse(editor.canUndo());
    }

    @Test
    void movingRightAtTheEndOfAnIncompleteMeasureAppendsARest() {
        Editor editor = new Editor(Score.blank());
        editor.moveRight();
        assertEquals(new Cursor(0, 0, 1, 1), editor.cursor());
        assertTrue(editor.currentBeat().isRest());
        assertEquals(Duration.quarter(), editor.currentBeat().duration());
        assertTrue(editor.canUndo());
    }

    @Test
    void movingRightAtTheEndOfACompleteMeasureEntersTheNextMeasure() {
        Measure complete = new Measure(TimeSignature.fourFour(), List.of(
                Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter()),
                Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter())));
        Measure next = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        Editor editor = editorWithMeasures(complete, next);
        editor.moveTo(0, 3, 1);
        editor.moveRight();
        assertEquals(new Cursor(0, 1, 0, 1), editor.cursor());
        assertFalse(editor.canUndo());
    }

    @Test
    void movingRightAtTheEndOfTheLastCompleteMeasureAppendsAMeasure() {
        Measure complete = new Measure(TimeSignature.fourFour(), List.of(
                Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter()),
                Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter())));
        Editor editor = editorWithMeasure(complete);
        editor.moveTo(0, 3, 1);
        editor.moveRight();
        assertEquals(2, editor.score().track(0).measures().size());
        assertEquals(new Cursor(0, 1, 0, 1), editor.cursor());
        assertTrue(editor.canUndo());
    }

    @Test
    void movesLeftToThePreviousBeat() {
        Beat first = Beat.of(Duration.quarter(), new Note(1, 0));
        Beat second = Beat.of(Duration.quarter(), new Note(1, 1));
        Editor editor = editorWithMeasure(new Measure(TimeSignature.fourFour(), List.of(first, second)));
        editor.moveTo(0, 1, 1);
        editor.moveLeft();
        assertEquals(new Cursor(0, 0, 0, 1), editor.cursor());
    }

    @Test
    void movingLeftAtTheStartOfAMeasureGoesToTheLastBeatOfThePreviousOne() {
        Measure first = new Measure(TimeSignature.fourFour(), List.of(
                Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter())));
        Measure second = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        Editor editor = editorWithMeasures(first, second);
        editor.moveTo(1, 0, 1);
        editor.moveLeft();
        assertEquals(new Cursor(0, 0, 1, 1), editor.cursor());
    }

    @Test
    void movingLeftAtTheStartOfTheScoreStaysPut() {
        Editor editor = new Editor(Score.blank());
        editor.moveLeft();
        assertEquals(new Cursor(0, 0, 0, 1), editor.cursor());
    }

    @Test
    void movesDownToTheNextString() {
        Editor editor = new Editor(Score.blank());
        editor.moveDown();
        assertEquals(new Cursor(0, 0, 0, 2), editor.cursor());
    }

    @Test
    void movingDownOnTheLastStringStaysPut() {
        Editor editor = new Editor(Score.blank());
        int lastString = editor.score().track(0).tuning().stringCount();
        editor.moveTo(0, 0, lastString);
        editor.moveDown();
        assertEquals(lastString, editor.cursor().string());
    }

    @Test
    void movesUpToThePreviousString() {
        Editor editor = new Editor(Score.blank());
        editor.moveTo(0, 0, 3);
        editor.moveUp();
        assertEquals(new Cursor(0, 0, 0, 2), editor.cursor());
    }

    @Test
    void movingUpOnStringOneStaysPut() {
        Editor editor = new Editor(Score.blank());
        editor.moveUp();
        assertEquals(new Cursor(0, 0, 0, 1), editor.cursor());
    }

    @Test
    void homeMovesToTheFirstBeatOfTheMeasure() {
        Beat first = Beat.of(Duration.quarter(), new Note(1, 0));
        Beat second = Beat.of(Duration.quarter(), new Note(1, 1));
        Editor editor = editorWithMeasure(new Measure(TimeSignature.fourFour(), List.of(first, second)));
        editor.moveTo(0, 1, 1);
        editor.moveToMeasureStart();
        assertEquals(new Cursor(0, 0, 0, 1), editor.cursor());
    }

    @Test
    void endMovesToTheLastBeatOfTheMeasure() {
        Beat first = Beat.of(Duration.quarter(), new Note(1, 0));
        Beat second = Beat.of(Duration.quarter(), new Note(1, 1));
        Editor editor = editorWithMeasure(new Measure(TimeSignature.fourFour(), List.of(first, second)));
        editor.moveToMeasureEnd();
        assertEquals(new Cursor(0, 0, 1, 1), editor.cursor());
    }

    @Test
    void movesToAnArbitraryPosition() {
        Editor editor = new Editor(Score.blank());
        editor.moveTo(0, 0, 3);
        assertEquals(new Cursor(0, 0, 0, 3), editor.cursor());
    }

    @Test
    void rejectsAnInvalidPosition() {
        Editor editor = new Editor(Score.blank());
        assertThrows(IllegalArgumentException.class, () -> editor.moveTo(5, 0, 1));
    }

    private Editor editorWithMeasure(Measure measure) {
        Track track = Track.standardGuitar("Test").withMeasure(0, measure);
        return new Editor(Score.blank().withTrack(0, track));
    }

    private Editor editorWithMeasures(Measure... measures) {
        Track base = Track.standardGuitar("Test");
        Track track = base.withMeasure(0, measures[0]);
        for (int i = 1; i < measures.length; i++) {
            track = track.withMeasureInsertedAt(i, measures[i]);
        }
        return new Editor(Score.blank().withTrack(0, track));
    }
}
