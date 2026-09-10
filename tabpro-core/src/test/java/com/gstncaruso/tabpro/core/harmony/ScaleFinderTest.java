package com.gstncaruso.tabpro.core.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScaleFinderTest {

    private static final List<Pitch> C_MAJOR =
            List.of(60, 62, 64, 65, 67, 69, 71).stream().map(Pitch::new).toList();

    @Test
    void anEmptyListHasNoCandidates() {
        assertTrue(ScaleFinder.find(List.of()).isEmpty());
    }

    @Test
    void aDiatonicMelodyFindsItsScaleWithoutIncidents() {
        List<ScaleMatch> candidates = ScaleFinder.find(C_MAJOR);

        assertEquals(0, candidates.get(0).incidentNotes());
        assertTrue(candidates.stream()
                .anyMatch(m -> m.incidentNotes() == 0
                        && m.tonic().equals(PitchClass.of("C"))
                        && m.scale().equals(ScaleLibrary.major())));
    }

    @Test
    void areOrderedByIncreasingIncidents() {
        List<ScaleMatch> candidates = ScaleFinder.find(C_MAJOR);
        for (int i = 1; i < candidates.size(); i++) {
            assertTrue(candidates.get(i - 1).incidentNotes() <= candidates.get(i).incidentNotes());
        }
    }

    @Test
    void aForeignNoteCountsAsAnIncident() {
        List<Pitch> withAStrangeNote = new java.util.ArrayList<>(C_MAJOR);
        withAStrangeNote.add(new Pitch(61));

        List<ScaleMatch> candidates = ScaleFinder.find(withAStrangeNote);

        ScaleMatch cMajorMatch = candidates.stream()
                .filter(m -> m.tonic().equals(PitchClass.of("C")) && m.scale().equals(ScaleLibrary.major()))
                .findFirst()
                .orElseThrow();
        assertEquals(1, cMajorMatch.incidentNotes());
    }

    @Test
    void searchesOverARangeOfBarsInATrack() {
        Beat beat = Beat.of(
                Duration.quarter(),
                new Note(1, 0),
                new Note(1, 2),
                new Note(1, 4),
                new Note(1, 5),
                new Note(1, 7));
        Measure bar = new Measure(TimeSignature.fourFour(), List.of(beat));
        Track track = Track.standardGuitar("Guitarra").withMeasures(List.of(bar));

        List<ScaleMatch> candidates = ScaleFinder.findIn(track, 0, 0);

        assertEquals(0, candidates.get(0).incidentNotes());
    }
}
