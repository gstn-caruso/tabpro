package com.gstncaruso.tabpro.format.exchange.musicxml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuplet;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

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
    void theFileKeySignatureIsRespected() {
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
    void aFullBarRestWithoutTypeFillsTheWholeBar() {
        Score score = importFixture("silencio-de-compas-completo");

        Measure measure = score.track(0).measure(0);
        Beat beat = measure.beat(0);

        assertTrue(beat.isRest(), "el unico note del compas es <rest measure=\"yes\"/>");
        assertTrue(measure.isComplete(),
                "un silencio de compas entero en 3/4 tiene que ocupar los 3 tiempos, no 1 negra por defecto");
        assertFalse(measure.isTooShort());
    }

    @Test
    void theTieAndTheDotCrossTheBar() {
        Score score = importFixture("ligadura-entre-compases");
        Track track = score.track(0);

        Beat firstBar = track.measure(0).beat(0);
        assertFalse(firstBar.notes().get(0).tied(), "la nota que ataca no viene marcada como ligada");
        assertEquals(NoteValue.QUARTER, firstBar.duration().value());
        assertTrue(firstBar.duration().dotted(), "quarter+dot en el archivo es una negra con puntillo");

        Beat secondBar = track.measure(1).beat(0);
        assertTrue(secondBar.notes().get(0).tied(), "tie type=\"stop\" es la continuacion, no un nuevo ataque");
        assertEquals(NoteValue.QUARTER, secondBar.duration().value());
        assertFalse(secondBar.duration().dotted());
    }

    @Test
    void theEighthNoteTripletIsReadWithForeignDivisions() {
        Score score = importFixture("tresillo-de-corcheas");
        Measure measure = score.track(0).measure(0);

        for (int i = 0; i < 3; i++) {
            Beat beat = measure.beat(i);
            assertEquals(NoteValue.EIGHTH, beat.duration().value(), "beat " + i);
            assertEquals(Tuplet.of(3), beat.duration().tuplet(), "beat " + i + ": 3 en el tiempo de 2");
        }
        assertTrue(measure.isComplete(), "el tresillo mas las tres negras tienen que completar el 4/4");
    }

    @Test
    void theTablatureInDropDDoesNotUseStandardTuning() {
        Score score = importFixture("tablatura-en-drop-d");
        Track track = score.track(0);

        assertEquals(List.of(64, 59, 55, 50, 45, 38),
                track.tuning().strings().stream().map(Pitch::midiNumber).toList(),
                "staff-tuning listado de la linea 1 a la 6 tiene que armar Drop D, no la estandar");

        Beat firstBeat = track.measure(0).beat(0);
        assertEquals(4, firstBeat.notes().get(0).string(),
                "el archivo pone la nota explicita en la cuerda 4; recalcular la mejor cuerda la pondria en la 3");
        assertEquals(7, firstBeat.notes().get(0).fret());

        Beat chordBeat = track.measure(0).beat(1);
        assertEquals(2, chordBeat.notes().size(), "el chord/ suma la segunda nota al mismo beat");
        assertEquals(0, chordBeat.noteOn(3).orElseThrow().fret());
        assertEquals(0, chordBeat.noteOn(4).orElseThrow().fret());

        Beat beatWithoutTechnical = track.measure(0).beat(2);
        assertEquals(6, beatWithoutTechnical.notes().get(0).string(),
                "sin <technical>, Re2 solo puede resolverse al aire en la cuerda 6 de la Drop D");
        assertEquals(0, beatWithoutTechnical.notes().get(0).fret());
    }
}
