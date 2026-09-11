package com.gstncaruso.tabpro.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class WindowTextsTest {

    private static final Texts ENGLISH = Texts.forLocale(Locale.ENGLISH);
    private static final Texts SPANISH = Texts.forLocale(Locale.forLanguageTag("es"));

    static Stream<Arguments> textsInBothLanguages() {
        return Stream.of(
                Arguments.of("window.MainFrame.guitarProFilter", new Object[] {},
                        "Partituras de Guitar Pro", "Guitar Pro Files"),
                Arguments.of("window.MainFrame.imageFilter", new Object[] {},
                        "Imagen (*.png, *.jpg, *.bmp)", "Image (*.png, *.jpg, *.bmp)"),
                Arguments.of("window.MainFrame.midiFilter", new Object[] {},
                        "Archivos MIDI (*.mid)", "MIDI Files (*.mid)"),
                Arguments.of("window.MainFrame.openableScoresFilter", new Object[] {},
                        "Partituras (*.tabpro, *.gp3, *.gp4, *.gp5, *.gtp, *.tef, *.ptb)",
                        "Scores (*.tabpro, *.gp3, *.gp4, *.gp5, *.gtp, *.tef, *.ptb)"),
                Arguments.of("window.MainFrame.powerTabFilter", new Object[] {},
                        "Partituras de PowerTab", "PowerTab Files"),
                Arguments.of("window.MainFrame.tabEditFilter", new Object[] {},
                        "Archivos de TablEdit (*.tef)", "TablEdit Files (*.tef)"),
                Arguments.of("window.MainFrame.tabproFilter", new Object[] {},
                        "Partituras tabpro (*.tabpro)", "tabpro Scores (*.tabpro)"),
                Arguments.of("window.MainFrame.waveFilter", new Object[] {},
                        "Audio WAVE (*.wav)", "WAVE Audio (*.wav)"),
                Arguments.of("window.MainFrame.exportGuitarProAnyway", new Object[] {"- a loss"},
                        "Al exportar a Guitar Pro 4 se va a perder:\n\n- a loss\n\n¿Exportar de todos modos?",
                        "Exporting to Guitar Pro 4 will lose:\n\n- a loss\n\nExport anyway?"),
                Arguments.of("window.error.printing", new Object[] {"no printer"},
                        "No se pudo imprimir: no printer", "Could not print: no printer"),
                Arguments.of("window.error.readingTheFile", new Object[] {"song.txt"},
                        "No se pudo leer el archivo: song.txt", "Could not read the file: song.txt"));
    }

    @ParameterizedTest
    @MethodSource
    void textsInBothLanguages(String key, Object[] arguments, String spanish, String english) {
        assertEquals(spanish, SPANISH.text(key, arguments));
        assertEquals(english, ENGLISH.text(key, arguments));
    }
}
