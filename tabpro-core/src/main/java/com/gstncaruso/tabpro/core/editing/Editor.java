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

    public void lengthenDuration() {
        change(withCurrentBeat(currentBeat().withDuration(currentBeat().duration().longer())), cursor);
    }

    public void shortenDuration() {
        change(withCurrentBeat(currentBeat().withDuration(currentBeat().duration().shorter())), cursor);
    }

    public void toggleDot() {
        change(withCurrentBeat(currentBeat().withDuration(currentBeat().duration().toggledDot())), cursor);
    }

    public void insertBeat() {
        Beat rest = Beat.rest(currentBeat().duration());
        Measure measure = currentMeasure().withBeatInsertedAt(cursor.beat(), rest);
        change(withCurrentMeasure(measure), cursor);
    }

    public void deleteBeat() {
        Measure measure = currentMeasure().withoutBeatAt(cursor.beat());
        int beat = Math.min(cursor.beat(), measure.beats().size() - 1);
        change(withCurrentMeasure(measure), cursorAt(cursor.measure(), beat, cursor.string()));
    }

    public void insertMeasure() {
        Measure empty = Measure.empty(currentMeasure().timeSignature(), currentBeat().duration());
        Track track = currentTrack().withMeasureInsertedAt(cursor.measure(), empty);
        Score next = score.withTrack(cursor.track(), track);
        change(next, cursorAt(cursor.measure(), 0, cursor.string()));
    }

    public void deleteMeasure() {
        Track track = currentTrack().withoutMeasureAt(cursor.measure());
        int measure = cursor.measure();
        int beat = cursor.beat();
        if (measure >= track.measures().size()) {
            measure--;
            beat = 0;
        }
        Score next = score.withTrack(cursor.track(), track);
        change(next, cursorAt(measure, beat, cursor.string()));
    }

    public void setTempo(int bpm) {
        change(score.withTempo(bpm), cursor);
    }

    public void setTitle(String title) {
        change(score.withTitle(title), cursor);
    }

    public void moveTo(int measure, int beat, int string) {
        Track track = currentTrack();
        if (measure < 0 || measure >= track.measures().size()) {
            throw new IllegalArgumentException("measure fuera de rango: " + measure);
        }
        Measure targetMeasure = track.measure(measure);
        if (beat < 0 || beat >= targetMeasure.beats().size()) {
            throw new IllegalArgumentException("beat fuera de rango: " + beat);
        }
        if (string < 1 || string > track.tuning().stringCount()) {
            throw new IllegalArgumentException("string fuera de rango: " + string);
        }
        moveCursor(cursorAt(measure, beat, string));
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
