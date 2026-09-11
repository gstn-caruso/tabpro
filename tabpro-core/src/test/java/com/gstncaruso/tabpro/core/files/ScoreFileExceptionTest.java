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

    @Test
    void anOperationThatIsNotSupportedCarriesTheOperation() {
        ScoreFileException failure = ScoreFileException.notSupported(ScoreOperation.EXPORT_WAVE);

        assertEquals(ScoreFileProblem.NOT_SUPPORTED, failure.problem());
        assertEquals(List.of(ScoreOperation.EXPORT_WAVE), failure.arguments());
        assertEquals("not supported yet: EXPORT_WAVE", failure.getMessage());
    }

    @Test
    void anExportThatFailsBeforeWritingKeepsWhyInItsDetail() {
        ScoreFileException failure = ScoreFileException.cannotExport(PATH, "the synthesizer cannot render offline");

        assertEquals(ScoreFileProblem.CANNOT_EXPORT, failure.problem());
        assertEquals(List.of(PATH), failure.arguments());
        assertEquals("could not export song.gp5: the synthesizer cannot render offline", failure.getMessage());
    }

    @Test
    void aFileOfAnotherKindCarriesTheFormatItWasExpectedToBe() {
        ScoreFileException failure = ScoreFileException.notRecognized("PowerTab", "missing the ptab marker");

        assertEquals(ScoreFileProblem.NOT_RECOGNIZED, failure.problem());
        assertEquals(List.of("PowerTab"), failure.arguments());
        assertEquals("missing the ptab marker", failure.getMessage());
    }

    @Test
    void aFileOfAnotherKindKeepsTheParserFailureAsItsCause() {
        ScoreFileException failure = ScoreFileException.notRecognized("MIDI", "invalid MIDI data", CAUSE);

        assertEquals(ScoreFileProblem.NOT_RECOGNIZED, failure.problem());
        assertEquals(List.of("MIDI"), failure.arguments());
        assertSame(CAUSE, failure.getCause());
    }

    @Test
    void anUnsupportedVersionCarriesTheFormatAndTheVersion() {
        ScoreFileException failure = ScoreFileException.unsupportedVersion("Guitar Pro", "6.00");

        assertEquals(ScoreFileProblem.UNSUPPORTED_VERSION, failure.problem());
        assertEquals(List.of("Guitar Pro", "6.00"), failure.arguments());
        assertEquals("unsupported Guitar Pro version: 6.00", failure.getMessage());
    }

    @Test
    void unsupportedContentCarriesTheFeatureTheFileUses() {
        ScoreFileException failure =
                ScoreFileException.unsupportedContent(ScoreFeature.RHYTHM_SLASHES, "rhythm slashes in system 2");

        assertEquals(ScoreFileProblem.UNSUPPORTED_CONTENT, failure.problem());
        assertEquals(List.of(ScoreFeature.RHYTHM_SLASHES), failure.arguments());
        assertEquals("rhythm slashes in system 2", failure.getMessage());
    }

    @Test
    void nothingToImportNeedsNoArguments() {
        ScoreFileException failure = ScoreFileException.nothingToImport("the file has no tracks");

        assertEquals(ScoreFileProblem.NOTHING_TO_IMPORT, failure.problem());
        assertEquals(List.of(), failure.arguments());
        assertEquals("the file has no tracks", failure.getMessage());
    }

    @Test
    void damagedContentKeepsOnlyTheTechnicalDetail() {
        ScoreFileException failure = ScoreFileException.damaged("missing field: tuning");

        assertEquals(ScoreFileProblem.DAMAGED, failure.problem());
        assertEquals(List.of(), failure.arguments());
        assertEquals("missing field: tuning", failure.getMessage());
    }

    @Test
    void damagedContentKeepsTheFailureThatRevealedIt() {
        IllegalArgumentException parserFailure = new IllegalArgumentException("fret out of range");

        ScoreFileException failure = ScoreFileException.damaged("could not parse song.gp5", parserFailure);

        assertEquals(ScoreFileProblem.DAMAGED, failure.problem());
        assertEquals("could not parse song.gp5", failure.getMessage());
        assertSame(parserFailure, failure.getCause());
    }
}
