package com.gstncaruso.tabpro.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.files.ScoreFiles;
import com.gstncaruso.tabpro.core.model.Beat;
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
        assertTrue(content.startsWith("{\n  \"format\": 1,"));
    }

    @Test
    void loadsTheVersionOneFixture() throws URISyntaxException {
        Path path = Path.of(getClass().getResource("/v1-one-measure.tabpro").toURI());

        Score loaded = scoreFiles.load(path);

        Beat firstBeat = Beat.of(Duration.quarter(), new Note(6, 0), new Note(5, 2));
        Beat secondBeat = Beat.rest(new Duration(NoteValue.EIGHTH, true));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(firstBeat, secondBeat));
        Track track = new Track("Guitarra", Tuning.standard(), 25, List.of(measure));
        Score expected = new Score("Prueba", 120, List.of(track));

        assertEquals(expected, loaded);
    }
}
