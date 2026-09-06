package com.gstncaruso.tabpro.core.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ScaleLibraryTest {

    private static List<String> namesOf(Scale scale, String tonic) {
        return scale.notesFrom(PitchClass.of(tonic)).stream().map(nota -> nota.pitchClass().name()).toList();
    }

    @Test
    void mayorEsDoReMiFaSolLaSi() {
        assertEquals(List.of("C", "D", "E", "F", "G", "A", "B"), namesOf(ScaleLibrary.major(), "C"));
    }

    @Test
    void dorianoDeReEsElRestoDeLasTeclasBlancas() {
        assertEquals(List.of("D", "E", "F", "G", "A", "B", "C"), namesOf(ScaleLibrary.dorian(), "D"));
    }

    @Test
    void menorNaturalDeLaEsElRestoDeLasTeclasBlancas() {
        assertEquals(List.of("A", "B", "C", "D", "E", "F", "G"), namesOf(ScaleLibrary.naturalMinor(), "A"));
    }

    @Test
    void menorArmonicaLlevaLaSensibleNaturalMayor() {
        assertEquals(List.of("A", "B", "C", "D", "E", "F", "G#"), namesOf(ScaleLibrary.harmonicMinor(), "A"));
    }

    @Test
    void menorMelodicaSoloAlteraLaTercera() {
        assertEquals(List.of("A", "B", "C", "D", "E", "F#", "G#"), namesOf(ScaleLibrary.melodicMinor(), "A"));
    }

    @Test
    void pentatonicaMayorSonCincoNotasSinCuartaNiSeptima() {
        assertEquals(List.of("C", "D", "E", "G", "A"), namesOf(ScaleLibrary.majorPentatonic(), "C"));
    }

    @Test
    void pentatonicaMenorSonCincoNotasConTerceraYSeptimaMenor() {
        assertEquals(List.of("A", "C", "D", "E", "G"), namesOf(ScaleLibrary.minorPentatonic(), "A"));
    }

    @Test
    void bluesAgregaLaQuintaDisminuidaALaPentatonicaMenor() {
        assertEquals(List.of("C", "Eb", "F", "Gb", "G", "Bb"), namesOf(ScaleLibrary.blues(), "C"));
    }

    @Test
    void tonosEnterosSonSeisNotasEquidistantes() {
        assertEquals(List.of("C", "D", "E", "F#", "G#", "A#"), namesOf(ScaleLibrary.wholeTone(), "C"));
    }

    @Test
    void cromaticaTieneLasDoceNotas() {
        assertEquals(12, ScaleLibrary.chromatic().degreeCount());
        assertEquals(
                List.of("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"),
                namesOf(ScaleLibrary.chromatic(), "C"));
    }

    @Test
    void disminuidaTonoSemitonoTieneOchoNotas() {
        assertEquals(
                List.of("C", "D", "Eb", "F", "Gb", "Ab", "A", "B"),
                namesOf(ScaleLibrary.diminishedWholeHalf(), "C"));
    }

    @Test
    void diminuidaDominanteEsSemitonoTono() {
        assertEquals(
                List.of("C", "Db", "Eb", "E", "F#", "G", "A", "Bb"),
                namesOf(ScaleLibrary.diminishedHalfWhole(), "C"));
    }

    @Test
    void laEscalaEspanolaEsUnaFrigiaConTerceraMayor() {
        assertEquals(
                List.of("E", "F", "G#", "A", "B", "C", "D"), namesOf(ScaleLibrary.phrygianDominant(), "E"));
    }

    @Test
    void todasLasEscalasSeDeletreanSinRepetirLetrasDeMasNiSaltearlas() {
        for (Scale scale : ScaleLibrary.all()) {
            List<ScaleTone> notas = scale.notesFrom(PitchClass.of("C"));
            assertEquals(scale.degreeCount(), notas.size(), scale.name());
        }
    }

    @Test
    void ofreceUnaBibliotecaAmplia() {
        assertTrue(ScaleLibrary.all().size() >= 20);
    }
}
