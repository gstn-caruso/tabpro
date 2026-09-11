package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFileProblem;
import java.util.List;
import org.junit.jupiter.api.Test;

class GuitarProVersionTest {

    @Test
    void aHeaderOfAnotherProgramIsNotRecognizedAsGuitarPro() {
        ScoreFileException failure =
                assertThrows(ScoreFileException.class, () -> GuitarProVersion.parse("PTAB something else"));

        assertEquals(ScoreFileProblem.NOT_RECOGNIZED, failure.problem());
        assertEquals(List.of("Guitar Pro"), failure.arguments());
    }

    @Test
    void aLaterGenerationIsAnUnsupportedVersion() {
        ScoreFileException failure =
                assertThrows(ScoreFileException.class, () -> GuitarProVersion.parse("FICHIER GUITAR PRO v6.00"));

        assertEquals(ScoreFileProblem.UNSUPPORTED_VERSION, failure.problem());
        assertEquals(List.of("Guitar Pro", "6.00"), failure.arguments());
    }
}
