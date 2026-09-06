package com.gstncaruso.tabpro.core.editing;

import com.gstncaruso.tabpro.core.model.Score;
import java.util.ArrayDeque;
import java.util.Deque;

/** Lo que hace posible deshacer y rehacer: la partitura tal como estaba. */
final class EditorHistory {

    private final Deque<Snapshot> past = new ArrayDeque<>();
    private final Deque<Snapshot> future = new ArrayDeque<>();

    void remember(Snapshot snapshot) {
        past.push(snapshot);
        future.clear();
    }

    boolean canUndo() {
        return !past.isEmpty();
    }

    boolean canRedo() {
        return !future.isEmpty();
    }

    Snapshot undo(Snapshot current) {
        Snapshot restored = past.pop();
        future.push(current);
        return restored;
    }

    Snapshot redo(Snapshot current) {
        Snapshot restored = future.pop();
        past.push(current);
        return restored;
    }

    void forget() {
        past.clear();
        future.clear();
    }

    record Snapshot(Score score, Cursor cursor) {
    }
}
