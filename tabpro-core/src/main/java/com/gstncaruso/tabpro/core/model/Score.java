package com.gstncaruso.tabpro.core.model;

import java.util.ArrayList;
import java.util.List;

public record Score(String title, int tempo, List<Track> tracks) {

    public Score {
        if (tempo <= 0) {
            throw new IllegalArgumentException("tempo debe ser > 0: " + tempo);
        }
        if (tracks.isEmpty()) {
            throw new IllegalArgumentException("una partitura necesita al menos una pista");
        }
        tracks = List.copyOf(tracks);
    }

    public static Score blank() {
        return new Score("", 120, List.of(Track.standardGuitar("Guitarra")));
    }

    public Track track(int index) {
        return tracks.get(index);
    }

    public Score withTrack(int index, Track track) {
        List<Track> updated = new ArrayList<>(tracks);
        updated.set(index, track);
        return new Score(title, tempo, updated);
    }

    public Score withTempo(int bpm) {
        return new Score(title, bpm, tracks);
    }

    public Score withTitle(String title) {
        return new Score(title, tempo, tracks);
    }
}
