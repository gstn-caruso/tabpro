package com.gstncaruso.tabpro.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFiles;
import com.gstncaruso.tabpro.core.model.Score;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ScoreDocumentTest {

    @Test
    void startsWithABlankScoreAndNoPath() {
        ScoreDocument document = new ScoreDocument(new Editor(Score.blank()), new FakeScoreFiles());

        assertTrue(document.path().isEmpty());
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
