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
        assertEquals(List.of("Do", "Re", "Mi", "Fa", "Sol", "La", "Si"), namesOf(ScaleLibrary.major(), "Do"));
    }

    @Test
    void dorianoDeReEsElRestoDeLasTeclasBlancas() {
        assertEquals(List.of("Re", "Mi", "Fa", "Sol", "La", "Si", "Do"), namesOf(ScaleLibrary.dorian(), "Re"));
    }

    @Test
    void menorNaturalDeLaEsElRestoDeLasTeclasBlancas() {
        assertEquals(List.of("La", "Si", "Do", "Re", "Mi", "Fa", "Sol"), namesOf(ScaleLibrary.naturalMinor(), "La"));
    }

    @Test
    void menorArmonicaLlevaLaSensibleNaturalMayor() {
        assertEquals(List.of("La", "Si", "Do", "Re", "Mi", "Fa", "Sol#"), namesOf(ScaleLibrary.harmonicMinor(), "La"));
    }

    @Test
    void menorMelodicaSoloAlteraLaTercera() {
        assertEquals(List.of("La", "Si", "Do", "Re", "Mi", "Fa#", "Sol#"), namesOf(ScaleLibrary.melodicMinor(), "La"));
    }

    @Test
    void pentatonicaMayorSonCincoNotasSinCuartaNiSeptima() {
        assertEquals(List.of("Do", "Re", "Mi", "Sol", "La"), namesOf(ScaleLibrary.majorPentatonic(), "Do"));
    }

    @Test
    void pentatonicaMenorSonCincoNotasConTerceraYSeptimaMenor() {
        assertEquals(List.of("La", "Do", "Re", "Mi", "Sol"), namesOf(ScaleLibrary.minorPentatonic(), "La"));
    }

    @Test
    void bluesAgregaLaQuintaDisminuidaALaPentatonicaMenor() {
        assertEquals(List.of("Do", "Mib", "Fa", "Solb", "Sol", "Sib"), namesOf(ScaleLibrary.blues(), "Do"));
    }

    @Test
    void tonosEnterosSonSeisNotasEquidistantes() {
        assertEquals(List.of("Do", "Re", "Mi", "Fa#", "Sol#", "La#"), namesOf(ScaleLibrary.wholeTone(), "Do"));
    }

    @Test
    void cromaticaTieneLasDoceNotas() {
        assertEquals(12, ScaleLibrary.chromatic().degreeCount());
        assertEquals(
                List.of("Do", "Do#", "Re", "Re#", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "La#", "Si"),
                namesOf(ScaleLibrary.chromatic(), "Do"));
    }

    @Test
    void disminuidaTonoSemitonoTieneOchoNotas() {
        assertEquals(
                List.of("Do", "Re", "Mib", "Fa", "Solb", "Lab", "La", "Si"),
                namesOf(ScaleLibrary.diminishedWholeHalf(), "Do"));
    }

    @Test
    void diminuidaDominanteEsSemitonoTono() {
        assertEquals(
                List.of("Do", "Reb", "Mib", "Mi", "Fa#", "Sol", "La", "Sib"),
                namesOf(ScaleLibrary.diminishedHalfWhole(), "Do"));
    }

    @Test
    void laEscalaEspanolaEsUnaFrigiaConTerceraMayor() {
        assertEquals(
                List.of("Mi", "Fa", "Sol#", "La", "Si", "Do", "Re"), namesOf(ScaleLibrary.phrygianDominant(), "Mi"));
    }

    @Test
    void todasLasEscalasSeDeletreanSinRepetirLetrasDeMasNiSaltearlas() {
        for (Scale scale : ScaleLibrary.all()) {
            List<ScaleTone> notas = scale.notesFrom(PitchClass.of("Do"));
            assertEquals(scale.degreeCount(), notas.size(), scale.name());
        }
    }

    @Test
    void ofreceUnaBibliotecaAmplia() {
        assertTrue(ScaleLibrary.all().size() >= 20);
    }
}
