package com.gstncaruso.tabpro.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFiles;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonScoreFilesTest {

    private final ScoreFiles scoreFiles = new JsonScoreFiles();

    @Test
    void savesAndLoadsTheSameScore(@TempDir Path tempDir) {
        Score score = Score.blank();
        Path path = tempDir.resolve("score.tabpro");

        scoreFiles.save(score, path);
        Score loaded = scoreFiles.load(path);

        assertEquals(score, loaded);
    }

    @Test
    void writesTheFormatVersionFirst(@TempDir Path tempDir) throws IOException {
        Score score = Score.blank();
        Path path = tempDir.resolve("score.tabpro");

        scoreFiles.save(score, path);

        String content = Files.readString(path);
        assertTrue(content.startsWith("{\n  \"format\": " + ScoreDto.CURRENT_FORMAT + ","));
    }

    @Test
    void loadsTheVersionOneFixture() throws URISyntaxException {
        Path path = Path.of(getClass().getResource("/v1-one-measure.tabpro").toURI());

        Score loaded = scoreFiles.load(path);

        Beat firstBeat = Beat.of(Duration.quarter(), new Note(6, 0), new Note(5, 2));
        Beat secondBeat = Beat.rest(new Duration(NoteValue.EIGHTH, true));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(firstBeat, secondBeat));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score expected = new Score("Prueba", 120, List.of(track));

        assertEquals(expected, loaded);
    }

    @Test
    void aVersionOneFileGetsTheDefaultMixerSettings() throws URISyntaxException {
        Path path = Path.of(getClass().getResource("/v1-one-measure.tabpro").toURI());

        Channel channel = scoreFiles.load(path).track(0).channel();

        assertEquals(Channel.DEFAULT_VOLUME, channel.volume());
        assertEquals(Channel.CENTER_PAN, channel.pan());
        assertFalse(channel.muted());
        assertFalse(channel.solo());
    }

    @Test
    void savesAndLoadsTheMixerOfEveryTrack(@TempDir Path tempDir) {
        Track guitar = Track.standardGuitar("Guitarra")
                .withChannel(Channel.playing(30).withVolume(80).withPan(20).toggledSolo());
        Track bass = Track.standardBass("Bajo").withChannel(Channel.playing(33).toggledMute());
        Score score = new Score("Prueba", 120, List.of(guitar, bass));
        Path path = tempDir.resolve("score.tabpro");

        scoreFiles.save(score, path);

        assertEquals(score, scoreFiles.load(path));
    }

    @Test
    void rejectsAnUnsupportedFormatVersion(@TempDir Path tempDir) throws IOException, URISyntaxException {
        String validContent = Files.readString(Path.of(getClass().getResource("/v1-one-measure.tabpro").toURI()));
        String unsupportedContent = validContent.replaceFirst("\"format\": 1", "\"format\": 99");
        Path path = tempDir.resolve("score.tabpro");
        Files.writeString(path, unsupportedContent);

        assertThrows(ScoreFileException.class, () -> scoreFiles.load(path));
    }

    @Test
    void rejectsMalformedJson(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("score.tabpro");
        Files.writeString(path, "{ esto no es json valido");

        assertThrows(ScoreFileException.class, () -> scoreFiles.load(path));
    }

    @Test
    void rejectsAMissingFile(@TempDir Path tempDir) {
        Path path = tempDir.resolve("no-existe.tabpro");

        assertThrows(ScoreFileException.class, () -> scoreFiles.load(path));
    }

    @Test
    void rejectsAMissingNestedField(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("score.tabpro");
        String jsonWithoutBeats = """
                {
                  "format": 1,
                  "title": "Prueba",
                  "tempo": 120,
                  "tracks": [
                    {
                      "name": "Guitarra",
                      "midiProgram": 25,
                      "tuning": [64, 59, 55, 50, 45, 40],
                      "measures": [
                        {
                          "timeSignature": { "beats": 4, "beatUnit": 4 }
                        }
                      ]
                    }
                  ]
                }
                """;
        Files.writeString(path, jsonWithoutBeats);

        ScoreFileException thrown = assertThrows(ScoreFileException.class, () -> scoreFiles.load(path));

        assertTrue(thrown.getMessage().contains("beats"));
        assertFalse(thrown.getMessage().contains("vacio"));
    }
}
