package com.gstncaruso.tabpro.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFiles;
import com.gstncaruso.tabpro.core.model.Score;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ScoreDocumentTest {

    @Test
    void startsWithABlankScoreAndNoPath() {
        ScoreDocument document = new ScoreDocument(new Editor(Score.blank()), new FakeScoreFiles());

        assertTrue(document.path().isEmpty());
    }

    @Test
    void describesAnUntitledDocument() {
        ScoreDocument document = new ScoreDocument(new Editor(Score.blank()), new FakeScoreFiles());

        assertEquals(ScoreDocument.UNTITLED, document.displayName());
    }

    @Test
    void saveWithoutAPathAsksForOne() {
        FakeScoreFiles files = new FakeScoreFiles();
        ScoreDocument document = new ScoreDocument(new Editor(Score.blank()), files);

        assertFalse(document.save());
        assertEquals(0, files.saveCount);
    }

    @Test
    void saveAsRemembersThePath() {
        FakeScoreFiles files = new FakeScoreFiles();
        ScoreDocument document = new ScoreDocument(new Editor(Score.blank()), files);
        Path path = Path.of("cancion.tabpro");

        document.saveAs(path);

        assertEquals(Optional.of(path), document.path());
        assertEquals(1, files.saveCount);
    }

    @Test
    void saveReusesTheRememberedPath() {
        FakeScoreFiles files = new FakeScoreFiles();
        ScoreDocument document = new ScoreDocument(new Editor(Score.blank()), files);
        Path path = Path.of("cancion.tabpro");
        document.saveAs(path);

        boolean saved = document.save();

        assertTrue(saved);
        assertEquals(2, files.saveCount);
    }

    private static final class FakeScoreFiles implements ScoreFiles {

        private final Map<Path, Score> scores = new HashMap<>();
        private int saveCount = 0;

        @Override
        public Score load(Path path) {
            Score score = scores.get(path);
            if (score == null) {
                throw new ScoreFileException("no existe " + path);
            }
            return score;
        }

        @Override
        public void save(Score score, Path path) {
            saveCount++;
            scores.put(path, score);
        }
    }
}
