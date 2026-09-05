package com.gstncaruso.tabpro.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void openReplacesTheScoreAndRemembersThePath() {
        FakeScoreFiles files = new FakeScoreFiles();
        Path path = Path.of("cancion.tabpro");
        Score savedScore = Score.blank().withTitle("Canción guardada");
        files.scores.put(path, savedScore);
        Editor editor = new Editor(Score.blank());
        ScoreDocument document = new ScoreDocument(editor, files);

        document.open(path);

        assertEquals(savedScore, editor.score());
        assertEquals(Optional.of(path), document.path());
    }

    @Test
    void newForgetsThePath() {
        FakeScoreFiles files = new FakeScoreFiles();
        Editor editor = new Editor(Score.blank());
        ScoreDocument document = new ScoreDocument(editor, files);
        document.saveAs(Path.of("cancion.tabpro"));

        document.newScore();

        assertTrue(document.path().isEmpty());
        assertEquals(Score.blank(), editor.score());
    }

    @Test
    void describesTheDocumentByItsFileName() {
        FakeScoreFiles files = new FakeScoreFiles();
        ScoreDocument document = new ScoreDocument(new Editor(Score.blank()), files);

        document.saveAs(Path.of("carpeta", "cancion.tabpro"));

        assertEquals("cancion.tabpro", document.displayName());
    }

    @Test
    void aFailedOpenKeepsThePreviousPath() {
        FakeScoreFiles files = new FakeScoreFiles();
        Path savedPath = Path.of("cancion.tabpro");
        Editor editor = new Editor(Score.blank());
        ScoreDocument document = new ScoreDocument(editor, files);
        document.saveAs(savedPath);

        assertThrows(ScoreFileException.class, () -> document.open(Path.of("no-existe.tabpro")));

        assertEquals(Optional.of(savedPath), document.path());
        assertEquals(Score.blank(), editor.score());
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
