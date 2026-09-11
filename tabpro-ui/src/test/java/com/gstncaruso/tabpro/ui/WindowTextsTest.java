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
                Arguments.of("window.MainFrame.aboutMessage", new Object[] {},
                        "tabpro — clon libre de Guitar Pro 5.", "tabpro — a free Guitar Pro 5 clone."),
                Arguments.of("window.MainFrame.aboutTitle", new Object[] {}, "Acerca de tabpro", "About tabpro"),
                Arguments.of("window.MainFrame.noMidiInstrument", new Object[] {},
                        "No hay ningún instrumento MIDI conectado.", "No MIDI instrument is connected."),
                Arguments.of("window.MainFrame.percussionAssistantNeedsAPercussionTrack", new Object[] {},
                        "El asistente de percusión sólo sirve en una pista de percusión.",
                        "The Percussion Assistant only works on a percussion track."),
                Arguments.of("window.MainFrame.percussionAssistantTitle", new Object[] {},
                        "Asistente de percusión", "Percussion Assistant"),
                Arguments.of("window.MainFrame.recoverUnsavedScore", new Object[] {},
                        "Quedó una partitura sin guardar de la última sesión. ¿Recuperarla?",
                        "An unsaved score was left from the last session. Recover it?"),
                Arguments.of("window.MainFrame.saveUnsavedChanges", new Object[] {},
                        "La partitura tiene cambios sin guardar. ¿Guardarlos?",
                        "The score has unsaved changes. Save them?"),
                Arguments.of("window.MainFrame.tempo", new Object[] {}, "Tempo", "Tempo"),
                Arguments.of("window.MainFrame.tempoMustBeANumber", new Object[] {},
                        "El tempo se escribe con un número.", "The tempo must be a number."),
                Arguments.of("window.MainFrame.tempoPrompt", new Object[] {},
                        "Tempo en negras por minuto", "Tempo in quarter notes per minute"),
                Arguments.of("window.MainFrame.textPrompt", new Object[] {},
                        "Texto sobre la tablatura", "Text above the tablature"),
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
