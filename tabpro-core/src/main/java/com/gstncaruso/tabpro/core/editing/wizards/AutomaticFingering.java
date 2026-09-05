package com.gstncaruso.tabpro.core.editing.wizards;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Reubica las notas en el diapason sin cambiar la melodia, para que la mano
 * viaje lo menos posible entre un beat y el siguiente.
 */
public final class AutomaticFingering {

    /** Lo que abarca la mano sin desplazarse. */
    private static final int HAND_SPAN = 4;

    private AutomaticFingering() {
    }

    public static Score run(Score score, int trackIndex) {
        return score.mappingTrack(trackIndex, AutomaticFingering::reposition);
    }

    private static Track reposition(Track track) {
        if (track.isPercussion()) {
            return track;
        }
        int[] hand = {0};
        return Wizards.mappingBeats(track, beat -> repositioned(track, beat, hand));
    }

    private static Beat repositioned(Track track, Beat beat, int[] hand) {
        if (beat.isRest()) {
            return beat;
        }
        List<Note> placed = new ArrayList<>();
        List<Integer> used = new ArrayList<>();
        for (Note note : beat.notes()) {
            placed.add(closestTo(track.tuning(), note, hand[0], used));
        }
        placed.stream().mapToInt(Note::fret).filter(fret -> fret > 0).min().ifPresent(fret -> hand[0] = fret);
        return beat.withNotes(placed);
    }

    /** La misma altura, en la cuerda libre cuyo traste quede mas cerca de la mano. */
    private static Note closestTo(Tuning tuning, Note note, int hand, List<Integer> used) {
        Note best = note;
        int bestDistance = Integer.MAX_VALUE;
        for (int string = 1; string <= tuning.stringCount(); string++) {
            if (used.contains(string)) {
                continue;
            }
            Optional<Note> candidate = tuning.noteFor(tuning.pitchOf(note), string);
            if (candidate.isEmpty()) {
                continue;
            }
            int distance = distanceFrom(hand, candidate.get().fret());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate.get().withEffects(note.effects()).tied(note.tied());
            }
        }
        used.add(best.string());
        return best;
    }

    private static int distanceFrom(int hand, int fret) {
        if (fret == 0) {
            return 0;
        }
        if (fret >= hand && fret < hand + HAND_SPAN) {
            return 1;
        }
        return 2 + Math.abs(fret - hand);
    }
}
