package com.gstncaruso.tabpro.ui.dialogs.style;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.PercussionKit;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.TuningLibrary;
import com.gstncaruso.tabpro.core.model.TuningName;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LibraryLabelsTest {

    private static final Texts ENGLISH = Texts.forLocale(Locale.ENGLISH);
    private static final Texts SPANISH = Texts.forLocale(Locale.forLanguageTag("es"));

    private static final List<String> TODAYS_SPANISH_GUITAR_TUNING_NAMES = List.of(
            "Guitarra estándar", "Drop D", "Medio tono abajo", "Un tono abajo", "Drop C", "Open D", "Open G",
            "Open C", "Open E", "Open A", "DADGAD", "Nuevo estándar", "Open Cm", "Open C6", "Open Dm", "Open D5",
            "Open Dsus4", "Open Em", "Open Gm", "Open G6", "Open Gsus4", "Open Am", "Open F", "Nashville",
            "Guitarra de 7 cuerdas", "Guitarra de 7 cuerdas Drop A");

    static Stream<Arguments> libraryTuningsWithTodaysSpanishName() {
        return pairedInOrder(TuningLibrary.guitars(), TODAYS_SPANISH_GUITAR_TUNING_NAMES);
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("libraryTuningsWithTodaysSpanishName")
    void everyLibraryTuningKeepsTodaysSpanishNameAndHasAnEnglishName(Tuning tuning, String todaysSpanishName) {
        String key = "library.tuning." + libraryIdOf(tuning);

        assertEquals(todaysSpanishName, SPANISH.text(key));
        assertTrue(Labels.of(tuning).startsWith(todaysSpanishName + " ("), Labels.of(tuning));
        assertFalse(ENGLISH.text(key).isBlank());
    }

    @Test
    void anEditedTuningIsLabeledAsCustomInSpanish() {
        assertEquals("Personalizada (DADGBE)", Labels.of(Tuning.standard().withStringPitch(6, new Pitch(38))));
    }

    @Test
    void aCustomTuningIsNamedCustomInEnglish() {
        assertEquals("Custom", ENGLISH.text("library.tuning.custom"));
    }

    @Test
    void thePercussionKitTuningIsLabeledPercusionInSpanishAndNamedPercussionInEnglish() {
        assertEquals("Percusión (CCCCCC)", Labels.of(PercussionKit.tuning()));
        assertEquals("Percussion", ENGLISH.text("library.tuning.percussion"));
    }

    @Test
    void aTuningNamedByTheUserIsLabeledWithThatNameAsTyped() {
        assertEquals("Mi afinación (DADGAD)", Labels.of(Tuning.of("Mi afinación", 62, 57, 55, 50, 45, 38)));
    }

    private static String libraryIdOf(Tuning tuning) {
        if (tuning.name() instanceof TuningName.Library(String id)) {
            return id;
        }
        throw new AssertionError("not named by a library id: " + tuning.name());
    }

    private static <T> Stream<Arguments> pairedInOrder(List<T> items, List<String> names) {
        assertEquals(items.size(), names.size());
        return IntStream.range(0, items.size()).mapToObj(index -> Arguments.of(items.get(index), names.get(index)));
    }
}
