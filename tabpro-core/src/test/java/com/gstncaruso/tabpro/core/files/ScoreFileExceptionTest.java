package com.gstncaruso.tabpro.core.files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScoreFileExceptionTest {

    private static final Path PATH = Path.of("song.gp5");
    private static final IOException CAUSE = new IOException("disk full");

    @Test
    void aFileThatCannotBeReadCarriesItsPath() {
        ScoreFileException failure = ScoreFileException.cannotRead(PATH, CAUSE);

        assertEquals(ScoreFileProblem.CANNOT_READ, failure.problem());
        assertEquals(List.of(PATH), failure.arguments());
        assertEquals("could not read song.gp5", failure.getMessage());
        assertSame(CAUSE, failure.getCause());
    }

    @Test
    void aFileThatCannotBeWrittenCarriesItsPath() {
        ScoreFileException failure = ScoreFileException.cannotWrite(PATH, CAUSE);

        assertEquals(ScoreFileProblem.CANNOT_WRITE, failure.problem());
        assertEquals(List.of(PATH), failure.arguments());
        assertEquals("could not write song.gp5", failure.getMessage());
        assertSame(CAUSE, failure.getCause());
    }

    @Test
    void aFileThatCannotBeExportedCarriesItsPath() {
        ScoreFileException failure = ScoreFileException.cannotExport(PATH, CAUSE);

        assertEquals(ScoreFileProblem.CANNOT_EXPORT, failure.problem());
        assertEquals(List.of(PATH), failure.arguments());
        assertEquals("could not export song.gp5", failure.getMessage());
        assertSame(CAUSE, failure.getCause());
    }
}
