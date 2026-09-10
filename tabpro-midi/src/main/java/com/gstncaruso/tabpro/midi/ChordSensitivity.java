package com.gstncaruso.tabpro.midi;

public final class ChordSensitivity {

    private final int sensitivityMillis;
    private boolean hasAPreviousNote;
    private long lastNoteAtMillis;

    public ChordSensitivity(int sensitivityMillis) {
        this.sensitivityMillis = sensitivityMillis;
    }

    public boolean sameChordAt(long nowMillis) {
        boolean sameChord = hasAPreviousNote && (nowMillis - lastNoteAtMillis) <= sensitivityMillis;
        hasAPreviousNote = true;
        lastNoteAtMillis = nowMillis;
        return sameChord;
    }
}
