package com.gstncaruso.tabpro.core.editing;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.UnaryOperator;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;

public final class Editor {

    private Score score;
    private Cursor cursor;
    private final Deque<Snapshot> undoStack = new ArrayDeque<>();
    private final Deque<Snapshot> redoStack = new ArrayDeque<>();
    private final List<EditorListener> listeners = new ArrayList<>();

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
        change(score.withMeasureInsertedInEveryTrackAt(cursor.measure()),
                cursorAt(cursor.measure(), 0, cursor.string()));
    }

    public void deleteMeasure() {
        Score next = score.withoutMeasureInEveryTrackAt(cursor.measure());
        int measure = Math.min(cursor.measure(), next.track(cursor.track()).measureCount() - 1);
        change(next, cursorAt(measure, 0, cursor.string()));
    }

    public void addTrack(Track track) {
        Track padded = paddedToScoreLength(track);
        Score next = score.withTrackAdded(padded);
        change(next, clampedCursorIn(next, next.trackCount() - 1));
    }

    public void removeCurrentTrack() {
        Score next = score.withoutTrackAt(cursor.track());
        change(next, clampedCursorIn(next, Math.max(0, cursor.track() - 1)));
    }

    public void selectTrack(int index) {
        if (index < 0 || index >= score.trackCount()) {
            throw new IllegalArgumentException("track fuera de rango: " + index);
        }
        moveCursor(clampedCursorIn(score, index));
    }

    public void renameTrack(int index, String name) {
        changeTrack(index, track -> track.withName(name));
    }

    public void setProgram(int index, int program) {
        changeChannel(index, channel -> channel.withProgram(program));
    }

    public void setVolume(int index, int volume) {
        changeChannel(index, channel -> channel.withVolume(volume));
    }

    public void setPan(int index, int pan) {
        changeChannel(index, channel -> channel.withPan(pan));
    }

    public void toggleMute(int index) {
        changeChannel(index, Channel::toggledMute);
    }

    public void toggleSolo(int index) {
        changeChannel(index, Channel::toggledSolo);
    }

    private void changeChannel(int index, UnaryOperator<Channel> howToChange) {
        changeTrack(index, track -> track.withChannel(howToChange.apply(track.channel())));
    }

    private void changeTrack(int index, UnaryOperator<Track> howToChange) {
        change(score.withTrack(index, howToChange.apply(score.track(index))), cursor);
    }

    private Track paddedToScoreLength(Track track) {
        Track padded = track;
        while (padded.measureCount() < score.measureCount()) {
            Measure empty = Measure.empty(
                    padded.measure(padded.measureCount() - 1).timeSignature(), Duration.quarter());
            padded = padded.withMeasureInsertedAt(padded.measureCount(), empty);
        }
        return padded;
    }

    private Cursor clampedCursorIn(Score next, int trackIndex) {
        Track track = next.track(trackIndex);
        int measure = Math.min(cursor.measure(), track.measureCount() - 1);
        int beat = Math.min(cursor.beat(), track.measure(measure).beats().size() - 1);
        int string = Math.min(cursor.string(), track.tuning().stringCount());
        return new Cursor(trackIndex, measure, beat, string);
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

    public void moveUp() {
        moveCursor(cursorAt(cursor.measure(), cursor.beat(), Math.max(1, cursor.string() - 1)));
    }

    public void moveLeft() {
        if (cursor.beat() > 0) {
            moveCursor(cursorAt(cursor.measure(), cursor.beat() - 1, cursor.string()));
            return;
        }
        if (cursor.measure() > 0) {
            int previousMeasure = cursor.measure() - 1;
            int lastBeat = currentTrack().measure(previousMeasure).beats().size() - 1;
            moveCursor(cursorAt(previousMeasure, lastBeat, cursor.string()));
        }
    }

    public void moveRight() {
        Measure measure = currentMeasure();
        if (cursor.beat() + 1 < measure.beats().size()) {
            moveCursor(cursorAt(cursor.measure(), cursor.beat() + 1, cursor.string()));
            return;
        }
        if (measure.durationTicks() < measure.timeSignature().ticksPerMeasure()) {
            Beat rest = Beat.rest(currentBeat().duration());
            int newBeat = measure.beats().size();
            Measure updated = measure.withBeatInsertedAt(newBeat, rest);
            change(withCurrentMeasure(updated), cursorAt(cursor.measure(), newBeat, cursor.string()));
            return;
        }
        Track track = currentTrack();
        if (cursor.measure() + 1 < track.measures().size()) {
            moveCursor(cursorAt(cursor.measure() + 1, 0, cursor.string()));
            return;
        }
        int newMeasure = track.measureCount();
        change(score.withMeasureInsertedInEveryTrackAt(newMeasure), cursorAt(newMeasure, 0, cursor.string()));
    }

    public void moveToMeasureStart() {
        moveCursor(cursorAt(cursor.measure(), 0, cursor.string()));
    }

    public void moveToMeasureEnd() {
        int lastBeat = currentMeasure().beats().size() - 1;
        moveCursor(cursorAt(cursor.measure(), lastBeat, cursor.string()));
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void undo() {
        if (undoStack.isEmpty()) {
            return;
        }
        Snapshot snapshot = undoStack.pop();
        redoStack.push(new Snapshot(score, cursor));
        score = snapshot.score();
        cursor = snapshot.cursor();
        notifyListeners();
    }

    public void redo() {
        if (redoStack.isEmpty()) {
            return;
        }
        Snapshot snapshot = redoStack.pop();
        undoStack.push(new Snapshot(score, cursor));
        score = snapshot.score();
        cursor = snapshot.cursor();
        notifyListeners();
    }

    public void replaceScore(Score score) {
        this.score = score;
        this.cursor = new Cursor(0, 0, 0, 1);
        undoStack.clear();
        redoStack.clear();
        notifyListeners();
    }

    public void addListener(EditorListener listener) {
        listeners.add(listener);
    }

    private Cursor cursorAt(int measure, int beat, int string) {
        return new Cursor(cursor.track(), measure, beat, string);
    }

    public Track currentTrack() {
        return score.track(cursor.track());
    }

    private Measure currentMeasure() {
        return currentTrack().measure(cursor.measure());
    }

    private Score withCurrentBeat(Beat beat) {
        return withCurrentMeasure(currentMeasure().withBeat(cursor.beat(), beat));
    }

    private Score withCurrentMeasure(Measure measure) {
        return withCurrentTrack(currentTrack().withMeasure(cursor.measure(), measure));
    }

    private Score withCurrentTrack(Track track) {
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
        notifyListeners();
    }

    private void moveCursor(Cursor next) {
        cursor = next;
        notifyListeners();
    }

    private void notifyListeners() {
        for (EditorListener listener : listeners) {
            listener.editorChanged();
        }
    }

    private record Snapshot(Score score, Cursor cursor) {
    }
}
