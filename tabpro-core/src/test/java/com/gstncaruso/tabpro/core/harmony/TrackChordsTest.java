package com.gstncaruso.tabpro.core.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.DiagramPlacement;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import java.util.List;
import org.junit.jupiter.api.Test;

class TrackChordsTest {

    private static final ChordDiagram AM = ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1));
    private static final ChordDiagram C = ChordDiagram.named("C", List.of(0, 1, 0, 2, 3, -1));

    @Test
    void unaPistaSinAcordesNoTieneNinguno() {
        Track pista = Track.standardGuitar("Guitarra");
        assertTrue(TrackChords.usedIn(pista).isEmpty());
    }

    @Test
    void listaLosAcordesEnOrdenDeAparicion() {
        Measure compas1 = new Measure(TimeSignature.fourFour(), List.of(beatWithChord(AM)));
        Measure compas2 = new Measure(TimeSignature.fourFour(), List.of(beatWithChord(C)));

        Track pista = Track.standardGuitar("Guitarra").withMeasures(List.of(compas1, compas2));

        assertEquals(List.of(AM, C), TrackChords.usedIn(pista));
    }

    @Test
    void noRepiteElMismoAcordeDosVeces() {
        Measure compas1 = new Measure(TimeSignature.fourFour(), List.of(beatWithChord(AM), beatWithChord(AM)));

        Track pista = Track.standardGuitar("Guitarra").withMeasures(List.of(compas1));

        assertEquals(List.of(AM), TrackChords.usedIn(pista));
    }

    @Test
    void underTheTitleIgnoraLasPistasQueNoLoPidieron() {
        Measure compas1 = new Measure(TimeSignature.fourFour(), List.of(beatWithChord(AM)));
        Track pista = Track.standardGuitar("Guitarra").withMeasures(List.of(compas1));
        // ABOVE_THE_STAFF es el placement por defecto: no pidio salir debajo del titulo.
        Score partitura = new Score("", 120, List.of(pista));

        assertTrue(TrackChords.underTheTitle(partitura).isEmpty());
    }

    @Test
    void underTheTitleTraeLosAcordesDeLaPistaQueLoPidio() {
        Measure compas1 = new Measure(TimeSignature.fourFour(), List.of(beatWithChord(AM)));
        Measure compas2 = new Measure(TimeSignature.fourFour(), List.of(beatWithChord(C)));
        Track pista = Track.standardGuitar("Guitarra")
                .withMeasures(List.of(compas1, compas2))
                .mappingSettings(settings -> settings.withDisplay(
                        settings.display().withDiagrams(DiagramPlacement.UNDER_THE_TITLE)));
        Score partitura = new Score("", 120, List.of(pista));

        assertEquals(List.of(AM, C), TrackChords.underTheTitle(partitura));
    }

    @Test
    void underTheTitleTambienValeParaElPlacementEnLosDosLados() {
        Measure compas1 = new Measure(TimeSignature.fourFour(), List.of(beatWithChord(AM)));
        Track pista = Track.standardGuitar("Guitarra")
                .withMeasures(List.of(compas1))
                .mappingSettings(settings -> settings.withDisplay(
                        settings.display().withDiagrams(DiagramPlacement.BOTH)));
        Score partitura = new Score("", 120, List.of(pista));

        assertEquals(List.of(AM), TrackChords.underTheTitle(partitura));
    }

    @Test
    void underTheTitleMezclaLasPistasEnOrdenYNoRepiteElMismoNombre() {
        Measure compasConAm = new Measure(TimeSignature.fourFour(), List.of(beatWithChord(AM)));
        Measure compasConC = new Measure(TimeSignature.fourFour(), List.of(beatWithChord(C)));
        Track primeraPista = Track.standardGuitar("Guitarra 1")
                .withMeasures(List.of(compasConAm))
                .mappingSettings(settings -> settings.withDisplay(
                        settings.display().withDiagrams(DiagramPlacement.UNDER_THE_TITLE)));
        Track segundaPista = Track.standardGuitar("Guitarra 2")
                .withMeasures(List.of(compasConAm, compasConC))
                .mappingSettings(settings -> settings.withDisplay(
                        settings.display().withDiagrams(DiagramPlacement.UNDER_THE_TITLE)));
        Score partitura = new Score("", 120, List.of(primeraPista, segundaPista));

        assertEquals(List.of(AM, C), TrackChords.underTheTitle(partitura));
    }

    private static Beat beatWithChord(ChordDiagram chord) {
        return Beat.rest(Duration.quarter()).withEffects(BeatEffects.none().withChord(chord));
    }
}
