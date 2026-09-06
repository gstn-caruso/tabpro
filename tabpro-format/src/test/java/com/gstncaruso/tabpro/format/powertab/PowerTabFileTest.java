package com.gstncaruso.tabpro.format.powertab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Los fixtures son del propio repositorio de powertabeditor
 * (powertab/powertabeditor, GPLv3, test/formats/powertab_old/data/): archivos
 * reales usados por su propia suite de tests, cada uno probando una seccion
 * del formato. Los valores esperados replican los de esa suite.
 */
class PowerTabFileTest {

    private final PowerTabFile files = new PowerTabFile();

    @Test
    void readsTheSongInformation() {
        Score score = read("song_header");

        assertEquals("Some Title", score.title());
        assertEquals("Some Artist", score.info().artist());
        assertEquals("Some Author", score.info().musicAuthor());
        assertEquals("Some Lyricist", score.info().lyricsAuthor());
        assertEquals("2001", score.info().copyright());
    }

    /**
     * La score de guitarra tiene 2 guitarras definidas pero un solo pentagrama
     * (el "guitar in" asigna la primera): la segunda guitarra, sin pentagrama
     * propio, no genera pista. La score de bajo aporta la tercera.
     */
    @Test
    void readsTheGuitarsFromBothScoresAsSeparateTracks() {
        Score score = read("guitars");

        assertEquals(2, score.trackCount());
        assertEquals("First Player", score.track(0).name());
        assertEquals(4, score.track(1).stringCount());
    }

    /**
     * La barra interna (doble barra, la menor con 2 sostenidos, 5/8) cierra el
     * primer compas y abre el segundo: la doble barra queda en el compas que
     * cierra, la armadura y la medida nuevas en el que abre.
     */
    @Test
    void readsADoubleBarAndAMinorKeyWithAnOddMeter() {
        Score score = read("barlines");
        Measure first = score.track(0).measure(0);
        Measure second = score.track(0).measure(1);

        assertTrue(first.attributes().doubleBar());
        assertEquals(Mode.MINOR, second.attributes().keySignature().mode());
        assertEquals(2, second.attributes().keySignature().accidentals());
        assertEquals(5, second.timeSignature().beats());
        assertEquals(8, second.timeSignature().beatUnit());
    }

    @Test
    void readsTheStavesAndTheirStringCounts() {
        Score score = read("staves");

        assertEquals(3, score.trackCount());
        assertEquals(6, score.track(0).stringCount());
        assertEquals(7, score.track(1).stringCount());
    }

    @Test
    void readsTheNotesAndTheirEffects() {
        Score score = read("notes");
        Measure first = score.track(0).measure(0);

        var note1 = first.beat(0).notes().getFirst();
        assertEquals(4, note1.string());
        assertEquals(3, note1.fret());
        assertTrue(note1.has(Ornament.GHOST));
        assertTrue(note1.has(Ornament.HAMMER_ON_PULL_OFF));

        var note2 = first.beat(1).notes().getFirst();
        assertEquals(java.util.Optional.of(HarmonicType.NATURAL), note2.effects().harmonic());
    }

    /** El final alternativo esta anclado en la posicion de la barra interna: abre el segundo compas. */
    @Test
    void readsTheAlternateEndingNumbers() {
        Score score = read("alternate_endings");
        Measure measure = score.track(0).measure(1);

        assertTrue(measure.attributes().hasAlternateEndings());
        assertEquals(java.util.List.of(2, 3), measure.attributes().alternateEndings());
    }

    @Test
    void readsTheStandardTempoMarker() {
        Score score = read("tempo_markers");

        assertEquals(99, score.tempo());
    }

    @ParameterizedTest
    @ValueSource(strings = {"positions", "merge_multibar_rests"})
    void aMultibarRestIsReportedInsteadOfGuessed(String name) {
        assertThrows(ScoreFileException.class, () -> read(name));
    }

    @Test
    void aGuitarReassignmentIsReportedInsteadOfGuessed() {
        assertThrows(ScoreFileException.class, () -> read("guitar_ins"));
    }

    /**
     * Estos fixtures ejercitan secciones que se descartan a proposito (diagramas
     * de acorde, texto de acorde, texto flotante, direcciones, dinamicas, bend,
     * volume swell, tremolo bar): lo que importa aca es que el lector no pierda
     * la sincronia del archivo al saltearlas, no el contenido que se descarta.
     */
    @ParameterizedTest
    @ValueSource(strings = {
        "chord_diagrams", "chordtext", "floating_text", "directions", "bends", "tremolo_bars", "volume_swells"
    })
    void aFileWithDiscardedSectionsStillReadsItsNotes(String name) {
        Score score = read(name);

        assertTrue(score.trackCount() >= 1);
        assertTrue(score.track(0).measureCount() >= 1);
    }

    @Test
    void aFileThatIsNotPowerTabIsReported() {
        assertThrows(ScoreFileException.class, () -> files.read("esto no es PowerTab".getBytes()));
    }

    private Score read(String name) {
        return files.read(PowerTabHeaderReaderTest.bytesOf(name));
    }
}
