package com.gstncaruso.tabpro.format.exchange.musicxml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.Tuplet;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MusicXmlRoundTripTest {

    private final MusicXmlScoreExporter exporter = new MusicXmlScoreExporter();
    private final MusicXmlScoreImporter importer = new MusicXmlScoreImporter();

    @Test
    void writesTheTitleAndTheAuthors() {
        Score score = scoreWith(Beat.of(Duration.quarter(), new Note(1, 5)));

        String xml = exporter.toXml(score.withInfo(
                ScoreInfo.titled("My song").withMusicAuthor("Someone").withCopyright("2026")));

        assertTrue(xml.contains("<work-title>My song</work-title>"), xml);
        assertTrue(xml.contains("<creator type=\"composer\">Someone</creator>"), xml);
        assertTrue(xml.contains("<rights>2026</rights>"), xml);
    }

    @Test
    void writesTheStringAndTheFretOfEveryNote() {
        String xml = exporter.toXml(scoreWith(Beat.of(Duration.quarter(), new Note(3, 7))));

        assertTrue(xml.contains("<string>3</string><fret>7</fret>"), xml);
    }

    @Test
    void writesTheTuningOfTheTablature() {
        String xml = exporter.toXml(scoreWith(Beat.rest(Duration.quarter())));

        assertTrue(xml.contains("<staff-lines>6</staff-lines>"), xml);
        assertTrue(xml.contains("<clef number=\"2\"><sign>TAB</sign>"), xml);
    }

    @Test
    void aNoteComesBackOnTheSameStringAndFret(@TempDir Path folder) throws Exception {
        Score score = scoreWith(Beat.of(Duration.quarter(), new Note(3, 7), new Note(5, 2)));
        Path file = folder.resolve("test.musicxml");

        exporter.export(score, file);
        Score loaded = importer.importScore(file);

        Beat beat = loaded.track(0).measure(0).beat(0);
        assertEquals(2, beat.notes().size());
        assertEquals(7, beat.noteOn(3).orElseThrow().fret());
        assertEquals(2, beat.noteOn(5).orElseThrow().fret());
    }

    @Test
    void theFigureAndTheTupletComeBack(@TempDir Path folder) throws Exception {
        Beat beat = Beat.of(Duration.of(NoteValue.EIGHTH).in(Tuplet.of(3)), new Note(1, 5));
        Path file = folder.resolve("test.musicxml");

        exporter.export(scoreWith(beat), file);
        Duration loaded = importer.importScore(file).track(0).measure(0).beat(0).duration();

        assertEquals(NoteValue.EIGHTH, loaded.value());
        assertEquals(Tuplet.of(3), loaded.tuplet());
    }

    @Test
    void aRestComesBackAsARest(@TempDir Path folder) throws Exception {
        Path file = folder.resolve("test.musicxml");

        exporter.export(scoreWith(Beat.rest(new Duration(NoteValue.HALF, true))), file);
        Beat loaded = importer.importScore(file).track(0).measure(0).beat(0);

        assertTrue(loaded.isRest());
        assertEquals(NoteValue.HALF, loaded.duration().value());
        assertTrue(loaded.duration().dotted());
    }

    @Test
    void theTitleAndTheTuningComeBack(@TempDir Path folder) throws Exception {
        Score score = scoreWith(Beat.of(Duration.quarter(), new Note(1, 0)))
                .withInfo(ScoreInfo.titled("My song").withMusicAuthor("Someone"));
        Path file = folder.resolve("test.musicxml");

        exporter.export(score, file);
        Score loaded = importer.importScore(file);

        assertEquals("My song", loaded.info().title());
        assertEquals("Someone", loaded.info().musicAuthor());
        assertEquals(Tuning.standard().strings(), loaded.track(0).tuning().strings());
    }

    @Test
    void aFileThatIsNotMusicXmlIsReported(@TempDir Path folder) throws Exception {
        Path file = folder.resolve("broken.musicxml");
        Files.writeString(file, "this is not xml");

        org.junit.jupiter.api.Assertions.assertThrows(
                com.gstncaruso.tabpro.core.files.ScoreFileException.class, () -> importer.importScore(file));
    }

    @Test
    void aKeySignatureChangeInTheMiddleOfThePieceSurvivesTheRoundTrip(@TempDir Path folder) throws Exception {
        Score score = scoreWithMeasures(measureInC(), measureInC(), measureInC())
                .withKeySignatureFrom(2, new KeySignature(2, Mode.MAJOR));
        Path file = folder.resolve("test.musicxml");

        exporter.export(score, file);
        Score loaded = importer.importScore(file);

        assertEquals(0, loaded.attributesOf(0).keySignature().accidentals(), "measure 1 is still in C major");
        assertEquals(0, loaded.attributesOf(1).keySignature().accidentals(), "measure 2 is still in C major");
        assertEquals(2, loaded.attributesOf(2).keySignature().accidentals(), "measure 3 changes to D major");
    }

    @Test
    void aTimeSignatureChangeInTheMiddleOfThePieceSurvivesTheRoundTrip(@TempDir Path folder) throws Exception {
        Score score = scoreWithMeasures(measureInC(), measureInC(), measureInC())
                .withTimeSignatureFrom(2, new TimeSignature(3, 4));
        Path file = folder.resolve("test.musicxml");

        exporter.export(score, file);
        Score loaded = importer.importScore(file);

        assertEquals(new TimeSignature(4, 4), loaded.timeSignatureOf(0), "measure 1 is still in 4/4");
        assertEquals(new TimeSignature(4, 4), loaded.timeSignatureOf(1), "measure 2 is still in 4/4");
        assertEquals(new TimeSignature(3, 4), loaded.timeSignatureOf(2), "measure 3 changes to 3/4");
    }

    private static Score scoreWith(Beat... beats) {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beats));
        Track track = new Track("Guitar", Tuning.standard(), Channel.playing(25), List.of(measure));
        return new Score("Test", 120, List.of(track));
    }

    private static Score scoreWithMeasures(Measure... measures) {
        Track track = new Track("Guitar", Tuning.standard(), Channel.playing(25), List.of(measures));
        return new Score("Test", 120, List.of(track));
    }

    private static Measure measureInC() {
        return new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.quarter(), new Note(1, 0))));
    }
}
