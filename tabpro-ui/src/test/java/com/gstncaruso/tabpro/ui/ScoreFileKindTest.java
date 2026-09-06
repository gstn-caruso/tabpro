package com.gstncaruso.tabpro.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.ui.browser.ScoreSearch;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * El escritorio le pasa a tabpro cualquiera de los archivos que sabe abrir, asi
 * que hay que reconocerlos por su nombre antes de elegir con que leerlos.
 */
class ScoreFileKindTest {

    @Test
    void recognisesTheFilesTheDesktopCanHandOver() {
        for (String extension : ScoreSearch.EXTENSIONS) {
            assertTrue(ScoreSearch.isAScore(Path.of("cancion" + extension)), extension);
        }
    }

    @Test
    void everyGuitarProGenerationIsRecognised() {
        assertTrue(ScoreSearch.isAScore(Path.of("cancion.gp3")));
        assertTrue(ScoreSearch.isAScore(Path.of("cancion.gp4")));
        assertTrue(ScoreSearch.isAScore(Path.of("cancion.gp5")));
        assertTrue(ScoreSearch.isAScore(Path.of("cancion.gtp")));
    }

    @Test
    void anythingElseIsNotAScore() {
        assertFalse(ScoreSearch.isAScore(Path.of("cancion.mp3")));
        assertFalse(ScoreSearch.isAScore(Path.of("cancion")));
    }
}
