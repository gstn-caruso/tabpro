package com.gstncaruso.tabpro.core.harmony;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Voice;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * El "Scale Finder" del manual: dadas las notas que sonaron, que tonalidad y escala las
 * explican mejor -la que menos notas ajenas (incidencias) tiene.
 */
public final class ScaleFinder {

    private ScaleFinder() {
    }

    /** Las candidatas para esas alturas, de la que menos incidencias tiene a la que mas. */
    public static List<ScaleMatch> find(List<Pitch> pitches) {
        if (pitches.isEmpty()) {
            return List.of();
        }
        List<Integer> playedSemitones = pitches.stream().map(pitch -> Math.floorMod(pitch.midiNumber(), 12)).toList();

        List<ScaleMatch> matches = new ArrayList<>();
        for (int tonicSemitone = 0; tonicSemitone < 12; tonicSemitone++) {
            PitchClass tonic = PitchClass.fromSemitone(tonicSemitone);
            for (Scale scale : ScaleLibrary.all()) {
                Set<Integer> scalePitchClasses = new HashSet<>();
                for (int semitone : scale.semitones()) {
                    scalePitchClasses.add(Math.floorMod(tonicSemitone + semitone, 12));
                }
                long incidents = playedSemitones.stream().filter(semitone -> !scalePitchClasses.contains(semitone)).count();
                matches.add(new ScaleMatch(tonic, scale, (int) incidents));
            }
        }
        return matches.stream().sorted(Comparator.comparingInt(ScaleMatch::incidentNotes)).toList();
    }

    /** Lo mismo, pero tomando las notas de un rango de compases de una pista. */
    public static List<ScaleMatch> findIn(Track track, int fromMeasureIndex, int toMeasureIndexInclusive) {
        List<Pitch> pitches = new ArrayList<>();
        for (int index = fromMeasureIndex; index <= toMeasureIndexInclusive; index++) {
            Measure measure = track.measure(index);
            for (Voice voice : measure.voices()) {
                for (Beat beat : voice.beats()) {
                    for (Note note : beat.notes()) {
                        pitches.add(track.pitchOf(note));
                    }
                }
            }
        }
        return find(pitches);
    }
}
