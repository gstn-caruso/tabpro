package com.gstncaruso.tabpro.core.editing.wizards;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class AutomaticFingering {

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

    private static Note closestTo(Tuning tuning, Note note, int hand, List<Integer> used) {
        Note best = bestFingeringFor(tuning, tuning.pitchOf(note), hand, used)
                .map(found -> found.withEffects(note.effects()).tied(note.tied()))
                .orElse(note);
        used.add(best.string());
        return best;
    }

    public static Optional<Note> bestFingeringFor(
            Tuning tuning, Pitch pitch, int hand, Collection<Integer> excludedStrings) {
        Note best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (int string = 1; string <= tuning.stringCount(); string++) {
            if (excludedStrings.contains(string)) {
                continue;
            }
            Optional<Note> candidate = tuning.noteFor(pitch, string);
            if (candidate.isEmpty()) {
                continue;
            }
            int distance = distanceFrom(hand, candidate.get().fret());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate.get();
            }
        }
        return Optional.ofNullable(best);
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
