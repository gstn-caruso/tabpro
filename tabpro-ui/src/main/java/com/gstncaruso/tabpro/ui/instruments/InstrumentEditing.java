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

    /** Una tecla del teclado: se escribe en la cuerda del cursor, si esa cuerda llega. */
    public void pressKey(int midiNumber) {
        tuning().noteFor(new Pitch(midiNumber), editor.cursor().string()).ifPresent(this::pressFret);
    }

    private void sound(Note note) {
        player.playNote(tuning().pitchOf(note), editor.currentTrack().channel().program());
    }

    private Tuning tuning() {
        return editor.currentTrack().tuning();
    }
}
