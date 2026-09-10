package com.gstncaruso.tabpro.core.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.DiagramPlacement;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import java.util.List;
import org.junit.jupiter.api.Test;

class TrackChordsTest {

    private static final ChordDiagram AM = ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1));
    private static final ChordDiagram C = ChordDiagram.named("C", List.of(0, 1, 0, 2, 3, -1));

    @Test
    void aTrackWithoutChordsHasNone() {
        Track track = Track.standardGuitar("Guitar");
        assertTrue(TrackChords.usedIn(track).isEmpty());
    }

    @Test
    void listsChordsInAppearanceOrder() {
        Measure bar1 = new Measure(TimeSignature.fourFour(), List.of(beatWithChord(AM)));
        Measure bar2 = new Measure(TimeSignature.fourFour(), List.of(beatWithChord(C)));

        Track track = Track.standardGuitar("Guitar").withMeasures(List.of(bar1, bar2));

        assertEquals(List.of(AM, C), TrackChords.usedIn(track));
    }

    @Test
    void doesNotRepeatTheSameChordTwice() {
        Measure bar1 = new Measure(TimeSignature.fourFour(), List.of(beatWithChord(AM), beatWithChord(AM)));

        Track track = Track.standardGuitar("Guitar").withMeasures(List.of(bar1));

        assertEquals(List.of(AM), TrackChords.usedIn(track));
    }

    @Test
    void underTheTitleIgnoresTracksThatDidNotAskForIt() {
        Measure bar1 = new Measure(TimeSignature.fourFour(), List.of(beatWithChord(AM)));
        Track track = Track.standardGuitar("Guitar").withMeasures(List.of(bar1));
        Score score = new Score("", 120, List.of(track));

        assertTrue(TrackChords.underTheTitle(score).isEmpty());
    }

    @Test
    void underTheTitleBringsChordsFromTheTrackThatAskedForIt() {
        Measure bar1 = new Measure(TimeSignature.fourFour(), List.of(beatWithChord(AM)));
        Measure bar2 = new Measure(TimeSignature.fourFour(), List.of(beatWithChord(C)));
        Track track = Track.standardGuitar("Guitar")
                .withMeasures(List.of(bar1, bar2))
                .mappingSettings(settings -> settings.withDisplay(
                        settings.display().withDiagrams(DiagramPlacement.UNDER_THE_TITLE)));
        Score score = new Score("", 120, List.of(track));

        assertEquals(List.of(AM, C), TrackChords.underTheTitle(score));
    }

    @Test
    void underTheTitleAlsoAppliesToThePlacementOnBothSides() {
        Measure bar1 = new Measure(TimeSignature.fourFour(), List.of(beatWithChord(AM)));
        Track track = Track.standardGuitar("Guitar")
                .withMeasures(List.of(bar1))
                .mappingSettings(settings -> settings.withDisplay(
                        settings.display().withDiagrams(DiagramPlacement.BOTH)));
        Score score = new Score("", 120, List.of(track));

        assertEquals(List.of(AM), TrackChords.underTheTitle(score));
    }

    @Test
    void underTheTitleMergesTracksInOrderAndDoesNotRepeatTheSameName() {
        Measure barWithAm = new Measure(TimeSignature.fourFour(), List.of(beatWithChord(AM)));
        Measure barWithC = new Measure(TimeSignature.fourFour(), List.of(beatWithChord(C)));
        Track firstTrack = Track.standardGuitar("Guitar 1")
                .withMeasures(List.of(barWithAm))
                .mappingSettings(settings -> settings.withDisplay(
                        settings.display().withDiagrams(DiagramPlacement.UNDER_THE_TITLE)));
        Track secondTrack = Track.standardGuitar("Guitar 2")
                .withMeasures(List.of(barWithAm, barWithC))
                .mappingSettings(settings -> settings.withDisplay(
                        settings.display().withDiagrams(DiagramPlacement.UNDER_THE_TITLE)));
        Score score = new Score("", 120, List.of(firstTrack, secondTrack));

        assertEquals(List.of(AM, C), TrackChords.underTheTitle(score));
    }

    private static Beat beatWithChord(ChordDiagram chord) {
        return Beat.rest(Duration.quarter()).withEffects(BeatEffects.none().withChord(chord));
    }
}
