package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.PercussionKit;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * La prueba mas dura: leer un archivo (aunque sea sintetico, generado a mano con el mismo
 * layout binario que un Guitar Pro real, y no con la API de tabpro) con
 * {@link GuitarProFile}, exportarlo con {@link GuitarProExporter} y volver a leerlo. Lo que
 * GP4 no puede representar (la segunda voz de gp5) se documenta explicitamente, no se
 * ignora.
 */
class GuitarProExporterFixtureRoundTripTest {

    private final GuitarProFile files = new GuitarProFile();
    private final GuitarProExporter exporter = new GuitarProExporter();

    private Score roundTrip(Score original) {
        return files.read(exporter.write(original));
    }

    private Score readFixture(String name, String extension) {
        try {
            Path path = Path.of(GuitarProExporterFixtureRoundTripTest.class
                    .getResource("/guitarpro/" + name + "." + extension).toURI());
            return files.read(path);
        } catch (java.net.URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void elFixtureSintéticoDeGp4SobreviveElViajeCompleto() {
        Score original = readFixture("tabpro-synthetic", "gp4");

        Score reread = roundTrip(original);

        assertEquals(original.info().title(), reread.info().title());
        assertEquals(original.tempo(), reread.tempo());
        assertEquals(original.trackCount(), reread.trackCount());
        assertEquals(original.measureCount(), reread.measureCount());
        assertEquals(fretsOf(original.track(0), 5), fretsOf(reread.track(0), 5));
    }

    @Test
    void elFixtureDeFeaturesEnGp4SobreviveElViajeCompleto() {
        Score original = readFixture("tabpro-features", "gp4");

        Score reread = roundTrip(original);

        assertEquals(3, reread.trackCount());
        assertEquals("Lead Guitar", reread.track(0).name());
        assertEquals("Bass", reread.track(1).name());
        assertEquals(4, reread.track(1).stringCount());
        assertTrue(reread.track(2).isPercussion());
        assertEquals(Channel.PERCUSSION_CHANNEL, reread.track(2).channel().number());
        assertTrue(PercussionKit.isPlayable(reread.track(2).measure(0).beat(0).notes().getFirst().fret()));

        assertEquals(TimeSignature.fourFour(), reread.track(0).measure(1).timeSignature());
        assertEquals(new TimeSignature(3, 4), reread.track(0).measure(2).timeSignature());
        assertEquals(TimeSignature.fourFour(), reread.track(0).measure(3).timeSignature());

        assertTrue(reread.track(0).measure(0).attributes().repeatOpen());
        assertTrue(reread.track(0).measure(2).attributes().repeatCloses());
        assertTrue(reread.track(0).measure(2).attributes().hasAlternateEndings());

        Measure first = reread.track(0).measure(0);
        assertEquals(Dynamic.FORTE_FORTISSIMO, first.beat(1).notes().getFirst().effects().dynamic());
        assertTrue(first.beat(2).notes().getFirst().effects().bend().isPresent());
        assertTrue(first.beat(3).notes().getFirst().has(Ornament.HAMMER_ON_PULL_OFF));

        Measure second = reread.track(0).measure(1);
        assertEquals(java.util.Optional.of(HarmonicType.NATURAL), second.beat(0).notes().getFirst().effects().harmonic());

        var chord = reread.track(0).measure(3).beat(0).effects().chord().orElseThrow();
        assertEquals("Cadd9", chord.name());
        assertEquals(6, chord.stringCount());
        assertEquals(List.of(-1, 3, 2, 0, 3, 0), chord.frets());
    }

    /**
     * El fixture gp5 trae segunda voz, que solo existe desde gp5: al exportar a GP4 se
     * pierde a proposito, y lo que se pierde queda listado en {@code warningsFor}.
     */
    @Test
    void elFixtureDeFeaturesEnGp5PierdeLaSegundaVozAlExportarAGp4() {
        Score original = readFixture("tabpro-features", "gp5");
        assertTrue(original.track(0).measure(0).usesTwoVoices(), "el fixture de origen trae segunda voz");

        assertTrue(exporter.warningsFor(original).stream().anyMatch(w -> w.contains("segunda voz")));

        Score reread = roundTrip(original);

        assertFalse(reread.track(0).measure(0).usesTwoVoices());
        // La voz principal, en cambio, sobrevive entera.
        assertEquals(fretsOf(original.track(0), 5), fretsOf(reread.track(0), 5));
        assertEquals(3, reread.trackCount());
        assertTrue(reread.track(2).isPercussion());
    }

    @Test
    void elFixtureDeEfectosEnGp4SobreviveElViajeCompleto() {
        Score original = readFixture("tabpro-effects2", "gp4");

        Score reread = roundTrip(original);

        Measure first = reread.track(0).measure(0);
        assertTrue(first.beat(0).notes().getFirst().has(Ornament.HAMMER_ON_PULL_OFF));
        assertTrue(first.beat(1).notes().getFirst().tied());
        assertTrue(first.beat(2).notes().getFirst().has(Ornament.VIBRATO));

        Measure second = reread.track(0).measure(1);
        assertEquals(java.util.Optional.of(HarmonicType.ARTIFICIAL), second.beat(0).notes().getFirst().effects().harmonic());
        assertEquals(java.util.Optional.of(HarmonicType.TAPPED), second.beat(1).notes().getFirst().effects().harmonic());
        assertEquals(java.util.Optional.of(HarmonicType.PINCH), second.beat(2).notes().getFirst().effects().harmonic());

        Measure third = reread.track(0).measure(2);
        assertEquals(java.util.Optional.of(HarmonicType.SEMI), third.beat(0).notes().getFirst().effects().harmonic());
    }

    private static List<Integer> fretsOf(Track track, int string) {
        return track.measures().stream()
                .flatMap(measure -> measure.beats().stream())
                .flatMap(beat -> beat.noteOn(string).stream())
                .map(note -> note.fret())
                .toList();
    }
}
