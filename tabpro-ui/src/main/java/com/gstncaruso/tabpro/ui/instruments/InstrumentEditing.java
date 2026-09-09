package com.gstncaruso.tabpro.ui.instruments;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.editing.wizards.AutomaticFingering;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.playback.Player;
import java.util.List;
import java.util.Optional;

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
            erase(note);
            return;
        }
        pressFret(note);
    }

    /** Apaga la nota: se muda a su cuerda y la borra del beat. */
    private void erase(Note note) {
        Cursor cursor = editor.cursor();
        editor.moveTo(cursor.measure(), cursor.beat(), note.string());
        editor.clearNote();
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

    /** Una tecla del teclado: se suma al acorde del beat, en la cuerda que la deje sonar. */
    public void pressKey(int midiNumber) {
        whereItFits(new Pitch(midiNumber)).ifPresent(this::pressFret);
    }

    /** Clic sobre una tecla: si esa altura ya suena en el beat la apaga; si no, la suma al acorde. */
    public void toggleKey(int midiNumber) {
        Optional<Note> sounding = soundingAt(new Pitch(midiNumber));
        if (sounding.isPresent()) {
            erase(sounding.get());
            return;
        }
        pressKey(midiNumber);
    }

    /** Clic derecho sobre una tecla: agrega y avanza al beat siguiente. */
    public void pressKeyAndAdvance(int midiNumber) {
        whereItFits(new Pitch(midiNumber)).ifPresent(this::pressFretAndAdvance);
    }

    /**
     * Donde entra esa altura: en la cuerda del cursor si esta libre y llega, y si no en la cuerda
     * libre mas comoda para la mano, para que tocar varias teclas seguidas sume un acorde en el
     * beat en vez de pisar siempre la misma cuerda. Sin ninguna cuerda libre que llegue vuelve a
     * la del cursor, que es lo unico que queda por pisar.
     */
    private Optional<Note> whereItFits(Pitch pitch) {
        Optional<Note> onTheCursorString = tuning().noteFor(pitch, editor.cursor().string());
        if (onTheCursorString.isPresent() && editor.currentNote().isEmpty()) {
            return onTheCursorString;
        }
        return AutomaticFingering.bestFingeringFor(tuning(), pitch, handPosition(), stringsAlreadySounding())
                .or(() -> onTheCursorString);
    }

    /** La nota del beat que suena a esa altura, en la cuerda que sea. */
    private Optional<Note> soundingAt(Pitch pitch) {
        return editor.currentBeat().notes().stream()
                .filter(note -> tuning().pitchOf(note).equals(pitch))
                .findFirst();
    }

    private List<Integer> stringsAlreadySounding() {
        return editor.currentBeat().notes().stream().map(Note::string).toList();
    }

    /** Donde esta la mano: el traste de la nota del cursor, o el primero si su cuerda esta libre. */
    private int handPosition() {
        return editor.currentNote().map(Note::fret).orElse(0);
    }

    private void sound(Note note) {
        player.playNote(tuning().pitchOf(note), editor.currentTrack().channel().program());
    }

    private Tuning tuning() {
        return editor.currentTrack().tuning();
    }
}
