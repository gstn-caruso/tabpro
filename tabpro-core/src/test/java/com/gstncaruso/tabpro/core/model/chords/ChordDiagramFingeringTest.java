package com.gstncaruso.tabpro.core.model.chords;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.effects.Finger;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChordDiagramFingeringTest {

    /** Do mayor abierto: cuerda6 muda, 5=3, 4=2, 3=0, 2=1, 1=0. */
    private static final ChordDiagram OPEN_C = ChordDiagram.named("C", List.of(0, 1, 0, 2, 3, -1));

    /** Fa mayor con cejilla en el primer traste. */
    private static final ChordDiagram BARRE_F = ChordDiagram.named("F", List.of(1, 1, 2, 3, 3, 1));

    @Test
    void unAcordeAbiertoNoNecesitaCejilla() {
        assertFalse(OPEN_C.requiresBarre());
        assertTrue(OPEN_C.barreFret().isEmpty());
    }

    @Test
    void masDeCuatroCuerdasPisadasNecesitanCejilla() {
        assertTrue(BARRE_F.requiresBarre());
        assertEquals(1, BARRE_F.barreFret().orElseThrow());
    }

    @Test
    void elEstiramientoEsLaDistanciaEntreElTrasteMasBajoYElMasAlto() {
        assertEquals(2, OPEN_C.fretSpan());
        assertEquals(2, BARRE_F.fretSpan());
    }

    @Test
    void digitaAutomaticamenteElDoMayorAbierto() {
        ChordDiagram digitado = OPEN_C.autoFingered();
        assertEquals(Finger.INDEX, digitado.fingerOfString(2).orElseThrow());
        assertEquals(Finger.MIDDLE, digitado.fingerOfString(4).orElseThrow());
        assertEquals(Finger.RING, digitado.fingerOfString(5).orElseThrow());
        assertTrue(digitado.fingerOfString(1).isEmpty());
        assertTrue(digitado.fingerOfString(3).isEmpty());
    }

    @Test
    void digitaAutomaticamenteLaCejillaDeFa() {
        ChordDiagram digitado = BARRE_F.autoFingered();
        assertEquals(Finger.INDEX, digitado.fingerOfString(1).orElseThrow());
        assertEquals(Finger.INDEX, digitado.fingerOfString(2).orElseThrow());
        assertEquals(Finger.INDEX, digitado.fingerOfString(6).orElseThrow());
        assertEquals(Finger.MIDDLE, digitado.fingerOfString(3).orElseThrow());
        assertEquals(Finger.RING, digitado.fingerOfString(4).orElseThrow());
        assertEquals(Finger.LITTLE, digitado.fingerOfString(5).orElseThrow());
    }

    @Test
    void unAcordeAbiertoSencilloEsSimple() {
        assertEquals(ChordComplexity.SIMPLE, OPEN_C.complexity());
    }

    @Test
    void unaCejillaEnPrimeraPosicionEsMedia() {
        assertEquals(ChordComplexity.MEDIUM, BARRE_F.complexity());
    }

    @Test
    void unaCejillaLejosDelClavijeroYConMuchoEstiramientoEsComplejo() {
        ChordDiagram lejos = ChordDiagram.named("X", List.of(10, 12, 11, 13, 13, 10));
        assertEquals(ChordComplexity.COMPLEX, lejos.complexity());
    }
}
