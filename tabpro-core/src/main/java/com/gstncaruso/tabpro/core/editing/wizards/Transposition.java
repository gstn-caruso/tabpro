package com.gstncaruso.tabpro.core.editing.wizards;

import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import java.util.Optional;

/**
 * Sube o baja la partitura la cantidad de semitonos que se pida. No transpone
 * los diagramas de acordes, como aclara el manual.
 */
public final class Transposition {

    private Transposition() {
    }

    public static Score transposeTrack(Score score, int trackIndex, int semitones) {
        return score.mappingTrack(trackIndex, track -> transpose(track, semitones));
    }

    public static Score transposeEveryTrack(Score score, int semitones) {
        Score transposed = score;
        for (int index = 0; index < score.trackCount(); index++) {
            transposed = transposeTrack(transposed, index, semitones);
        }
        return transposed;
    }

    private static Track transpose(Track track, int semitones) {
        if (track.isPercussion()) {
            return track;
        }
        return Wizards.mappingNotes(track, note -> relocate(track.tuning(), note, semitones));
    }

    /** Busca la misma cuerda; si la nota se sale del diapason, la muda a otra. */
    private static Note relocate(Tuning tuning, Note note, int semitones) {
        int fret = note.fret() + semitones;
        if (fret >= 0 && fret <= Tuning.MAX_FRET) {
            return note.withFret(fret);
        }
        Optional<Note> elsewhere = tuning.bestNoteFor(
                tuning.pitchOf(note).transposed(semitones), Tuning.MAX_FRET);
        return elsewhere.map(moved -> moved.withEffects(note.effects()).tied(note.tied())).orElse(note);
    }
}
