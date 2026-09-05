package com.gstncaruso.tabpro.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.files.ScoreFiles;
import com.gstncaruso.tabpro.core.model.Score;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
}
