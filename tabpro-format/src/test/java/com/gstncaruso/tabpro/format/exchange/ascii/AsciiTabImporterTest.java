package com.gstncaruso.tabpro.format.exchange.ascii;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFileProblem;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AsciiTabImporterTest {

    private final AsciiTabImporter importer = new AsciiTabImporter();

    @Test
    void ignoresCommentsAroundTheTabAndReadsTwoNotesWithAFixedRhythm() {
        String block = block(6, "--5--0--");
        String text = "A comment before.\n\n" + block + "\nComment after.\n";

        Score score = importer.importScore(text, AsciiTabImportOptions.standard());

        Track track = score.track(0);
        assertEquals(Tuning.standard(), track.tuning());
        Beat first = track.measure(0).beat(0);
        Beat second = track.measure(0).beat(1);
        assertEquals(new Duration(NoteValue.EIGHTH, false), first.duration());
        assertEquals(List.of(new Note(1, 5)), first.notes());
        assertEquals(List.of(new Note(1, 0)), second.notes());
    }

    @Test
    void readsMultiDigitFrets() {
        String text = block(6, "-12------");

        Score score = importer.importScore(text, AsciiTabImportOptions.standard());

        assertEquals(12, track(score).measure(0).beat(0).notes().get(0).fret());
    }

    @Test
    void infersDurationFromTheSpacingBetweenColumns() {
        String text = block(6, "--5--0--");

        Score score = importer.importScore(text, AsciiTabImportOptions.standard().withRhythm(RhythmStrategy.fromSpacing(2)));

        List<Beat> beats = track(score).measure(0).beats();
        assertEquals(new Duration(NoteValue.QUARTER, false), beats.get(0).duration());
        assertEquals(List.of(new Note(1, 5)), beats.get(1).notes());
        assertEquals(new Duration(NoteValue.QUARTER, true).ticks(), beats.get(1).duration().ticks());
        assertEquals(List.of(new Note(1, 0)), beats.get(2).notes());
    }

    @Test
    void aDifferentNumberOfIntervalsPerQuarterNoteInfersADifferentRhythmFromTheSameSpacing() {
        String text = block(6, "--5--0--");

        Score coarse = importer.importScore(text, AsciiTabImportOptions.standard().withRhythm(RhythmStrategy.fromSpacing(2)));
        Score fine = importer.importScore(text, AsciiTabImportOptions.standard().withRhythm(RhythmStrategy.fromSpacing(4)));

        Beat coarseNote = track(coarse).measure(0).beats().get(1);
        Beat fineNote = track(fine).measure(0).beats().get(1);
        assertEquals(new Duration(NoteValue.QUARTER, true).ticks(), coarseNote.duration().ticks());
        assertEquals(new Duration(NoteValue.EIGHTH, true).ticks(), fineNote.duration().ticks());
    }

    @Test
    void mergesConsecutiveBlocksWithTheSameStringCountIntoOneTrack() {
        String text = block(6, "-5-") + "\n" + block(6, "-0-");

        Score score = importer.importScore(text, AsciiTabImportOptions.standard());

        assertEquals(1, score.trackCount());
        assertEquals(2, score.track(0).measureCount());
    }

    @Test
    void startsANewTrackWhenTheStringCountChanges() {
        String text = block(6, "-5-") + "\n" + block(4, "-3-");

        Score score = importer.importScore(text, AsciiTabImportOptions.standard());

        assertEquals(2, score.trackCount());
        assertEquals(6, score.track(0).stringCount());
        assertEquals(4, score.track(1).stringCount());
        assertEquals(Tuning.standardBass(), score.track(1).tuning());
    }

    @Test
    void rejectsATextWithoutAnyTab() {
        ScoreFileException failure = assertThrows(
                ScoreFileException.class, () -> importer.importScore("no tab here", AsciiTabImportOptions.standard()));

        assertEquals(ScoreFileProblem.NOTHING_TO_IMPORT, failure.problem());
    }

    @Test
    void aMissingFileIsReportedWithItsPath(@TempDir Path folder) {
        Path path = folder.resolve("missing.tab");

        ScoreFileException failure = assertThrows(
                ScoreFileException.class, () -> importer.importScore(path, AsciiTabImportOptions.standard()));

        assertEquals(ScoreFileProblem.CANNOT_READ, failure.problem());
        assertEquals(List.of(path), failure.arguments());
    }

    @Test
    void importsOntoTheActiveTrackKeepingItsIdentity() {
        String text = block(4, "-5-");
        Track existing = Track.standardBass("Active bass");

        Track merged = importer.importInto(existing, text, AsciiTabImportOptions.standard());

        assertEquals("Active bass", merged.name());
        assertEquals(Tuning.standardBass(), merged.tuning());
        assertEquals(existing.channel(), merged.channel());
        assertEquals(List.of(new Note(1, 5)), merged.measure(0).beat(0).notes());
    }

    @Test
    void mergesConsecutiveBlocksWithTheSameStringCountWhenImportingOntoATrack() {
        String text = block(6, "-5-") + "\n" + block(6, "-0-");
        Track existing = Track.standardGuitar("Active guitar");

        Track merged = importer.importInto(existing, text, AsciiTabImportOptions.standard());

        assertEquals(2, merged.measureCount());
    }

    @Test
    void onlyUsesTheFirstGroupOfBlocksWhenTheStringCountChangesWhileImportingOntoATrack() {
        String text = block(6, "-5-") + "\n" + block(4, "-3-");
        Track existing = Track.standardGuitar("Active guitar");

        Track merged = importer.importInto(existing, text, AsciiTabImportOptions.standard());

        assertEquals(1, merged.measureCount());
        assertEquals(List.of(new Note(1, 5)), merged.measure(0).beat(0).notes());
    }

    @Test
    void rejectsATextWithoutAnyTabWhenImportingOntoATrack() {
        Track existing = Track.standardGuitar("Active guitar");
        ScoreFileException failure = assertThrows(ScoreFileException.class,
                () -> importer.importInto(existing, "no tab here", AsciiTabImportOptions.standard()));

        assertEquals(ScoreFileProblem.NOTHING_TO_IMPORT, failure.problem());
    }

    private static String block(int stringCount, String firstStringContent) {
        StringBuilder text = new StringBuilder();
        text.append('|').append(firstStringContent).append('|').append('\n');
        String emptyLine = "|" + "-".repeat(firstStringContent.length()) + "|";
        for (int i = 1; i < stringCount; i++) {
            text.append(emptyLine).append('\n');
        }
        return text.toString();
    }

    private static Track track(Score score) {
        return score.track(0);
    }
}
