package com.gstncaruso.tabpro.format.exchange.musicxml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuplet;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class MusicXmlForeignFixtureImportTest {

    private final MusicXmlScoreImporter importer = new MusicXmlScoreImporter();

    private Score importFixture(String name) {
        try {
            Path path = Path.of(MusicXmlForeignFixtureImportTest.class
                    .getResource("/musicxml/" + name + ".musicxml").toURI());
            return importer.importScore(path);
        } catch (java.net.URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void theFileKeySignatureIsRespected() {
        Score score = importFixture("armadura-en-fa");

        assertEquals(-1, score.attributesOf(0).keySignature().accidentals(),
                "F major is 1 flat (fifths=-1), not the default C major key signature");

        Track track = score.track(0);
        Measure measure = track.measure(0);
        List<Beat> beats = measure.beats();
        assertEquals(65, track.pitchOf(beats.get(0).notes().get(0)).midiNumber(), "F4");
        assertEquals(67, track.pitchOf(beats.get(1).notes().get(0)).midiNumber(), "G4");
        assertEquals(69, track.pitchOf(beats.get(2).notes().get(0)).midiNumber(), "A4");
        assertEquals(70, track.pitchOf(beats.get(3).notes().get(0)).midiNumber(), "Bb4, with its explicit alteration");
    }

    @Test
    void aFullBarRestWithoutTypeFillsTheWholeBar() {
        Score score = importFixture("silencio-de-compas-completo");

        Measure measure = score.track(0).measure(0);
        Beat beat = measure.beat(0);

        assertTrue(beat.isRest(), "the only note of the measure is <rest measure=\"yes\"/>");
        assertTrue(measure.isComplete(),
                "a whole-measure rest in 3/4 must occupy all 3 beats, not a default 1 quarter note");
        assertFalse(measure.isTooShort());
    }

    @Test
    void theTieAndTheDotCrossTheBar() {
        Score score = importFixture("ligadura-entre-compases");
        Track track = score.track(0);

        Beat firstBar = track.measure(0).beat(0);
        assertFalse(firstBar.notes().get(0).tied(), "the attacking note is not marked as tied");
        assertEquals(NoteValue.QUARTER, firstBar.duration().value());
        assertTrue(firstBar.duration().dotted(), "quarter+dot in the file is a dotted quarter note");

        Beat secondBar = track.measure(1).beat(0);
        assertTrue(secondBar.notes().get(0).tied(), "tie type=\"stop\" is the continuation, not a new attack");
        assertEquals(NoteValue.QUARTER, secondBar.duration().value());
        assertFalse(secondBar.duration().dotted());
    }

    @Test
    void theEighthNoteTripletIsReadWithForeignDivisions() {
        Score score = importFixture("tresillo-de-corcheas");
        Measure measure = score.track(0).measure(0);

        for (int i = 0; i < 3; i++) {
            Beat beat = measure.beat(i);
            assertEquals(NoteValue.EIGHTH, beat.duration().value(), "beat " + i);
            assertEquals(Tuplet.of(3), beat.duration().tuplet(), "beat " + i + ": 3 in the time of 2");
        }
        assertTrue(measure.isComplete(), "the triplet plus the three quarter notes must complete the 4/4");
    }

    @Test
    void theTablatureInDropDDoesNotUseStandardTuning() {
        Score score = importFixture("tablatura-en-drop-d");
        Track track = score.track(0);

        assertEquals(List.of(64, 59, 55, 50, 45, 38),
                track.tuning().strings().stream().map(Pitch::midiNumber).toList(),
                "staff-tuning listed from line 1 to 6 must build Drop D, not the standard tuning");

        Beat firstBeat = track.measure(0).beat(0);
        assertEquals(4, firstBeat.notes().get(0).string(),
                "the file places the note explicitly on string 4; recalculating the best string would put it on string 3");
        assertEquals(7, firstBeat.notes().get(0).fret());

        Beat chordBeat = track.measure(0).beat(1);
        assertEquals(2, chordBeat.notes().size(), "the chord/ adds the second note to the same beat");
        assertEquals(0, chordBeat.noteOn(3).orElseThrow().fret());
        assertEquals(0, chordBeat.noteOn(4).orElseThrow().fret());

        Beat beatWithoutTechnical = track.measure(0).beat(2);
        assertEquals(6, beatWithoutTechnical.notes().get(0).string(),
                "without <technical>, D2 can only resolve open on string 6 of Drop D");
        assertEquals(0, beatWithoutTechnical.notes().get(0).fret());
    }
}
