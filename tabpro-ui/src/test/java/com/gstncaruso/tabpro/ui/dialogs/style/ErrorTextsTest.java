package com.gstncaruso.tabpro.ui.dialogs.style;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.gstncaruso.tabpro.core.files.ScoreFeature;
import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreOperation;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class ErrorTextsTest {

    private static final Texts ENGLISH = Texts.forLocale(Locale.ENGLISH);
    private static final Texts SPANISH = Texts.forLocale(Locale.forLanguageTag("es"));
    private static final Path SONG = Path.of("song.gp5");
    private static final IOException CAUSE = new IOException("disk full");

    static Stream<Arguments> everyProblemInBothLanguages() {
        return Stream.of(
                Arguments.of(ScoreFileException.cannotRead(SONG, CAUSE),
                        "no se pudo leer song.gp5",
                        "Could not read song.gp5"),
                Arguments.of(ScoreFileException.cannotWrite(SONG, CAUSE),
                        "no se pudo escribir song.gp5",
                        "Could not write song.gp5"),
                Arguments.of(ScoreFileException.cannotExport(SONG, CAUSE),
                        "no se pudo exportar song.gp5",
                        "Could not export song.gp5"),
                Arguments.of(ScoreFileException.notSupported(ScoreOperation.EXPORT_WAVE),
                        "la exportación a WAVE todavía no está disponible.",
                        "WAVE export is not available yet."),
                Arguments.of(ScoreFileException.notRecognized("PowerTab", "missing the ptab marker"),
                        "el archivo no es un archivo de PowerTab reconocido",
                        "The file is not a recognized PowerTab file"),
                Arguments.of(ScoreFileException.unsupportedVersion("Guitar Pro", "6.00"),
                        "la versión 6.00 de Guitar Pro no está soportada",
                        "Guitar Pro version 6.00 is not supported"),
                Arguments.of(ScoreFileException.unsupportedContent(ScoreFeature.RHYTHM_SLASHES, "rhythm slashes"),
                        "este archivo usa barras de ritmo (rhythm slash), que tabpro todavía no soporta",
                        "This file uses rhythm slashes, which tabpro does not support yet"),
                Arguments.of(ScoreFileException.nothingToImport("the file has no tracks"),
                        "no hay nada para importar",
                        "There is nothing to import"),
                Arguments.of(ScoreFileException.damaged("missing field: tuning"),
                        "el contenido está dañado o no se pudo interpretar\nmissing field: tuning",
                        "The contents are damaged or could not be interpreted\nmissing field: tuning"));
    }

    @ParameterizedTest
    @MethodSource
    void everyProblemInBothLanguages(ScoreFileException failure, String spanish, String english) {
        assertEquals(spanish, ErrorTexts.of(failure, SPANISH));
        assertEquals(english, ErrorTexts.of(failure, ENGLISH));
    }

    static Stream<Arguments> everyUnsupportedOperationReadsAsTodaysSpanishSentence() {
        return Stream.of(
                Arguments.of(ScoreOperation.IMPORT_MIDI, "la importación de MIDI todavía no está disponible."),
                Arguments.of(ScoreOperation.EXPORT_MIDI, "la exportación a MIDI todavía no está disponible."),
                Arguments.of(ScoreOperation.EXPORT_WAVE, "la exportación a WAVE todavía no está disponible."),
                Arguments.of(ScoreOperation.IMPORT_ASCII,
                        "la importación de tablatura ASCII todavía no está disponible."),
                Arguments.of(ScoreOperation.EXPORT_ASCII,
                        "la exportación a tablatura ASCII todavía no está disponible."),
                Arguments.of(ScoreOperation.IMPORT_MUSIC_XML, "la importación de MusicXML todavía no está disponible."),
                Arguments.of(ScoreOperation.EXPORT_MUSIC_XML, "la exportación a MusicXML todavía no está disponible."),
                Arguments.of(ScoreOperation.OPEN_GUITAR_PRO,
                        "la apertura de archivos de Guitar Pro todavía no está disponible."),
                Arguments.of(ScoreOperation.OPEN_TAB_EDIT,
                        "la apertura de archivos de TablEdit todavía no está disponible."),
                Arguments.of(ScoreOperation.EXPORT_GUITAR_PRO, "la exportación a Guitar Pro todavía no está disponible."),
                Arguments.of(ScoreOperation.IMPORT_POWER_TAB,
                        "la importación de archivos de PowerTab todavía no está disponible."));
    }

    @ParameterizedTest
    @MethodSource
    void everyUnsupportedOperationReadsAsTodaysSpanishSentence(ScoreOperation operation, String spanish) {
        assertEquals(spanish, ErrorTexts.of(ScoreFileException.notSupported(operation), SPANISH));
    }

    @Test
    void theProcessLanguageRendersTheSpanishSentence() {
        assertEquals("no se pudo leer song.gp5", ErrorTexts.of(ScoreFileException.cannotRead(SONG, CAUSE)));
    }

    @ParameterizedTest
    @EnumSource(ScoreOperation.class)
    void everyOperationHasATextInBothLanguages(ScoreOperation operation) {
        assertDifferentTextsInBothLanguages(ScoreFileException.notSupported(operation));
    }

    @ParameterizedTest
    @EnumSource(ScoreFeature.class)
    void everyFeatureHasATextInBothLanguages(ScoreFeature feature) {
        assertDifferentTextsInBothLanguages(ScoreFileException.unsupportedContent(feature, feature.name()));
    }

    private static void assertDifferentTextsInBothLanguages(ScoreFileException failure) {
        assertNotEquals(ErrorTexts.of(failure, SPANISH), ErrorTexts.of(failure, ENGLISH));
    }
}
