package com.gstncaruso.tabpro.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

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

    public int trackCount() {
        return tracks.size();
    }

    public int measureCount() {
        return tracks.stream().mapToInt(Track::measureCount).max().orElse(0);
    }

    public boolean isAudible(int index) {
        Channel channel = track(index).channel();
        if (channel.isSilent()) {
            return false;
        }
        return !anyTrackPlaysSolo() || channel.solo();
    }

    public Score withTrack(int index, Track track) {
        List<Track> updated = new ArrayList<>(tracks);
        updated.set(index, track);
        return withTracks(updated);
    }

    public Score withTrackAdded(Track track) {
        List<Track> updated = new ArrayList<>(tracks);
        updated.add(track);
        return withTracks(updated);
    }

    public Score withoutTrackAt(int index) {
        if (tracks.size() == 1) {
            throw new IllegalStateException("una partitura necesita al menos una pista");
        }
        List<Track> updated = new ArrayList<>(tracks);
        updated.remove(index);
        return withTracks(updated);
    }

    public Score withMeasureInsertedInEveryTrackAt(int index) {
        return mapTracks(track -> {
            int insertionPoint = Math.min(index, track.measureCount());
            Measure empty = Measure.empty(timeSignatureAround(track, insertionPoint), Duration.quarter());
            return track.withMeasureInsertedAt(insertionPoint, empty);
        });
    }

    public Score withoutMeasureInEveryTrackAt(int index) {
        return mapTracks(track ->
                index < track.measureCount() ? track.withoutMeasureAt(index) : track);
    }

    private static TimeSignature timeSignatureAround(Track track, int index) {
        int reference = Math.min(index, track.measureCount() - 1);
        return track.measure(reference).timeSignature();
    }

    private Score mapTracks(UnaryOperator<Track> change) {
        return withTracks(tracks.stream().map(change).toList());
    }

    public Score withTempo(int bpm) {
        return new Score(title, bpm, tracks);
    }

    public Score withTitle(String title) {
        return new Score(title, tempo, tracks);
    }

    private boolean anyTrackPlaysSolo() {
        return tracks.stream().anyMatch(track -> track.channel().solo());
    }

    private Score withTracks(List<Track> updated) {
        return new Score(title, tempo, updated);
    }
}
