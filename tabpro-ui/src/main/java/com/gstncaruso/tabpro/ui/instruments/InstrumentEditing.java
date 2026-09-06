package com.gstncaruso.tabpro.ui.instruments;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.playback.Player;

/**
 * Lo que se toca en el diapason o en el teclado se escribe en el beat donde esta el cursor, y suena.
 */
public final class InstrumentEditing {

    private final Editor editor;
    private final Player player;

    public InstrumentEditing(Editor editor, Player player) {
        this.editor = editor;
        this.player = player;
    }

    /** Un traste del diapason: se escribe en su cuerda, y el cursor se muda ahi. */
    public void pressFret(Note note) {
        Cursor cursor = editor.cursor();
        editor.moveTo(cursor.measure(), cursor.beat(), note.string());
        editor.setFret(note.fret());
        sound(note);
    }

    /**
     * Clic sobre un traste: si esa nota ya esta sonando en el beat, la borra; si no,
     * la agrega. Asi el mismo clic sirve para escribir y para deshacer.
     */
    public void toggleFret(Note note) {
        if (isAlreadyThere(note)) {
            Cursor cursor = editor.cursor();
            editor.moveTo(cursor.measure(), cursor.beat(), note.string());
            editor.clearNote();
            return;
        }
        pressFret(note);
    }

    /** Clic derecho: agrega la nota y avanza al beat siguiente, como aconseja el manual. */
    public void pressFretAndAdvance(Note note) {
        pressFret(note);
        editor.moveRight();
    }

    private boolean isAlreadyThere(Note note) {
        return editor.currentBeat().noteOn(note.string())
                .map(existing -> existing.fret() == note.fret())
                .orElse(false);
    }

    /** Una tecla del teclado: se escribe en la cuerda del cursor, si esa cuerda llega. */
    public void pressKey(int midiNumber) {
        tuning().noteFor(new Pitch(midiNumber), editor.cursor().string()).ifPresent(this::pressFret);
    }

    /** Clic sobre una tecla: agrega o borra, igual que en el diapason. */
    public void toggleKey(int midiNumber) {
        tuning().noteFor(new Pitch(midiNumber), editor.cursor().string()).ifPresent(this::toggleFret);
    }

    /** Clic derecho sobre una tecla: agrega y avanza al beat siguiente. */
    public void pressKeyAndAdvance(int midiNumber) {
        tuning().noteFor(new Pitch(midiNumber), editor.cursor().string()).ifPresent(this::pressFretAndAdvance);
    }

    private void sound(Note note) {
        player.playNote(tuning().pitchOf(note), editor.currentTrack().channel().program());
    }

    private Tuning tuning() {
        return editor.currentTrack().tuning();
    }
}
