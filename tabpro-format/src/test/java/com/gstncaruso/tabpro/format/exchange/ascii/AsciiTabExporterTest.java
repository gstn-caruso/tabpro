package com.gstncaruso.tabpro.format.exchange.ascii;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFileProblem;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AsciiTabExporterTest {

    private final AsciiTabExporter exporter = new AsciiTabExporter();

    @Test
    void aFileThatCannotBeWrittenIsReportedWithItsPath(@TempDir Path folder) {
        Path path = folder.resolve("missing-folder").resolve("song.tab");
        Track track = Track.standardGuitar("Guitar");

        ScoreFileException failure = assertThrows(
                ScoreFileException.class, () -> exporter.export(track, path, AsciiTabExportOptions.standard()));

        assertEquals(ScoreFileProblem.CANNOT_WRITE, failure.problem());
        assertEquals(List.of(path), failure.arguments());
    }

    @Test
    void drawsEachStringAsADashLineWithBarsAtTheEdges() {
        Measure measure = Measure.empty(TimeSignature.fourFour(), Duration.of(NoteValue.WHOLE));
        Track track = new Track("Guitar", Tuning.standard(), com.gstncaruso.tabpro.core.model.Channel.playing(25), List.of(measure));
        Score score = new Score("Test", 120, List.of(track));

        String tab = exporter.export(score, AsciiTabExportOptions.standard());

        List<String> lines = linesOf(tab);
        assertEquals(6, stringLinesOf(lines).size());
        for (String line : stringLinesOf(lines)) {
            assertTrue(line.startsWith("|"), "every string line starts with a bar: " + line);
            assertTrue(line.endsWith("|"), "every string line ends with a bar: " + line);
            assertTrue(line.chars().allMatch(c -> c == '-' || c == '|'), "only dashes and bars: " + line);
        }
    }

    @Test
    void placesEachFretUnderItsOwnString() {
        Beat beat = Beat.of(Duration.of(NoteValue.QUARTER), new Note(3, 5), new Note(6, 0));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beat, Beat.rest(new Duration(NoteValue.HALF, true))));
        Track track = new Track("Guitar", Tuning.standard(), com.gstncaruso.tabpro.core.model.Channel.playing(25), List.of(measure));
        Score score = new Score("Test", 120, List.of(track));

        String tab = exporter.export(score, AsciiTabExportOptions.standard());

        List<String> stringLines = stringLinesOf(linesOf(tab));
        assertTrue(stringLines.get(2).contains("5"), "string 3 has fret 5: " + stringLines.get(2));
        assertTrue(stringLines.get(5).contains("0"), "string 6 has fret 0: " + stringLines.get(5));
        assertTrue(stringLines.get(0).chars().noneMatch(Character::isDigit), "string 1 does not sound: " + stringLines.get(0));
    }

    @Test
    void keepsTwoDigitFretsAligned() {
        Beat beat = Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 12), new Note(6, 0));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beat, Beat.rest(new Duration(NoteValue.HALF, true))));
        Track track = new Track("Guitar", Tuning.standard(), com.gstncaruso.tabpro.core.model.Channel.playing(25), List.of(measure));
        Score score = new Score("Test", 120, List.of(track));

        String tab = exporter.export(score, AsciiTabExportOptions.standard());

        List<String> stringLines = stringLinesOf(linesOf(tab));
        assertEquals(stringLines.get(0).length(), stringLines.get(5).length());
    }

    @Test
    void wrapsIntoANewSystemWhenAMeasureWouldNotFit() {
        List<Measure> measures = Arrays.asList(
                new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.of(NoteValue.WHOLE), new Note(6, 0)))),
                new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.of(NoteValue.WHOLE), new Note(6, 1)))),
                new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.of(NoteValue.WHOLE), new Note(6, 2)))));
        Track track = new Track("Guitar", Tuning.standard(), com.gstncaruso.tabpro.core.model.Channel.playing(25), measures);
        Score score = new Score("Test", 120, List.of(track));
        AsciiTabExportOptions narrow = new AsciiTabExportOptions(20);

        String tab = exporter.export(score, narrow);

        List<String> stringLines = stringLinesOf(linesOf(tab));
        assertTrue(stringLines.size() > 6, "narrow columns need several systems: " + tab);
        for (String line : stringLines) {
            assertTrue(line.length() <= narrow.columnsPerLine(), "no line exceeds the requested width: " + line);
        }
    }

    @Test
    void includesTheTrackNameAsAHeading() {
        Track track = Track.standardBass("Bass");
        Score score = new Score("Test", 120, List.of(track));

        String tab = exporter.export(score, AsciiTabExportOptions.standard());

        assertTrue(tab.contains("Bass"));
    }

    @Test
    void exportsOnlyTheGivenTrackNotTheWholeScore() {
        Track guitar = new Track("Guitar", Tuning.standard(), com.gstncaruso.tabpro.core.model.Channel.playing(25),
                List.of(Measure.empty(TimeSignature.fourFour(), Duration.of(NoteValue.WHOLE))));
        Track bass = Track.standardBass("Bass");

        String tab = exporter.export(guitar, AsciiTabExportOptions.standard());

        assertTrue(tab.contains("Guitar"));
        assertTrue(!tab.contains("Bass"), "must not carry other tracks: " + tab);
    }

    private static List<String> linesOf(String text) {
        return Arrays.stream(text.split("\n", -1)).toList();
    }

    private static List<String> stringLinesOf(List<String> lines) {
        return lines.stream().filter(line -> !line.isBlank() && line.chars().allMatch(c -> c == '-' || c == '|' || Character.isDigit(c))).toList();
    }
}
