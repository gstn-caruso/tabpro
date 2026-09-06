package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.harmony.Chord;
import com.gstncaruso.tabpro.core.harmony.ChordType;
import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.model.chords.ChordComplexity;
import org.junit.jupiter.api.Test;

class ChordSelectionTest {

    @Test
    void sinInversionElBajoEsLaFundamental() {
        ChordSelection seleccion = ChordSelection.initial();

        assertEquals(Chord.of(seleccion.root(), seleccion.type()), seleccion.chord());
        assertFalse(seleccion.chord().isInverted());
    }

    @Test
    void elBajoDistintoDeLaFundamentalArmaUnAcordeInvertido() {
        ChordSelection seleccion = ChordSelection.initial().withBass(PitchClass.of("E"));

        assertEquals(
                Chord.inverted(seleccion.root(), seleccion.type(), PitchClass.of("E")),
                seleccion.chord());
        assertTrue(seleccion.chord().isInverted());
    }

    @Test
    void cambiarLaFundamentalConservaElTipoYElFiltro() {
        ChordSelection seleccion = ChordSelection.initial()
                .withType(ChordType.MINOR_SEVENTH)
                .withComplexity(ChordComplexity.SIMPLE)
                .withRoot(PitchClass.of("D"));

        assertEquals(PitchClass.of("D"), seleccion.root());
        assertEquals(ChordType.MINOR_SEVENTH, seleccion.type());
        assertEquals(ChordComplexity.SIMPLE, seleccion.complexity());
    }

    @Test
    void cambiarLaFundamentalMueveElBajoConEllaCuandoNoHabiaInversion() {
        ChordSelection seleccion = ChordSelection.initial().withRoot(PitchClass.of("G"));

        assertEquals(PitchClass.of("G"), seleccion.bass());
    }

    @Test
    void cambiarLaFundamentalNoMueveUnBajoYaElegidoADeliberado() {
        ChordSelection seleccion = ChordSelection.initial()
                .withBass(PitchClass.of("E"))
                .withRoot(PitchClass.of("G"));

        assertEquals(PitchClass.of("E"), seleccion.bass());
    }
}
