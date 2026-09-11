package com.gstncaruso.tabpro.ui.dialogs.style;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.gstncaruso.tabpro.core.files.ExportWarning;
import com.gstncaruso.tabpro.core.files.ExportWarning.Loss;
import com.gstncaruso.tabpro.core.files.ScoreFeature;
import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreOperation;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.print.ImageExportException;
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

    static Stream<Arguments> everyImageExportProblemInBothLanguages() {
        return Stream.of(
                Arguments.of(ImageExportException.bmpOnlyInPageMode(),
                        "La exportación a BMP sólo está disponible en modo Página.",
                        "BMP export is only available in Page mode."),
                Arguments.of(ImageExportException.noImageWriterFor("jpg"),
                        "No se pudo exportar la imagen en formato JPG: ningún códec de imagen instalado sabe codificarla"
                                + " en ese formato.",
                        "Could not export the image as JPG: no installed image codec can encode that format."));
    }

    @ParameterizedTest
    @MethodSource
    void everyImageExportProblemInBothLanguages(ImageExportException failure, String spanish, String english) {
        assertEquals(spanish, ErrorTexts.of(failure, SPANISH));
        assertEquals(english, ErrorTexts.of(failure, ENGLISH));
    }

    @Test
    void theProcessLanguageRendersTheSpanishImageExportSentence() {
        assertEquals("La exportación a BMP sólo está disponible en modo Página.",
                ErrorTexts.of(ImageExportException.bmpOnlyInPageMode()));
    }

    static Stream<Arguments> everyGuitarProExportLossInBothLanguages() {
        return Stream.of(
                Arguments.of(ExportWarning.of(Loss.MUSIC_AUTHOR, "Composer"),
                        "El autor de la música ('Composer') se pierde: Guitar Pro 4 no tiene un campo propio para él.",
                        "The music author ('Composer') is lost: Guitar Pro 4 has no field for it."),
                Arguments.of(ExportWarning.of(Loss.SECOND_VOICE),
                        "La segunda voz de los compases se pierde: Guitar Pro 4 admite una sola voz por compás.",
                        "The second voice of the bars is lost: Guitar Pro 4 allows a single voice per bar."),
                Arguments.of(ExportWarning.of(Loss.TRIPLET_FEEL_CHANGES),
                        "El 'triplet feel' cambia entre compases; Guitar Pro 4 admite uno solo para toda la "
                                + "partitura (se exporta el del primer compás).",
                        "The Triplet Feel changes between bars; Guitar Pro 4 allows only one for the whole score "
                                + "(the first bar's is exported)."),
                Arguments.of(ExportWarning.of(Loss.TRACK_DISPLAY, "Lead"),
                        "La pista 'Lead' tiene una configuración de vista (pentagrama, tablatura o diagramas) que "
                                + "Guitar Pro 4 no guarda: vuelve a mostrarse con los valores por defecto.",
                        "Track 'Lead' has display settings (standard notation, tablature or diagrams) that "
                                + "Guitar Pro 4 does not store: they return to their defaults."),
                Arguments.of(ExportWarning.of(Loss.MIDI_PORT, "Lead", 2),
                        "La pista 'Lead' usa el puerto MIDI 2; Guitar Pro 4 solo admite el puerto 1.",
                        "Track 'Lead' uses MIDI port 2; Guitar Pro 4 only allows port 1."),
                Arguments.of(ExportWarning.of(Loss.WIDE_VIBRATO),
                        "El vibrato ancho de algún compás se pierde: solo existe en Guitar Pro 3.",
                        "The Wide Vibrato of some bar is lost: it only exists in Guitar Pro 3."),
                Arguments.of(ExportWarning.of(Loss.CHORD_NAME_ONLY),
                        "Algún acorde marcado para mostrar solo el nombre va a mostrarse con el diagrama completo.",
                        "A chord set to show only its name will show the full diagram."),
                Arguments.of(ExportWarning.of(Loss.GRACE_NOTE_ON_BEAT_OR_DEAD),
                        "Alguna nota de adorno usa 'en el tiempo' o 'nota muerta': esos datos no existen en "
                                + "Guitar Pro 4.",
                        "A grace note uses 'on the beat' or 'dead note': Guitar Pro 4 has no such data."));
    }

    @ParameterizedTest
    @MethodSource
    void everyGuitarProExportLossInBothLanguages(ExportWarning warning, String spanish, String english) {
        assertEquals(spanish, ErrorTexts.of(warning, SPANISH));
        assertEquals(english, ErrorTexts.of(warning, ENGLISH));
    }

    @Test
    void theProcessLanguageRendersTheSpanishLossSentence() {
        assertEquals("La segunda voz de los compases se pierde: Guitar Pro 4 admite una sola voz por compás.",
                ErrorTexts.of(ExportWarning.of(Loss.SECOND_VOICE)));
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
