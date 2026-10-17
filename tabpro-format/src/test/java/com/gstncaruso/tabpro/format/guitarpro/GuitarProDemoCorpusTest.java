package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.format.TestDefaultNames;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.HexFormat;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

/** Uses a locally supplied GP5 installation; no vendor scores are redistributed. */
@EnabledIfSystemProperty(named = "tabpro.gp5.demos", matches = ".+")
class GuitarProDemoCorpusTest {

    @ParameterizedTest(name = "{0}")
    @CsvFileSource(resources = "/guitarpro/gp5-demos.csv", delimiter = '|', numLinesToSkip = 1)
    void importsTheNotesRecordedByPyGuitarPro(
            String relativePath, String fileHash, int tracks, int bars, int tempo, int notes, String noteHash)
            throws Exception {
        Path path = Path.of(System.getProperty("tabpro.gp5.demos")).resolve(relativePath);
        byte[] data = Files.readAllBytes(path);
        assertEquals(fileHash, sha256(data), "the oracle requires the same original demo file");

        Score score = new GuitarProFile(new TestDefaultNames()).read(data);

        assertEquals(tracks, score.trackCount());
        assertEquals(bars, score.measureCount());
        assertEquals(tempo, score.tempo());
        long noteCount = score.tracks().stream().flatMap(track -> track.measures().stream())
                .flatMap(measure -> measure.voices().stream()).flatMap(voice -> voice.beats().stream())
                .mapToLong(beat -> beat.notes().size()).sum();
        assertEquals(notes, noteCount);
        assertEquals(noteHash, sha256(notePositions(score).getBytes(StandardCharsets.UTF_8)),
                "note positions, strings, frets and ties must match the independent reader");
    }

    private static String notePositions(Score score) {
        StringBuilder positions = new StringBuilder();
        for (int ti = 0; ti < score.trackCount(); ti++) {
            var track = score.track(ti);
            for (int mi = 0; mi < track.measures().size(); mi++) {
                var measure = track.measures().get(mi);
                for (int vi = 0; vi < measure.voices().size(); vi++) {
                    var voice = measure.voices().get(vi);
                    for (int bi = 0; bi < voice.beats().size(); bi++) {
                        var beat = voice.beats().get(bi);
                        for (Note note : beat.notes().stream().sorted(Comparator.comparingInt(Note::string)).toList()) {
                            positions.append(ti).append(':').append(mi).append(':').append(vi).append(':')
                                    .append(bi).append(':').append(note.string()).append(':').append(note.fret())
                                    .append(':').append(note.tied() ? 1 : 0).append('\n');
                        }
                    }
                }
            }
        }
        return positions.toString();
    }

    private static String sha256(byte[] data) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
    }
}
