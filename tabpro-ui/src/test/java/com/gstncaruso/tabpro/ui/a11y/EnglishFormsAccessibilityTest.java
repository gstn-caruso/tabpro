package com.gstncaruso.tabpro.ui.a11y;

import static org.junit.jupiter.api.Named.named;

import com.gstncaruso.tabpro.core.files.AudioQuality;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.TripletFeel;
import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendType;
import com.gstncaruso.tabpro.core.model.effects.GraceNote;
import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import com.gstncaruso.tabpro.core.model.effects.Stroke;
import com.gstncaruso.tabpro.core.model.effects.StrokeDirection;
import com.gstncaruso.tabpro.core.model.effects.TremoloPicking;
import com.gstncaruso.tabpro.core.model.effects.Trill;
import com.gstncaruso.tabpro.ui.dialogs.ascii.AsciiExportPanel;
import com.gstncaruso.tabpro.ui.dialogs.ascii.AsciiImportPanel;
import com.gstncaruso.tabpro.ui.dialogs.effects.BendPanel;
import com.gstncaruso.tabpro.ui.dialogs.effects.GraceNotePanel;
import com.gstncaruso.tabpro.ui.dialogs.effects.HarmonicPanel;
import com.gstncaruso.tabpro.ui.dialogs.effects.StrokePanel;
import com.gstncaruso.tabpro.ui.dialogs.effects.TremoloPickingPanel;
import com.gstncaruso.tabpro.ui.dialogs.effects.TrillPanel;
import com.gstncaruso.tabpro.ui.dialogs.info.DefaultScorePropertiesPanel;
import com.gstncaruso.tabpro.ui.dialogs.info.NewScoreDefaults;
import com.gstncaruso.tabpro.ui.dialogs.info.ScoreInfoPanel;
import com.gstncaruso.tabpro.ui.dialogs.instrument.InstrumentPanel;
import com.gstncaruso.tabpro.ui.dialogs.measure.AlternateEndingsPanel;
import com.gstncaruso.tabpro.ui.dialogs.measure.DirectionsPanel;
import com.gstncaruso.tabpro.ui.dialogs.measure.KeySignaturePanel;
import com.gstncaruso.tabpro.ui.dialogs.measure.RepeatPanel;
import com.gstncaruso.tabpro.ui.dialogs.measure.TimeSignaturePanel;
import com.gstncaruso.tabpro.ui.dialogs.measure.TripletFeelPanel;
import com.gstncaruso.tabpro.ui.dialogs.metronome.MetronomePanel;
import com.gstncaruso.tabpro.ui.dialogs.metronome.MetronomeSettings;
import com.gstncaruso.tabpro.ui.dialogs.midi.RecordingPlayer;
import com.gstncaruso.tabpro.ui.dialogs.pagesetup.PageSetupPanel;
import com.gstncaruso.tabpro.ui.dialogs.preferences.Preferences;
import com.gstncaruso.tabpro.ui.dialogs.preferences.PreferencesPanel;
import com.gstncaruso.tabpro.ui.dialogs.print.PrintPanel;
import com.gstncaruso.tabpro.ui.dialogs.track.TrackPropertiesPanel;
import com.gstncaruso.tabpro.ui.dialogs.wave.WaveExportPanel;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.page.PageSetup;
import java.awt.Container;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@Isolated
class EnglishFormsAccessibilityTest {

    static Stream<Named<Supplier<Container>>> mainForms() {
        return Stream.of(
                named("Preferences", () -> new PreferencesPanel(Preferences.defaults())),
                named("Track Properties", () -> new TrackPropertiesPanel(Track.standardGuitar("Guitar"), new RecordingPlayer())),
                named("Score Information", () -> new ScoreInfoPanel(ScoreInfo.empty())),
                named("Default Score Properties", () -> new DefaultScorePropertiesPanel(
                        new NewScoreDefaults(120, new TimeSignature(4, 4), KeySignature.cMajor(), "", ""))),
                named("Page Setup", () -> new PageSetupPanel(PageSetup.defaults())),
                named("Print", () -> new PrintPanel(10)),
                named("Time Signature", () -> new TimeSignaturePanel(new TimeSignature(4, 4))),
                named("Key Signature", () -> new KeySignaturePanel(KeySignature.cMajor())),
                named("Triplet Feel", () -> new TripletFeelPanel(TripletFeel.EIGHTH)),
                named("Repeat", () -> new RepeatPanel(false, 0)),
                named("Alternate Endings", () -> new AlternateEndingsPanel(List.of())),
                named("Directions", () -> new DirectionsPanel(Optional.empty(), Optional.empty())),
                named("Bend", () -> new BendPanel(Bend.of(BendType.BEND, 4))),
                named("Grace Note", () -> new GraceNotePanel(GraceNote.before(0))),
                named("Stroke", () -> new StrokePanel(Stroke.of(StrokeDirection.DOWN))),
                named("Trill", () -> new TrillPanel(Trill.to(0))),
                named("Tremolo Picking", () -> new TremoloPickingPanel(TremoloPicking.at(NoteValue.SIXTEENTH))),
                named("Harmonics", () -> new HarmonicPanel(HarmonicType.NATURAL)),
                named("Metronome", () -> new MetronomePanel(120, new MetronomeSettings(true, 80))),
                named("Instrument", () -> new InstrumentPanel(0)),
                named("Wave Export", () -> new WaveExportPanel(new AudioQuality(44_100, 16, 2))),
                named("ASCII Import", AsciiImportPanel::new),
                named("ASCII Export", AsciiExportPanel::new));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("mainForms")
    void everyMainFormInEnglishIsAccessibleAndHasNoMnemonicClashes(Supplier<Container> form) {
        Texts.install(Locale.ENGLISH);
        try {
            AccessibilityAssertions.assertNoViolations(form.get());
        } finally {
            Texts.install(Locale.forLanguageTag("es"));
        }
    }
}
