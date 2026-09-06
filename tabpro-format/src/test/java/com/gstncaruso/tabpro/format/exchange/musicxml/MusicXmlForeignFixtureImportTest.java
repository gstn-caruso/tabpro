package com.gstncaruso.tabpro.format.exchange.musicxml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * {@link MusicXmlRoundTripTest} solo prueba que el exportador y el importador se den la
 * razon entre ellos: si los dos entienden mal el mismo elemento, el viaje de ida y vuelta
 * sale perfecto igual. Estos fixtures estan escritos a mano, con la forma en que MuseScore,
 * Finale o Sibelius emiten MusicXML de verdad -nunca con {@link MusicXmlScoreExporter}-, para
 * que el importador se mida contra el estandar y no contra si mismo.
 */
class MusicXmlForeignFixtureImportTest {

    private final MusicXmlScoreImporter importer = new MusicXmlScoreImporter();

    private Score importFixture(String name) {
        try {
            Path path = Path.of(MusicXmlForeignFixtureImportTest.class
                    .getResource("/musicxml/" + name + ".musicxml").toURI());
            return importer.importScore(path);
        } catch (java.net.URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void laArmaduraDelArchivoSeRespeta() {
        Score score = importFixture("armadura-en-fa");

        assertEquals(-1, score.attributesOf(0).keySignature().accidentals(),
                "fa mayor son 1 bemol (fifths=-1), no la armadura de do mayor por defecto");

        Track track = score.track(0);
        Measure measure = track.measure(0);
        List<Beat> beats = measure.beats();
        assertEquals(65, track.pitchOf(beats.get(0).notes().get(0)).midiNumber(), "Fa4");
        assertEquals(67, track.pitchOf(beats.get(1).notes().get(0)).midiNumber(), "Sol4");
        assertEquals(69, track.pitchOf(beats.get(2).notes().get(0)).midiNumber(), "La4");
        assertEquals(70, track.pitchOf(beats.get(3).notes().get(0)).midiNumber(), "Si b4, con su alteracion explicita");
    }

    @Test
    void unSilencioDeCompasEnteroSinTypeOcupaTodoElCompas() {
        Score score = importFixture("silencio-de-compas-completo");

        Measure measure = score.track(0).measure(0);
        Beat beat = measure.beat(0);

        assertTrue(beat.isRest(), "el unico note del compas es <rest measure=\"yes\"/>");
        assertTrue(measure.isComplete(),
                "un silencio de compas entero en 3/4 tiene que ocupar los 3 tiempos, no 1 negra por defecto");
        assertFalse(measure.isTooShort());
    }
}
