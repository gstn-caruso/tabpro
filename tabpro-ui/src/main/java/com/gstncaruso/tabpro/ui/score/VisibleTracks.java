package com.gstncaruso.tabpro.ui.score;

import java.util.HashSet;
import java.util.Set;

public record VisibleTracks(boolean multitrack, int activeTrack, Set<Integer> turnedOff) {

    public VisibleTracks {
        turnedOff = Set.copyOf(turnedOff);
    }

    public static VisibleTracks all() {
        return new VisibleTracks(true, 0, Set.of());
    }

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
