package com.gstncaruso.tabpro.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.files.ScoreFiles;
import com.gstncaruso.tabpro.core.model.PercussionKit;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.TuningLibrary;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TuningNamesInTabproFilesTest {

    private static final List<String> NAMES_STORED_FOR_THE_LIBRARY_BEFORE_IDS = List.of(
            "Guitarra estándar", "Drop D", "Medio tono abajo", "Un tono abajo", "Drop C", "Open D", "Open G",
            "Open C", "Open E", "Open A", "DADGAD", "Nuevo estándar", "Open Cm", "Open C6", "Open Dm", "Open D5",
            "Open Dsus4", "Open Em", "Open Gm", "Open G6", "Open Gsus4", "Open Am", "Open F", "Nashville",
            "Guitarra de 7 cuerdas", "Guitarra de 7 cuerdas Drop A",
            "Bajo estándar", "Bajo Drop D", "Bajo medio tono abajo", "Bajo un tono abajo", "Bajo de 5 cuerdas",
            "Bajo de 6 cuerdas",
            "Banjo Open G", "Banjo Open D", "Banjo Drop C", "Banjo Sol menor", "Banjo Sol modal", "Mandolina",
            "Ukelele en Do", "Ukelele en Sol", "Violín", "Viola", "Violoncello");

    private final ScoreFiles scoreFiles = new JsonScoreFiles();

    @Test
    void aFileSavedWithTheStandardGuitarNameInSpanishLoadsTheLibraryStandardGuitar(@TempDir Path tempDir)
            throws IOException {
        Path path = tempDir.resolve("old.tabpro");
        Files.writeString(path, """
                {
                  "format": 5,
                  "title": "Saved before library ids",
                  "tempo": 120,
                  "tracks": [
                    {
                      "name": "Guitarra",
                      "midiProgram": 25,
                      "channel": 1,
                      "effectChannel": 2,
                      "tuningName": "Guitarra estándar",
                      "tuning": [64, 59, 55, 50, 45, 40],
                      "measures": [
                        {
                          "timeSignature": { "beats": 4, "beatUnit": 4 },
                          "beats": [
                            { "value": 4, "dotted": false, "notes": [ { "string": 1, "fret": 0 } ] }
                          ]
                        }
                      ]
                    }
                  ]
                }
                """);

        assertEquals(TuningLibrary.standardGuitar(), scoreFiles.load(path).track(0).tuning());
    }

    @Test
    void theNamesOldFilesStoredCoverTheWholeLibrary() {
        assertEquals(TuningLibrary.all().size(), NAMES_STORED_FOR_THE_LIBRARY_BEFORE_IDS.size());
    }

    static Stream<Arguments> libraryTuningsWithTheNameOldFilesStored() {
        List<Tuning> library = TuningLibrary.all();
        return IntStream.range(0, library.size())
                .mapToObj(index -> Arguments.of(NAMES_STORED_FOR_THE_LIBRARY_BEFORE_IDS.get(index), library.get(index)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("libraryTuningsWithTheNameOldFilesStored")
    void aFileThatStoredTheSpanishNameOfALibraryTuningLoadsThatLibraryTuning(
            String storedName, Tuning libraryTuning, @TempDir Path tempDir) throws IOException {
        Path path = fileWithOneTrack(tempDir, "\"" + storedName + "\"", libraryTuning.strings());

        assertEquals(libraryTuning, scoreFiles.load(path).track(0).tuning());
    }

    @Test
    void aFileThatStoredPercusionLoadsThePercussionKitTuning(@TempDir Path tempDir) throws IOException {
        Path path = fileWithOneTrack(tempDir, "\"Percusión\"", PercussionKit.tuning().strings());

        assertEquals(PercussionKit.tuning(), scoreFiles.load(path).track(0).tuning());
    }

    @Test
    void aFileThatStoredPersonalizadaLoadsACustomTuningEvenWhenItsPitchesMatchTheLibrary(@TempDir Path tempDir)
            throws IOException {
        List<Pitch> dropDPitches = pitches(64, 59, 55, 50, 45, 38);
        Path path = fileWithOneTrack(tempDir, "\"Personalizada\"", dropDPitches);

        assertEquals(new Tuning(dropDPitches), scoreFiles.load(path).track(0).tuning());
    }

    @Test
    void aFileThatStoredANameOutsideTheLibraryKeepsThatNameEvenWhenItsPitchesMatchTheLibrary(@TempDir Path tempDir)
            throws IOException {
        Path path = fileWithOneTrack(tempDir, "\"Mi afinación\"", pitches(62, 57, 55, 50, 45, 38));

        assertEquals(Tuning.of("Mi afinación", 62, 57, 55, 50, 45, 38), scoreFiles.load(path).track(0).tuning());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("libraryTuningsWithTheNameOldFilesStored")
    void aSavedLibraryTuningIsStoredUnderTheNameEveryEarlierVersionReads(
            String storedName, Tuning libraryTuning, @TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("new.tabpro");

        scoreFiles.save(scoreWithTunings(List.of(libraryTuning)), path);

        assertTrue(Files.readString(path).contains("\"tuningName\": \"" + storedName + "\""));
    }

    @Test
    void aSavedPercussionTrackIsStoredAsPercusion(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("new.tabpro");

        scoreFiles.save(new Score("Drums", 120, List.of(Track.percussion("Drums"))), path);

        assertTrue(Files.readString(path).contains("\"tuningName\": \"Percusión\""));
    }

    @Test
    void aSavedCustomTuningIsStoredAsPersonalizada(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("new.tabpro");

        scoreFiles.save(scoreWithTunings(List.of(Tuning.standard().withStringPitch(6, new Pitch(38)))), path);

        assertTrue(Files.readString(path).contains("\"tuningName\": \"Personalizada\""));
    }

    @Test
    void aScoreWithEveryKindOfTuningReloadsIdentically(@TempDir Path tempDir) {
        List<Tuning> tunings = Stream.concat(TuningLibrary.all().stream(), Stream.of(
                PercussionKit.tuning(),
                Tuning.standard().withStringPitch(6, new Pitch(38)),
                Tuning.standardBass().withStringCount(5),
                Tuning.of("Mi afinación", 62, 57, 55, 50, 45, 38),
                TuningLibrary.standardGuitar().transposed(-1))).toList();
        Score score = scoreWithTunings(tunings);
        Path path = tempDir.resolve("new.tabpro");

        scoreFiles.save(score, path);

        assertEquals(score, scoreFiles.load(path));
    }

    private static Score scoreWithTunings(List<Tuning> tunings) {
        List<Track> tracks = tunings.stream().map(tuning -> Track.standardGuitar("Track").withTuning(tuning)).toList();
        return new Score("Saved after library ids", 120, tracks);
    }

    private static Path fileWithOneTrack(Path directory, String storedTuningName, List<Pitch> strings)
            throws IOException {
        Path path = directory.resolve("old.tabpro");
        String tuning = strings.stream().map(pitch -> String.valueOf(pitch.midiNumber()))
                .collect(Collectors.joining(", ", "[", "]"));
        Files.writeString(path, """
                {
                  "format": 5,
                  "title": "Saved before library ids",
                  "tempo": 120,
                  "tracks": [
                    {
                      "name": "Track",
                      "midiProgram": 25,
                      "tuningName": %s,
                      "tuning": %s,
                      "measures": [
                        {
                          "timeSignature": { "beats": 4, "beatUnit": 4 },
                          "beats": [ { "value": 4, "dotted": false, "notes": [] } ]
                        }
                      ]
                    }
                  ]
                }
                """.formatted(storedTuningName, tuning));
        return path;
    }

    private static List<Pitch> pitches(int... midiNumbers) {
        return IntStream.of(midiNumbers).mapToObj(Pitch::new).toList();
    }
}
