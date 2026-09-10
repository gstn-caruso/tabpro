package com.gstncaruso.tabpro.core.model;

import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.LineBreak;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import com.gstncaruso.tabpro.core.model.bars.OctaveMark;
import com.gstncaruso.tabpro.core.model.bars.TripletFeel;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public record Score(ScoreInfo info, int tempo, List<Track> tracks, Lyrics lyrics) {

    public static final int MAX_TRACKS = 256;

    public Score {
        if (tempo <= 0) {
            throw new IllegalArgumentException("tempo debe ser > 0: " + tempo);
        }
        if (tracks.isEmpty()) {
            throw new IllegalArgumentException("una partitura necesita al menos una pista");
        }
        if (tracks.size() > MAX_TRACKS) {
            throw new IllegalArgumentException("una partitura admite hasta " + MAX_TRACKS + " pistas");
        }
        tracks = List.copyOf(tracks);
    }

    public Score(String title, int tempo, List<Track> tracks) {
        this(ScoreInfo.titled(title), tempo, tracks, Lyrics.none());
    }

    public static Score blank() {
        return new Score(ScoreInfo.empty(), 120, List.of(Track.standardGuitar("Guitarra")), Lyrics.none());
    }

    public String title() {
        return info.title();
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

    public MeasureAttributes attributesOf(int measureIndex) {
        Track first = track(0);
        int clamped = Math.clamp(measureIndex, 0, first.measureCount() - 1);
        return first.attributesOf(clamped);
    }

    public OptionalInt measureOfMarkerInEffectAt(int measureIndex) {
        for (int measure = measureIndex; measure >= 0; measure--) {
            if (attributesOf(measure).marker().isPresent()) {
                return OptionalInt.of(measure);
            }
        }
        return OptionalInt.empty();
    }

    public TimeSignature timeSignatureOf(int measureIndex) {
        Track first = track(0);
        int clamped = Math.clamp(measureIndex, 0, first.measureCount() - 1);
        return first.measure(clamped).timeSignature();
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

    public Score mappingTrack(int index, UnaryOperator<Track> change) {
        return withTrack(index, change.apply(track(index)));
    }

    public Score withTrackAdded(Track track) {
        return withTrackInsertedAt(tracks.size(), track);
    }

    public Score withTrackInsertedAt(int index, Track track) {
        List<Track> updated = new ArrayList<>(tracks);
        updated.add(index, track);
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

    public Score withTrackMoved(int from, int to) {
        if (to < 0 || to >= tracks.size()) {
            return this;
        }
        List<Track> updated = new ArrayList<>(tracks);
        updated.add(to, updated.remove(from));
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
        return mapTracks(track -> index < track.measureCount() ? track.withoutMeasureAt(index) : track);
    }

    public Score withAttributesInEveryTrackAt(int index, MeasureAttributes attributes) {
        return mapTracks(track -> index < track.measureCount()
                ? track.mappingMeasure(index, measure -> measure.withAttributes(attributes))
                : track);
    }

    public Score withLineBreakInTrackAt(int trackIndex, int measureIndex, LineBreak lineBreak) {
        return mappingTrack(trackIndex, track -> measureIndex < track.measureCount()
                ? track.mappingMeasure(measureIndex,
                        measure -> measure.withAttributes(measure.attributes().withLineBreak(lineBreak)))
                : track);
    }

    public Score withOctaveMarkInTrackAt(int trackIndex, int measureIndex, OctaveMark octaveMark) {
        return mappingTrack(trackIndex, track -> measureIndex < track.measureCount()
                ? track.mappingMeasure(measureIndex,
                        measure -> measure.withAttributes(measure.attributes().withOctaveMark(octaveMark)))
                : track);
    }

    public Score withTimeSignatureFrom(int index, TimeSignature timeSignature) {
        return mapTracks(track -> propagatingFrom(track, index,
                Measure::timeSignature, Measure::withTimeSignature, timeSignature));
    }

    public Score withKeySignatureFrom(int index, KeySignature keySignature) {
        return mapTracks(track -> propagatingFrom(track, index,
                measure -> measure.attributes().keySignature(),
                (measure, value) -> measure.mappingAttributes(attrs -> attrs.withKeySignature(value)),
                keySignature));
    }

    public Score withTripletFeelFrom(int index, TripletFeel tripletFeel) {
        return mapTracks(track -> propagatingFrom(track, index,
                measure -> measure.attributes().tripletFeel(),
                (measure, value) -> measure.mappingAttributes(attrs -> attrs.withTripletFeel(value)),
                tripletFeel));
    }

    private static <V> Track propagatingFrom(
            Track track, int index, Function<Measure, V> valueOf, BiFunction<Measure, V, Measure> withValue, V value) {
        Track changed = track;
        V previous = valueOf.apply(track.measure(Math.min(index, track.measureCount() - 1)));
        for (int measure = index; measure < track.measureCount(); measure++) {
            if (measure > index && !valueOf.apply(changed.measure(measure)).equals(previous)) {
                break;
            }
            changed = changed.mappingMeasure(measure, it -> withValue.apply(it, value));
        }
        return changed;
    }

    private static TimeSignature timeSignatureAround(Track track, int index) {
        int reference = Math.min(index, track.measureCount() - 1);
        return track.measure(reference).timeSignature();
    }

    private Score mapTracks(UnaryOperator<Track> change) {
        return withTracks(tracks.stream().map(change).toList());
    }

    public Score withTempo(int bpm) {
        return new Score(info, bpm, tracks, lyrics);
    }

    public Score withInfo(ScoreInfo info) {
        return new Score(info, tempo, tracks, lyrics);
    }

    public Score withTitle(String title) {
        return withInfo(info.withTitle(title));
    }

    public Score withLyrics(Lyrics lyrics) {
        return new Score(info, tempo, tracks, lyrics);
    }

    private boolean anyTrackPlaysSolo() {
        return tracks.stream().anyMatch(track -> track.channel().solo());
    }

    private Score withTracks(List<Track> updated) {
        return new Score(info, tempo, updated, lyrics);
    }
}
