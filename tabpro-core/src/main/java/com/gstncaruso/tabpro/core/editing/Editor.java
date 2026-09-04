package com.gstncaruso.tabpro.core.editing;

import java.util.ArrayDeque;
import java.util.Deque;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;

public final class Editor {

    private Score score;
    private Cursor cursor;
    private final Deque<Snapshot> undoStack = new ArrayDeque<>();
    private final Deque<Snapshot> redoStack = new ArrayDeque<>();

    public Editor(Score initial) {
        this.score = initial;
        this.cursor = new Cursor(0, 0, 0, 1);
    }

    public Score score() {
        return score;
    }

    public Cursor cursor() {
        return cursor;
    }

    public Beat currentBeat() {
        return currentMeasure().beat(cursor.beat());
    }

    public void setFret(int fret) {
        change(withCurrentBeat(currentBeat().withNote(new Note(cursor.string(), fret))), cursor);
    }

    public void clearNote() {
        change(withCurrentBeat(currentBeat().withoutNoteOn(cursor.string())), cursor);
    }

    public void clearBeat() {
        change(withCurrentBeat(Beat.rest(currentBeat().duration())), cursor);
    }

    public void moveDown() {
        int maxString = currentTrack().tuning().stringCount();
        moveCursor(cursorAt(cursor.measure(), cursor.beat(), Math.min(maxString, cursor.string() + 1)));
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    private Cursor cursorAt(int measure, int beat, int string) {
        return new Cursor(cursor.track(), measure, beat, string);
    }

    private Track currentTrack() {
        return score.track(cursor.track());
    }

    private Measure currentMeasure() {
        return currentTrack().measure(cursor.measure());
    }

    private Score withCurrentBeat(Beat beat) {
        return withCurrentMeasure(currentMeasure().withBeat(cursor.beat(), beat));
    }

    private Score withCurrentMeasure(Measure measure) {
        Track track = currentTrack().withMeasure(cursor.measure(), measure);
        return score.withTrack(cursor.track(), track);
    }

    private void change(Score next, Cursor nextCursor) {
        if (next.equals(score)) {
            moveCursor(nextCursor);
            return;
        }
        undoStack.push(new Snapshot(score, cursor));
        redoStack.clear();
        score = next;
        cursor = nextCursor;
    }

    private void moveCursor(Cursor next) {
        cursor = next;
    }

    private record Snapshot(Score score, Cursor cursor) {
    }
}
