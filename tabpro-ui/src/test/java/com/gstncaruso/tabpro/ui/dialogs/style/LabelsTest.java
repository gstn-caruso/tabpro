package com.gstncaruso.tabpro.ui.dialogs.style;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.gstncaruso.tabpro.core.harmony.Chord;
import com.gstncaruso.tabpro.core.harmony.ChordType;
import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.harmony.Scale;
import com.gstncaruso.tabpro.core.harmony.ScaleLibrary;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.TuningLibrary;
import com.gstncaruso.tabpro.core.model.chords.ChordComplexity;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.ui.instruments.FretboardDisplayMode;
import com.gstncaruso.tabpro.ui.instruments.FretboardType;
import com.gstncaruso.tabpro.ui.instruments.KeyboardDisplayMode;
import com.gstncaruso.tabpro.ui.instruments.NoteNameMode;
import com.gstncaruso.tabpro.ui.instruments.ScaleLabelMode;
import com.gstncaruso.tabpro.ui.instruments.ScaleType;
import com.gstncaruso.tabpro.ui.harmony.BarrePreference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * El unico punto que traduce un tipo del dominio a su texto en castellano para
 * mostrarlo en un combo o una lista: ningun combo debe apoyarse en toString().
 */
class LabelsTest {

    @Test
    void traduceLaFiguraDeNota() {
        assertEquals("Negra", Labels.of(NoteValue.QUARTER));
    }

    @ParameterizedTest
    @EnumSource(NoteValue.class)
    void todaFiguraDeNotaTieneUnaEtiquetaPropia(NoteValue value) {
        String etiqueta = Labels.of(value);

        assertFalse(etiqueta.isBlank());
        assertNotEquals(value.name(), etiqueta);
    }

    @Test
    void traduceElTipoDeAcordeMayorAlSufijoQueUsaElManual() {
        assertEquals("M", Labels.of(ChordType.MAJOR));
    }

    @Test
    void traduceElTipoDeAcordeASuSufijoMusical() {
        assertEquals("m7", Labels.of(ChordType.MINOR_SEVENTH));
    }

    @ParameterizedTest
    @EnumSource(ChordType.class)
    void todoTipoDeAcordeTieneUnaEtiquetaPropia(ChordType value) {
        String etiqueta = Labels.of(value);

        assertFalse(etiqueta.isBlank());
        assertNotEquals(value.name(), etiqueta);
    }

    @Test
    void traduceLaComplejidadDeLasPosiciones() {
        assertEquals("Todas", Labels.of(ChordComplexity.COMPLEX));
    }

    @ParameterizedTest
    @EnumSource(ChordComplexity.class)
    void todaComplejidadDePosicionesTieneUnaEtiquetaPropia(ChordComplexity value) {
        String etiqueta = Labels.of(value);

        assertFalse(etiqueta.isBlank());
        assertNotEquals(value.name(), etiqueta);
    }

    @Test
    void traduceLaPreferenciaDeCejilla() {
        assertEquals("Cualquiera", Labels.of(BarrePreference.ANY));
    }

    @ParameterizedTest
    @EnumSource(BarrePreference.class)
    void todaPreferenciaDeCejillaTieneUnaEtiquetaPropia(BarrePreference value) {
        String etiqueta = Labels.of(value);

        assertFalse(etiqueta.isBlank());
        assertNotEquals(value.name(), etiqueta);
    }

    @Test
    void traduceLaNotaConSuNombreEnCastellanoEntreParentesis() {
        assertEquals("C (Do)", Labels.of(PitchClass.of("C")));
    }

    @Test
    void laEtiquetaDeUnaNotaNuncaCoincideConSuToString() {
        PitchClass fSharp = PitchClass.of("F#");

        assertNotEquals(fSharp.toString(), Labels.of(fSharp));
    }

    @Test
    void traduceLaEscalaConSuNombreEnCastellano() {
        assertEquals("Mayor (Jonico)", Labels.of(ScaleLibrary.major()));
    }

    @Test
    void todaEscalaDeLaBibliotecaTieneUnaEtiquetaQueNoEsSuToString() {
        for (Scale scale : ScaleLibrary.all()) {
            String etiqueta = Labels.of(scale);

            assertFalse(etiqueta.isBlank());
            assertNotEquals(scale.toString(), etiqueta);
        }
    }

    @Test
    void traduceLaAfinacionConSuNombreYElResumenDeCuerdas() {
        assertEquals("Guitarra estandar (EADGBE)", Labels.of(TuningLibrary.standardGuitar()));
    }

    @Test
    void todaAfinacionDeLaBibliotecaTieneUnaEtiquetaQueNoEsSuToString() {
        for (Tuning tuning : TuningLibrary.guitars()) {
            String etiqueta = Labels.of(tuning);

            assertFalse(etiqueta.isBlank());
            assertNotEquals(tuning.toString(), etiqueta);
        }
    }

    @Test
    void traduceLaDinamicaASuSimboloMusical() {
        assertEquals("mf", Labels.of(Dynamic.MEZZO_FORTE));
    }

    @ParameterizedTest
    @EnumSource(Dynamic.class)
    void todaDinamicaTieneUnaEtiquetaPropia(Dynamic value) {
        String etiqueta = Labels.of(value);

        assertFalse(etiqueta.isBlank());
        assertNotEquals(value.name(), etiqueta);
    }

    @Test
    void traduceElAcordeConSuNombreEnVezDelRecordCrudo() {
        Chord chord = Chord.of(PitchClass.of("C"), ChordType.MINOR_SEVENTH);

        assertEquals("Cm7", Labels.of(chord));
    }

    @Test
    void traduceElTipoDeEscalaDelDiapason() {
        assertEquals("Mayor", Labels.of(ScaleType.MAJOR));
    }

    @ParameterizedTest
    @EnumSource(ScaleType.class)
    void todoTipoDeEscalaDelDiapasonTieneUnaEtiquetaPropia(ScaleType value) {
        String etiqueta = Labels.of(value);

        assertFalse(etiqueta.isBlank());
        assertNotEquals(value.name(), etiqueta);
    }

    @Test
    void traduceLosModosDelDiapasonYElTeclado() {
        assertEquals("Solo el beat", Labels.of(FretboardDisplayMode.ONLY_BEAT));
        assertEquals("Sin nombres", Labels.of(NoteNameMode.NONE));
        assertEquals("Nombre", Labels.of(ScaleLabelMode.NAME));
        assertEquals("Electrica", Labels.of(FretboardType.ELECTRIC));
        assertEquals("Solo el beat", Labels.of(KeyboardDisplayMode.ONLY_BEAT));
    }
}
