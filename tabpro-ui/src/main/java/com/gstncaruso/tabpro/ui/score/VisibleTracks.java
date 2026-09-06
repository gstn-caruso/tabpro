package com.gstncaruso.tabpro.ui.score;

import java.util.HashSet;
import java.util.Set;

/**
 * Que pistas se dibujan. En la vista multipista se ven todas menos las que se apagaron en la
 * mesa de mezcla; con la vista multipista apagada se ve solo la pista activa. La pista activa
 * se ve siempre, aunque este apagada, porque es la que se esta editando.
 */
public record VisibleTracks(boolean multitrack, int activeTrack, Set<Integer> turnedOff) {

    public VisibleTracks {
        turnedOff = Set.copyOf(turnedOff);
    }

    public static VisibleTracks all() {
        return new VisibleTracks(true, 0, Set.of());
    }

    /** Si la pista esta prendida, sin importar si es la que se esta editando. */
    public boolean isTurnedOn(int track) {
        return !turnedOff.contains(track);
    }

    public boolean shows(int track) {
        if (track == activeTrack) {
            return true;
        }
        return multitrack && isTurnedOn(track);
    }

    public VisibleTracks withMultitrack(boolean multitrack) {
        return new VisibleTracks(multitrack, activeTrack, turnedOff);
    }

    public VisibleTracks withActiveTrack(int activeTrack) {
        return new VisibleTracks(multitrack, activeTrack, turnedOff);
    }

    public VisibleTracks withTrackShown(int track, boolean shown) {
        Set<Integer> next = new HashSet<>(turnedOff);
        if (shown) {
            next.remove(track);
        } else {
            next.add(track);
        }
        return new VisibleTracks(multitrack, activeTrack, next);
    }
}
