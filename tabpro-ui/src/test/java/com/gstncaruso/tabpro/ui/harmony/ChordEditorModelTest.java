package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.harmony.Chord;
import com.gstncaruso.tabpro.core.harmony.ChordType;
import com.gstncaruso.tabpro.core.harmony.Interval;
import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.chords.ChordComplexity;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import com.gstncaruso.tabpro.core.model.effects.Finger;
import java.util.List;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ChordEditorModelTest {

    private final Preferences scratch = Preferences.userRoot().node("tabpro-test/" + getClass().getSimpleName());
    private final FingeringMemory fingeringMemory = new FingeringMemory(scratch);
    private final ChordEditorModel model = new ChordEditorModel(Tuning.standard(), true, fingeringMemory);

    @AfterEach
    void limpiarElNodoDePrueba() throws BackingStoreException {
        scratch.removeNode();
    }

    @Test
    void empiezaEnDoMayorConTodosLosDiagramas() {
        assertEquals(PitchClass.of("C"), model.selection().root());
        assertEquals(ChordType.MAJOR, model.selection().type());
        assertFalse(model.isCustom());
        assertFalse(model.candidates().isEmpty());
        assertEquals(model.candidates().get(0), model.current());
    }

    @Test
    void elegirOtraFundamentalRearmaLosDiagramas() {
        model.selectRoot(PitchClass.of("G"));

        assertEquals(PitchClass.of("G"), model.selection().root());
        assertTrue(model.current().name().startsWith("G"));
        assertFalse(model.isCustom());
    }

    @Test
    void elegirElTipoRearmaLosDiagramas() {
        model.selectType(ChordType.MINOR_SEVENTH);

        assertEquals(ChordType.MINOR_SEVENTH, model.selection().type());
        assertEquals("Cm7", model.current().name());
    }

    @Test
    void elegirElBajoArmaUnaInversion() {
        model.selectBass(PitchClass.of("E"));

        assertEquals("C/E", model.current().name());
        assertTrue(model.selection().chord().isInverted());
    }

    @Test
    void elFiltroDeComplejidadDejaAfueraLosDificiles() {
        model.selectType(ChordType.MAJOR_SEVENTH);
        model.selectComplexity(ChordComplexity.SIMPLE);

        assertTrue(model.candidates().stream().noneMatch(ChordDiagram::requiresBarre));
    }

    @Test
    void elFiltroDeCejillaSoloDejaLosQueLaNecesitan() {
        model.selectRoot(PitchClass.of("F"));
        model.selectBarrePreference(BarrePreference.FORCE);

        assertFalse(model.candidates().isEmpty());
        assertTrue(model.candidates().stream().allMatch(ChordDiagram::requiresBarre));
    }

    @Test
    void elFiltroDeCejillaProhibidaSacaLosQueLaNecesitan() {
        model.selectRoot(PitchClass.of("F"));
        model.selectBarrePreference(BarrePreference.FORBID);

        assertTrue(model.candidates().stream().noneMatch(ChordDiagram::requiresBarre));
    }

    @Test
    void elegirUnDiagramaDeLaListaCLoHaceElPrincipal() {
        model.selectType(ChordType.SEVENTH);
        ChordDiagram otro = model.candidates().get(model.candidates().size() - 1);

        model.pickCandidate(otro);

        assertEquals(otro, model.current());
        assertFalse(model.isCustom());
    }

    @Test
    void ofreceNombresAlternativosParaElDiagramaPrincipal() {
        // Do mayor sin la fundamental en el bajo grave suena igual que Do6 sin la sexta, etc.
        // Alcanza con verificar que el nombre elegido este entre los alternativos.
        assertTrue(model.alternativeNames().stream().anyMatch(chord -> chord.name().equals("C")));
    }

    @Test
    void elegirUnNombreAlternativoRearmaLaZonaA() {
        model.selectType(ChordType.MINOR_SEVENTH);
        Chord alternativo = Chord.of(PitchClass.of("F"), ChordType.SIXTH);

        model.pickAlternativeName(alternativo);

        assertEquals(PitchClass.of("F"), model.selection().root());
        assertEquals(ChordType.SIXTH, model.selection().type());
        assertEquals("F6", model.current().name());
    }

    @Test
    void tocarUnaCuerdaEnUnTrastePasaAModoPersonalizado() {
        model.toggleFret(1, 3);

        assertTrue(model.isCustom());
        assertEquals("", model.current().name());
        assertEquals(3, model.current().fretOfString(1));
    }

    @Test
    void tocarLaMismaNotaDeNuevoLaSaca() {
        model.toggleFret(1, 0); // la primera cuerda al aire ya suena en Do mayor

        model.toggleFret(1, 0);

        assertEquals(ChordDiagram.MUTED, model.current().fretOfString(1));
    }

    @Test
    void elEncabezadoAlternaEntreCuerdaAlAireYCuerdaMuda() {
        model.toggleFret(2, 3);

        model.toggleOpenOrMuted(2);
        assertEquals(0, model.current().fretOfString(2));

        model.toggleOpenOrMuted(2);
        assertEquals(ChordDiagram.MUTED, model.current().fretOfString(2));

        model.toggleOpenOrMuted(2);
        assertEquals(0, model.current().fretOfString(2));
    }

    @Test
    void editarUnDedoQuedaEnLaDigitacion() {
        model.setFinger(1, Finger.LITTLE);

        assertEquals(Finger.LITTLE, model.current().fingerOfString(1).orElseThrow());
    }

    @Test
    void clickearElNumeroPasaAlDedoSiguienteYLuegoVuelveASinDedo() {
        model.toggleFret(1, 3);
        model.setFinger(1, null);

        model.cycleFinger(1);
        assertEquals(Finger.THUMB, model.current().fingerOfString(1).orElseThrow());

        model.cycleFinger(1);
        assertEquals(Finger.INDEX, model.current().fingerOfString(1).orElseThrow());

        for (int i = 0; i < Finger.values().length - 1; i++) {
            model.cycleFinger(1);
        }
        assertTrue(model.current().fingerOfString(1).isEmpty(), "despues del ultimo dedo vuelve a no tener ninguno");
    }

    @Test
    void unaCuerdaAlAireOMudaNoSeDigita() {
        model.toggleOpenOrMuted(1);

        model.cycleFinger(1);

        assertTrue(model.current().fingerOfString(1).isEmpty());
    }

    @Test
    void editarUnDedoLoMemorizaParaLaMismaFormaEnOtroAcorde() {
        model.selectRoot(PitchClass.of("F"));
        ChordDiagram cejillaDeFa = model.candidates().stream()
                .filter(ChordDiagram::requiresBarre)
                .findFirst()
                .orElseThrow(() -> new AssertionError("hace falta una posicion con cejilla para Fa mayor"));
        model.pickCandidate(cejillaDeFa);

        model.setFinger(1, Finger.LITTLE);

        model.selectRoot(PitchClass.of("G"));
        List<ChordDiagram> mismaForma = model.candidates().stream()
                .filter(diagrama -> diagrama.shape().equals(cejillaDeFa.shape()))
                .toList();
        assertFalse(mismaForma.isEmpty(), "Sol mayor tiene que ofrecer la misma forma de cejilla que Fa");
        assertTrue(
                mismaForma.stream().allMatch(diagrama -> diagrama.fingerOfString(1).equals(Optional.of(Finger.LITTLE))),
                "la digitacion corregida a mano se reusa en la forma parecida");
    }

    @Test
    void cambiarDeAcordeEmpiezaSinNingunTonoOmitido() {
        model.setToneOmitted(Interval.PERFECT_FIFTH, true);

        model.selectType(ChordType.MINOR);

        assertTrue(model.omittedTones().isEmpty());
    }

    @Test
    void losTonosQueSePuedenOmitirSonLosDeLaFormulaDelAcorde() {
        model.selectType(ChordType.SEVENTH);

        assertEquals(
                List.of(Interval.ROOT, Interval.MAJOR_THIRD, Interval.PERFECT_FIFTH, Interval.MINOR_SEVENTH),
                model.omittableTones());
    }

    @Test
    void omitirUnTonoOfreceDiagramasQueNoLoNecesitan() {
        int sinOmitir = model.candidates().size();

        model.setToneOmitted(Interval.PERFECT_FIFTH, true);

        assertTrue(model.candidates().size() > sinOmitir, "omitir la quinta suma posiciones nuevas");
        assertTrue(model.omittedTones().contains(Interval.PERFECT_FIFTH));
    }

    @Test
    void destildarUnTonoLoVuelveAExigir() {
        model.setToneOmitted(Interval.PERFECT_FIFTH, true);

        model.setToneOmitted(Interval.PERFECT_FIFTH, false);

        assertTrue(model.omittedTones().isEmpty());
    }

    @Test
    void moverElTrasteBaseNoTocaLoQueYaEstaPisado() {
        var frets = model.current().frets();

        model.setBaseFret(5);

        assertEquals(5, model.current().baseFret());
        assertEquals(frets, model.current().frets());
    }

    @Test
    void escribirElNombreACualquierCosaSoloValeEnModoPersonalizado() {
        model.toggleFret(1, 3);

        model.setCustomName("Mi acorde raro");

        assertEquals("Mi acorde raro", model.current().name());
    }

    @Test
    void siNoSeUsaElDiagramaElResultadoSoloMuestraElNombre() {
        model.setUseDiagram(false);

        assertFalse(model.result().shown());
        assertTrue(model.current().shown(), "la zona B sigue mostrando el diagrama mientras se edita");
    }

    @Test
    void siNoSeUsaLaDigitacionElResultadoNoLaLleva() {
        model.setShowFingering(false);

        assertTrue(model.result().fingering().stream().allMatch(finger -> finger == null));
    }

    @Test
    void porDefectoElResultadoUsaDiagramaYDigitacion() {
        ChordDiagram resultado = model.result();

        assertTrue(resultado.shown());
        assertEquals(model.current().fingering(), resultado.fingering());
    }

    @Test
    void laPreferenciaDeMostrarElBajoSePuedeApagar() {
        ChordEditorModel sinBajo = new ChordEditorModel(Tuning.standard(), false, fingeringMemory);
        sinBajo.selectBass(PitchClass.of("E"));

        assertEquals("C", sinBajo.current().name());
    }
}
