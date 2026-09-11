package com.gstncaruso.tabpro.ui.dialogs.style;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.harmony.Scale;
import com.gstncaruso.tabpro.core.harmony.ScaleLibrary;
import com.gstncaruso.tabpro.core.model.PercussionKit;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.TuningLibrary;
import com.gstncaruso.tabpro.core.model.TuningName;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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

    private static final List<String> TODAYS_SPANISH_BASS_TUNING_NAMES = List.of(
            "Bajo estándar", "Bajo Drop D", "Bajo medio tono abajo", "Bajo un tono abajo", "Bajo de 5 cuerdas",
            "Bajo de 6 cuerdas");

    private static final List<String> TODAYS_SPANISH_OTHER_INSTRUMENT_TUNING_NAMES = List.of(
            "Banjo Open G", "Banjo Open D", "Banjo Drop C", "Banjo Sol menor", "Banjo Sol modal", "Mandolina",
            "Ukelele en Do", "Ukelele en Sol", "Violín", "Viola", "Violoncello");

    private static final List<String> TODAYS_SPANISH_SCALE_NAMES = List.of(
            "Mayor (Jónico)", "Dórico", "Frigio", "Lidio", "Mixolidio", "Menor natural (Eólico)", "Locrio",
            "Menor armónica", "Menor melódica", "Pentatónica mayor", "Pentatónica menor", "Blues", "Tonos enteros",
            "Cromática", "Disminuida (tono-semitono)", "Disminuida dominante (semitono-tono)", "Menor húngara",
            "Española (frigia dominante)", "Napolitana menor", "Napolitana mayor", "Enigmática", "Hirajoshi",
            "In Sen", "Iwato", "China", "Egipcia", "Locrio natural 6", "Mayor #5 (Jónico aumentado)", "Dórico #4",
            "Lidio #2", "Dórico b2", "Lidio aumentado", "Lidio b7", "Mixolidio b6", "Locrio #2",
            "Superlocrio (alterada)", "Be-bop dominante", "Aumentada", "Blues mayor", "Árabe", "Balinesa",
            "Bizantina", "Húngara mayor", "Javanesa", "Kumoi", "Oriental", "Persa", "Pelog", "Armónicos (Overtone)");

    private static final List<String> TODAYS_SPANISH_PERCUSSION_SOUND_NAMES = List.of(
            "Bombo acústico", "Bombo", "Aro de caja", "Caja acústica", "Palmas", "Caja eléctrica",
            "Tom de piso grave", "Hi-hat cerrado", "Tom de piso agudo", "Hi-hat con pedal", "Tom grave",
            "Hi-hat abierto", "Tom medio grave", "Tom medio agudo", "Crash 1", "Tom agudo", "Ride 1",
            "Platillo chino", "Campana del ride", "Pandereta", "Splash", "Cencerro", "Crash 2", "Vibraslap",
            "Ride 2", "Bongo agudo", "Bongo grave", "Conga aguda apagada", "Conga aguda abierta", "Conga grave",
            "Timbal agudo", "Timbal grave", "Agogo agudo", "Agogo grave", "Cabasa", "Maracas", "Silbato corto",
            "Silbato largo", "Guiro corto", "Guiro largo", "Claves", "Cajita china aguda", "Cajita china grave",
            "Cuica apagada", "Cuica abierta", "Triangulo apagado", "Triangulo abierto");

    static Stream<Arguments> libraryTuningsWithTodaysSpanishName() {
        return Stream.of(
                        pairedInOrder(TuningLibrary.guitars(), TODAYS_SPANISH_GUITAR_TUNING_NAMES),
                        pairedInOrder(TuningLibrary.basses(), TODAYS_SPANISH_BASS_TUNING_NAMES),
                        pairedInOrder(TuningLibrary.otherStringInstruments(), TODAYS_SPANISH_OTHER_INSTRUMENT_TUNING_NAMES))
                .flatMap(pairs -> pairs);
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
    void aLibraryTuningIsLabeledInSpanishAndNamedInEnglishThroughItsId() {
        Tuning standardGuitar = TuningLibrary.standardGuitar();

        assertEquals("Guitarra estándar (EADGBE)", Labels.of(standardGuitar));
        assertEquals("Standard Guitar", ENGLISH.text("library.tuning." + libraryIdOf(standardGuitar)));
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

    static Stream<Arguments> libraryScalesWithTodaysSpanishName() {
        return pairedInOrder(ScaleLibrary.all(), TODAYS_SPANISH_SCALE_NAMES);
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("libraryScalesWithTodaysSpanishName")
    void everyLibraryScaleKeepsTodaysSpanishNameAndHasAnEnglishName(Scale scale, String todaysSpanishName) {
        String key = "library.scale." + scale.id();

        assertEquals(todaysSpanishName, SPANISH.text(key));
        assertEquals(todaysSpanishName, Labels.of(scale));
        assertFalse(ENGLISH.text(key).isBlank());
    }

    @Test
    void theMajorScaleIsNamedMajorIonianInEnglish() {
        assertEquals("Major (Ionian)", ENGLISH.text("library.scale." + ScaleLibrary.major().id()));
    }

    static Stream<Arguments> percussionSoundsWithTodaysSpanishName() {
        return pairedInOrder(PercussionKit.sounds(), TODAYS_SPANISH_PERCUSSION_SOUND_NAMES);
    }

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("percussionSoundsWithTodaysSpanishName")
    void everyPercussionSoundKeepsTodaysSpanishNameAndHasAnEnglishName(int sound, String todaysSpanishName) {
        String key = "library.percussion." + sound;

        assertEquals(todaysSpanishName, SPANISH.text(key));
        assertEquals(Optional.of(todaysSpanishName), Labels.percussionSoundName(sound));
        assertFalse(ENGLISH.text(key).isBlank());
    }

    @Test
    void aNumberOutsideTheGeneralMidiPercussionRangeHasNoSoundName() {
        assertEquals(Optional.empty(), Labels.percussionSoundName(PercussionKit.LOWEST_SOUND - 1));
        assertEquals(Optional.empty(), Labels.percussionSoundName(PercussionKit.HIGHEST_SOUND + 1));
    }

    @Test
    void percussionSoundsAreNamedInEnglishAfterTheGeneralMidiPercussionKeyMap() {
        assertEquals("Acoustic Bass Drum", ENGLISH.text("library.percussion.35"));
        assertEquals("Acoustic Snare", ENGLISH.text("library.percussion.38"));
        assertEquals("Closed Hi Hat", ENGLISH.text("library.percussion.42"));
        assertEquals("Open Triangle", ENGLISH.text("library.percussion.81"));
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
