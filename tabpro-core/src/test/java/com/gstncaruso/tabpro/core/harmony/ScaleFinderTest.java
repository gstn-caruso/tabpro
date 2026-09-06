package com.gstncaruso.tabpro.core.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScaleFinderTest {

    /** Do Re Mi Fa Sol La Si en Do central: una melodia bien diatonica. */
    private static final List<Pitch> DO_MAYOR =
            List.of(60, 62, 64, 65, 67, 69, 71).stream().map(Pitch::new).toList();

    @Test
    void unaListaVaciaNoTieneCandidatas() {
        assertTrue(ScaleFinder.find(List.of()).isEmpty());
    }

    @Test
    void unaMelodiaDiatonicaEncuentraSuEscalaSinIncidencias() {
        List<ScaleMatch> candidatas = ScaleFinder.find(DO_MAYOR);

        assertEquals(0, candidatas.get(0).incidentNotes());
        assertTrue(candidatas.stream()
                .anyMatch(m -> m.incidentNotes() == 0
                        && m.tonic().equals(PitchClass.of("C"))
                        && m.scale().equals(ScaleLibrary.major())));
    }

    @Test
    void estanOrdenadasPorIncidenciasCrecientes() {
        List<ScaleMatch> candidatas = ScaleFinder.find(DO_MAYOR);
        for (int i = 1; i < candidatas.size(); i++) {
            assertTrue(candidatas.get(i - 1).incidentNotes() <= candidatas.get(i).incidentNotes());
        }
    }

    @Test
    void unaNotaAjenaCuentaComoUnaIncidencia() {
        List<Pitch> conUnaNotaExtranha = new java.util.ArrayList<>(DO_MAYOR);
        conUnaNotaExtranha.add(new Pitch(61)); // Do#, ajeno a Do mayor

        List<ScaleMatch> candidatas = ScaleFinder.find(conUnaNotaExtranha);

        ScaleMatch doMayor = candidatas.stream()
                .filter(m -> m.tonic().equals(PitchClass.of("C")) && m.scale().equals(ScaleLibrary.major()))
                .findFirst()
                .orElseThrow();
        assertEquals(1, doMayor.incidentNotes());
    }

    @Test
    void buscaSobreUnRangoDeCompasesDeUnaPista() {
        Beat beat = Beat.of(
                Duration.quarter(),
                new Note(1, 0),
                new Note(1, 2),
                new Note(1, 4),
                new Note(1, 5),
                new Note(1, 7));
        Measure compas = new Measure(TimeSignature.fourFour(), List.of(beat));
        Track pista = Track.standardGuitar("Guitarra").withMeasures(List.of(compas));

        List<ScaleMatch> candidatas = ScaleFinder.findIn(pista, 0, 0);

        assertEquals(0, candidatas.get(0).incidentNotes());
    }
}
