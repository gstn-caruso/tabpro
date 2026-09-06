package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.PercussionKit;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Lee los tres fixtures, que traen el mismo contenido escrito en las tres
 * generaciones del formato: una guitarra en afinacion estandar, dos compases de
 * 4/4 en negras con una escala de Do mayor sobre la quinta cuerda.
 */
class GuitarProFileTest {

    private static final List<Integer> FIRST_MEASURE_FRETS = List.of(3, 5, 7, 8);
    private static final List<Integer> SECOND_MEASURE_FRETS = List.of(3, 5, 7, 9);
    private static final int SCALE_STRING = 5;

    private final GuitarProFile files = new GuitarProFile();

    @ParameterizedTest
    @ValueSource(strings = {"gp3", "gp4", "gp5"})
    void readsTheScoreInformation(String extension) {
        Score score = read(extension);

        assertEquals("TabPro Synthetic Fixture", score.info().title());
        assertEquals(120, score.tempo());
        assertEquals(1, score.trackCount());
        assertEquals(2, score.measureCount());
    }

    @ParameterizedTest
    @ValueSource(strings = {"gp3", "gp4", "gp5"})
    void readsTheTrackAndItsTuning(String extension) {
        Track track = read(extension).track(0);

        assertEquals("Guitar", track.name());
        assertEquals(6, track.stringCount());
        assertEquals(Tuning.standard().strings(), track.tuning().strings());
        assertEquals(24, track.settings().fretCount());
        assertFalse(track.isPercussion());
    }

    @ParameterizedTest
    @ValueSource(strings = {"gp3", "gp4", "gp5"})
    void readsEveryNoteOnItsStringAndFret(String extension) {
        Track track = read(extension).track(0);

        assertEquals(FIRST_MEASURE_FRETS, fretsOf(track.measure(0)));
        assertEquals(SECOND_MEASURE_FRETS, fretsOf(track.measure(1)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"gp3", "gp4", "gp5"})
    void readsFourQuarterNotesPerMeasure(String extension) {
        Measure measure = read(extension).track(0).measure(0);

        assertEquals(TimeSignature.fourFour(), measure.timeSignature());
        assertEquals(4, measure.beats().size());
        assertTrue(measure.isComplete(), "el compás no suma lo que su medida pide");
        assertTrue(measure.beats().stream().noneMatch(beat -> beat.isRest()));
    }

    @Test
    void theThreeGenerationsReadTheSame() {
        Score gp3 = read("gp3");

        assertEquals(fretsOf(gp3.track(0).measure(0)), fretsOf(read("gp4").track(0).measure(0)));
        assertEquals(fretsOf(gp3.track(0).measure(0)), fretsOf(read("gp5").track(0).measure(0)));
    }

    @Test
    void aFileThatIsNotGuitarProIsReported(@TempDir Path folder) throws Exception {
        Path path = folder.resolve("roto.gp5");
        Files.writeString(path, "esto no es un archivo de Guitar Pro, ni de lejos");

        assertThrows(ScoreFileException.class, () -> files.read(path));
    }

    @Test
    void aTruncatedFileIsReported(@TempDir Path folder) throws Exception {
        Path path = folder.resolve("cortado.gp5");
        byte[] whole = Files.readAllBytes(fixture("gp5"));
        Files.write(path, java.util.Arrays.copyOf(whole, whole.length / 2));

        assertThrows(ScoreFileException.class, () -> files.read(path));
    }

    // ---- el fixture con efectos, acordes, varias pistas y percusion ----------

    @ParameterizedTest
    @ValueSource(strings = {"gp3", "gp4", "gp5"})
    void readsEveryTrackOfAScoreWithSeveralOfThem(String extension) {
        Score score = readFeatures(extension);

        assertEquals(3, score.trackCount());
        assertEquals("Lead Guitar", score.track(0).name());
        assertEquals(6, score.track(0).stringCount());
        assertEquals("Bass", score.track(1).name());
        assertEquals(4, score.track(1).stringCount());
    }

    @ParameterizedTest
    @ValueSource(strings = {"gp3", "gp4", "gp5"})
    void readsAPercussionTrackOnItsOwnChannel(String extension) {
        Track drums = readFeatures(extension).track(2);

        assertTrue(drums.isPercussion());
        assertEquals(Channel.PERCUSSION_CHANNEL, drums.channel().number());
        assertEquals(PercussionKit.LINE_COUNT, drums.stringCount());
        assertTrue(PercussionKit.isPlayable(drums.measure(0).beat(0).notes().getFirst().fret()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"gp3", "gp4", "gp5"})
    void readsAChangeOfTimeSignatureInTheMiddle(String extension) {
        Track lead = readFeatures(extension).track(0);

        assertEquals(TimeSignature.fourFour(), lead.measure(1).timeSignature());
        assertEquals(new TimeSignature(3, 4), lead.measure(2).timeSignature());
        assertEquals(TimeSignature.fourFour(), lead.measure(3).timeSignature());
    }

    @ParameterizedTest
    @ValueSource(strings = {"gp3", "gp4", "gp5"})
    void readsTheRepeatAndItsAlternateEndings(String extension) {
        Track lead = readFeatures(extension).track(0);

        assertTrue(lead.measure(0).attributes().repeatOpen());
        assertTrue(lead.measure(2).attributes().repeatCloses());
        assertTrue(lead.measure(2).attributes().hasAlternateEndings());
        assertTrue(lead.measure(3).attributes().hasAlternateEndings());
    }

    @ParameterizedTest
    @ValueSource(strings = {"gp4", "gp5"})
    void readsTheEffectsOfEveryNote(String extension) {
        Measure first = readFeatures(extension).track(0).measure(0);

        assertEquals(Dynamic.FORTE_FORTISSIMO, first.beat(1).notes().getFirst().effects().dynamic());
        assertTrue(first.beat(2).notes().getFirst().effects().bend().isPresent());
        assertTrue(first.beat(3).notes().getFirst().has(Ornament.HAMMER_ON_PULL_OFF));
    }

    @ParameterizedTest
    @ValueSource(strings = {"gp4", "gp5"})
    void readsANaturalHarmonic(String extension) {
        Measure second = readFeatures(extension).track(0).measure(1);

        assertEquals(
                java.util.Optional.of(com.gstncaruso.tabpro.core.model.effects.HarmonicType.NATURAL),
                second.beat(0).notes().getFirst().effects().harmonic());
    }

    @ParameterizedTest
    @ValueSource(strings = {"gp4", "gp5"})
    void readsTheChordDiagramWithTheStringsOfItsTrack() {
        Measure last = readFeatures("gp5").track(0).measure(3);

        var chord = last.beat(0).effects().chord().orElseThrow();
        assertEquals("Cadd9", chord.name());
        assertEquals(6, chord.stringCount());
        assertEquals(List.of(-1, 3, 2, 0, 3, 0), chord.frets());
    }

    /** La segunda voz existe recien en gp5: gp3 y gp4 guardan una sola por compas. */
    @Test
    void readsTheSecondVoiceOfAGp5() {
        Measure first = readFeatures("gp5").track(0).measure(0);

        assertTrue(first.usesTwoVoices());
        assertEquals(2, first.voice(VoicePart.BASS).beatCount());
        assertEquals(
                java.util.Optional.of(com.gstncaruso.tabpro.core.model.effects.SlideType.LEGATO),
                first.voice(VoicePart.BASS).beat(1).notes().getFirst().effects().slide());
    }

    @Test
    void gp3AndGp4KeepASingleVoicePerMeasure() {
        assertFalse(readFeatures("gp3").track(0).measure(0).usesTwoVoices());
        assertFalse(readFeatures("gp4").track(0).measure(0).usesTwoVoices());
    }

    private Score readFeatures(String extension) {
        return files.read(fixtureNamed("tabpro-features", extension));
    }

    private static List<Integer> fretsOf(Measure measure) {
        return measure.beats().stream()
                .flatMap(beat -> beat.noteOn(SCALE_STRING).stream())
                .map(note -> note.fret())
                .toList();
    }

    private Score read(String extension) {
        return files.read(fixture(extension));
    }

    private static Path fixture(String extension) {
        return fixtureNamed("tabpro-synthetic", extension);
    }

    private static Path fixtureNamed(String name, String extension) {
        try {
            return Path.of(GuitarProFileTest.class
                    .getResource("/guitarpro/" + name + "." + extension).toURI());
        } catch (java.net.URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
