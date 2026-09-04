package com.gstncaruso.tabpro.core.editing;

import java.util.ArrayDeque;
import java.util.Deque;
import com.gstncaruso.tabpro.core.model.Score;

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

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    private record Snapshot(Score score, Cursor cursor) {
    }
}
