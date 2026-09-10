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

public final class InstrumentEditing {

    private final Editor editor;
    private final Player player;

    public InstrumentEditing(Editor editor, Player player) {
        this.editor = editor;
        this.player = player;
    }

    public void pressFret(Note note) {
        Cursor cursor = editor.cursor();
        editor.moveTo(cursor.measure(), cursor.beat(), note.string());
        editor.setFret(note.fret());
        sound(note);
    }

    public void toggleFret(Note note) {
        if (isAlreadyThere(note)) {
            erase(note);
            return;
        }
        pressFret(note);
    }

    private void erase(Note note) {
        Cursor cursor = editor.cursor();
        editor.moveTo(cursor.measure(), cursor.beat(), note.string());
        editor.clearNote();
    }

    public void pressFretAndAdvance(Note note) {
        pressFret(note);
        editor.moveRight();
    }

    private boolean isAlreadyThere(Note note) {
        return editor.currentBeat().noteOn(note.string())
                .map(existing -> existing.fret() == note.fret())
                .orElse(false);
    }

    public void pressKey(int midiNumber) {
        whereItFits(new Pitch(midiNumber)).ifPresent(this::pressFret);
    }

    public void toggleKey(int midiNumber) {
        Optional<Note> sounding = soundingAt(new Pitch(midiNumber));
        if (sounding.isPresent()) {
            erase(sounding.get());
            return;
        }
        pressKey(midiNumber);
    }

    public void pressKeyAndAdvance(int midiNumber) {
        whereItFits(new Pitch(midiNumber)).ifPresent(this::pressFretAndAdvance);
    }

    private Optional<Note> whereItFits(Pitch pitch) {
        Optional<Note> onTheCursorString = tuning().noteFor(pitch, editor.cursor().string());
        if (onTheCursorString.isPresent() && editor.currentNote().isEmpty()) {
            return onTheCursorString;
        }
        return AutomaticFingering.bestFingeringFor(tuning(), pitch, handPosition(), stringsAlreadySounding())
                .or(() -> onTheCursorString);
    }

    private Optional<Note> soundingAt(Pitch pitch) {
        return editor.currentBeat().notes().stream()
                .filter(note -> tuning().pitchOf(note).equals(pitch))
                .findFirst();
    }

    private List<Integer> stringsAlreadySounding() {
        return editor.currentBeat().notes().stream().map(Note::string).toList();
    }

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
