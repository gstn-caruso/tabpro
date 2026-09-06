package com.gstncaruso.tabpro.midi;

/**
 * La sensibilidad de "Enter Notes > Using a MIDI Instrument": cuanto puede
 * tardar la segunda nota de un acorde antes de que la captura la mande a
 * un beat nuevo en vez de sumarla al acorde que se esta tocando.
 */
public final class ChordSensitivity {

    private final int sensitivityMillis;
    private boolean hasAPreviousNote;
    private long lastNoteAtMillis;

    public ChordSensitivity(int sensitivityMillis) {
        this.sensitivityMillis = sensitivityMillis;
    }

    /** Si la nota que llega en ese instante cae en el mismo acorde que la anterior. */
    public boolean sameChordAt(long nowMillis) {
        boolean sameChord = hasAPreviousNote && (nowMillis - lastNoteAtMillis) <= sensitivityMillis;
        hasAPreviousNote = true;
        lastNoteAtMillis = nowMillis;
        return sameChord;
    }
}
