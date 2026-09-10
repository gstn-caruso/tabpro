package com.gstncaruso.tabpro.ui.dialogs.style;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.gstncaruso.tabpro.core.harmony.Chord;
import com.gstncaruso.tabpro.core.harmony.ChordType;
import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.harmony.Scale;
import com.gstncaruso.tabpro.core.harmony.ScaleLibrary;
import com.gstncaruso.tabpro.core.model.DiagramPlacement;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.TuningLibrary;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.model.bars.LineBreak;
import com.gstncaruso.tabpro.core.model.effects.BeamBreak;
import com.gstncaruso.tabpro.core.model.effects.PickstrokeDirection;
import com.gstncaruso.tabpro.core.model.effects.StemOverride;
import com.gstncaruso.tabpro.core.model.bars.TripletFeel;
import com.gstncaruso.tabpro.core.model.chords.ChordComplexity;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.instruments.FretboardDisplayMode;
import com.gstncaruso.tabpro.ui.instruments.FretboardType;
import com.gstncaruso.tabpro.ui.instruments.KeyboardDisplayMode;
import com.gstncaruso.tabpro.ui.instruments.NoteNameMode;
import com.gstncaruso.tabpro.ui.instruments.ScaleLabelMode;
import com.gstncaruso.tabpro.ui.instruments.ScaleType;
import com.gstncaruso.tabpro.ui.harmony.BarrePreference;
import com.gstncaruso.tabpro.ui.page.Orientation;
import com.gstncaruso.tabpro.ui.page.PaperFormat;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class LabelsTest {

    @Test
    void translatesTheNoteValue() {
        assertEquals("Negra", Labels.of(NoteValue.QUARTER));
    }

    @ParameterizedTest
    @EnumSource(NoteValue.class)
    void everyNoteValueHasItsOwnLabel(NoteValue value) {
        String label = Labels.of(value);

        assertFalse(label.isBlank());
        assertNotEquals(value.name(), label);
    }

    @Test
    void translatesTheMajorChordTypeToTheSuffixTheManualUses() {
        assertEquals("M", Labels.of(ChordType.MAJOR));
    }

    @Test
    void translatesTheChordTypeToItsMusicalSuffix() {
        assertEquals("m7", Labels.of(ChordType.MINOR_SEVENTH));
    }

    @ParameterizedTest
    @EnumSource(ChordType.class)
    void everyChordTypeHasItsOwnLabel(ChordType value) {
        String label = Labels.of(value);

        assertFalse(label.isBlank());
        assertNotEquals(value.name(), label);
    }

    @Test
    void translatesThePositionsComplexity() {
        assertEquals("Todas", Labels.of(ChordComplexity.COMPLEX));
    }

    @ParameterizedTest
    @EnumSource(ChordComplexity.class)
    void everyPositionsComplexityHasItsOwnLabel(ChordComplexity value) {
        String label = Labels.of(value);

        assertFalse(label.isBlank());
        assertNotEquals(value.name(), label);
    }

    @Test
    void translatesTheBarrePreference() {
        assertEquals("Cualquiera", Labels.of(BarrePreference.ANY));
    }

    @ParameterizedTest
    @EnumSource(BarrePreference.class)
    void everyBarrePreferenceHasItsOwnLabel(BarrePreference value) {
        String label = Labels.of(value);

        assertFalse(label.isBlank());
        assertNotEquals(value.name(), label);
    }

    @Test
    void translatesTheNoteWithItsSpanishNameInParentheses() {
        assertEquals("C (Do)", Labels.of(PitchClass.of("C")));
    }

    @Test
    void theLabelOfANoteNeverMatchesItsToString() {
        PitchClass fSharp = PitchClass.of("F#");

        assertNotEquals(fSharp.toString(), Labels.of(fSharp));
    }

    @Test
    void translatesTheScaleWithItsSpanishName() {
        assertEquals("Mayor (Jónico)", Labels.of(ScaleLibrary.major()));
    }

    @Test
    void everyScaleInTheLibraryHasALabelThatIsNotItsToString() {
        for (Scale scale : ScaleLibrary.all()) {
            String label = Labels.of(scale);

            assertFalse(label.isBlank());
            assertNotEquals(scale.toString(), label);
        }
    }

    @Test
    void translatesTheTuningWithItsNameAndStringSummary() {
        assertEquals("Guitarra estándar (EADGBE)", Labels.of(TuningLibrary.standardGuitar()));
    }

    @Test
    void everyTuningInTheLibraryHasALabelThatIsNotItsToString() {
        for (Tuning tuning : TuningLibrary.guitars()) {
            String label = Labels.of(tuning);

            assertFalse(label.isBlank());
            assertNotEquals(tuning.toString(), label);
        }
    }

    @Test
    void translatesTheDynamicToItsMusicalSymbol() {
        assertEquals("mf", Labels.of(Dynamic.MEZZO_FORTE));
    }

    @ParameterizedTest
    @EnumSource(Dynamic.class)
    void everyDynamicHasItsOwnLabel(Dynamic value) {
        String label = Labels.of(value);

        assertFalse(label.isBlank());
        assertNotEquals(value.name(), label);
    }

    @Test
    void translatesTheChordWithItsNameInsteadOfTheRawRecord() {
        Chord chord = Chord.of(PitchClass.of("C"), ChordType.MINOR_SEVENTH);

        assertEquals("Cm7", Labels.of(chord));
    }

    @Test
    void translatesTheFretboardScaleType() {
        assertEquals("Mayor", Labels.of(ScaleType.MAJOR));
    }

    @ParameterizedTest
    @EnumSource(ScaleType.class)
    void everyFretboardScaleTypeHasItsOwnLabel(ScaleType value) {
        String label = Labels.of(value);

        assertFalse(label.isBlank());
        assertNotEquals(value.name(), label);
    }

    @Test
    void translatesTheFretboardAndKeyboardModes() {
        assertEquals("Solo el beat", Labels.of(FretboardDisplayMode.ONLY_BEAT));
        assertEquals("Sin nombres", Labels.of(NoteNameMode.NONE));
        assertEquals("Nombre", Labels.of(ScaleLabelMode.NAME));
        assertEquals("Eléctrica", Labels.of(FretboardType.ELECTRIC));
        assertEquals("Solo el beat", Labels.of(KeyboardDisplayMode.ONLY_BEAT));
    }

    @Test
    void translatesTheOrientationToItsSpanishName() {
        assertEquals("Vertical", Labels.of(Orientation.PORTRAIT));
        assertEquals("Horizontal", Labels.of(Orientation.LANDSCAPE));
    }

    @ParameterizedTest
    @EnumSource(Orientation.class)
    void everyOrientationHasALabelThatIsNotItsName(Orientation value) {
        String label = Labels.of(value);

        assertFalse(label.isBlank());
        assertNotEquals(value.name(), label);
    }

    @Test
    void translatesThePaperFormatWithItsDimensionsInMillimeters() {
        assertEquals("A4 (210 x 297 mm)", Labels.of(PaperFormat.A4));
        assertEquals("Carta (216 x 279 mm)", Labels.of(PaperFormat.LETTER));
    }

    @ParameterizedTest
    @EnumSource(PaperFormat.class)
    void everyPaperFormatHasALabelThatIsNotItsName(PaperFormat value) {
        String label = Labels.of(value);

        assertFalse(label.isBlank());
        assertNotEquals(value.name(), label);
    }

    @Test
    void translatesTheTripletFeel() {
        assertEquals("Corcheas con swing", Labels.of(TripletFeel.EIGHTH));
    }

    @ParameterizedTest
    @EnumSource(TripletFeel.class)
    void everyTripletFeelHasSpanishAndEnglishText(TripletFeel value) {
        String key = "domain.TripletFeel." + value.name();

        assertFalse(Labels.of(value).isBlank());
        assertFalse(Texts.forLocale(Locale.ENGLISH).text(key).isBlank());
    }

    @Test
    void translatesTheLineBreak() {
        assertEquals("Forzar salto", Labels.of(LineBreak.FORCED));
    }

    @ParameterizedTest
    @EnumSource(LineBreak.class)
    void everyLineBreakHasSpanishAndEnglishText(LineBreak value) {
        String key = "domain.LineBreak." + value.name();

        assertFalse(Labels.of(value).isBlank());
        assertFalse(Texts.forLocale(Locale.ENGLISH).text(key).isBlank());
    }

    @Test
    void translatesTheBeamBreak() {
        assertEquals("Forzar corte", Labels.of(BeamBreak.FORCED));
    }

    @ParameterizedTest
    @EnumSource(BeamBreak.class)
    void everyBeamBreakHasSpanishAndEnglishText(BeamBreak value) {
        String key = "domain.BeamBreak." + value.name();

        assertFalse(Labels.of(value).isBlank());
        assertFalse(Texts.forLocale(Locale.ENGLISH).text(key).isBlank());
    }

    @Test
    void translatesTheStemOverride() {
        assertEquals("Arriba", Labels.of(StemOverride.UP));
    }

    @ParameterizedTest
    @EnumSource(StemOverride.class)
    void everyStemOverrideHasSpanishAndEnglishText(StemOverride value) {
        String key = "domain.StemOverride." + value.name();

        assertFalse(Labels.of(value).isBlank());
        assertFalse(Texts.forLocale(Locale.ENGLISH).text(key).isBlank());
    }

    @Test
    void translatesThePickstrokeDirection() {
        assertEquals("Hacia arriba", Labels.of(PickstrokeDirection.UP));
    }

    @ParameterizedTest
    @EnumSource(PickstrokeDirection.class)
    void everyPickstrokeDirectionHasSpanishAndEnglishText(PickstrokeDirection value) {
        String key = "domain.PickstrokeDirection." + value.name();

        assertFalse(Labels.of(value).isBlank());
        assertFalse(Texts.forLocale(Locale.ENGLISH).text(key).isBlank());
    }

    @Test
    void translatesTheDiagramPlacement() {
        assertEquals("Debajo del título", Labels.of(DiagramPlacement.UNDER_THE_TITLE));
    }

    @ParameterizedTest
    @EnumSource(DiagramPlacement.class)
    void everyDiagramPlacementHasSpanishAndEnglishText(DiagramPlacement value) {
        String key = "domain.DiagramPlacement." + value.name();

        assertFalse(Labels.of(value).isBlank());
        assertFalse(Texts.forLocale(Locale.ENGLISH).text(key).isBlank());
    }

    @Test
    void translatesTheVoicePart() {
        assertEquals("Voz 1", Labels.of(VoicePart.LEAD));
    }

    @ParameterizedTest
    @EnumSource(VoicePart.class)
    void everyVoicePartHasSpanishAndEnglishText(VoicePart value) {
        String key = "domain.VoicePart." + value.name();

        assertFalse(Labels.of(value).isBlank());
        assertFalse(Texts.forLocale(Locale.ENGLISH).text(key).isBlank());
    }
}
