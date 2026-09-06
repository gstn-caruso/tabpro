package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import java.util.List;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class ChordDiagramCanvasTest {

    private static final int WIDTH = 220;
    private static final int HEIGHT = 260;

    @Test
    void laCuerdaSeisQuedaALaIzquierdaYLaUnoALaDerecha() {
        ChordDiagramCanvas canvas = sized(new ChordDiagramCanvas());

        assertTrue(canvas.stringX(6) < canvas.stringX(1), "la mas grave va a la izquierda, como en el manual");
        assertTrue(canvas.stringX(6) < canvas.stringX(5));
    }

    @Test
    void lasCuerdasEstanParejas() {
        ChordDiagramCanvas canvas = sized(new ChordDiagramCanvas());

        assertEquals(
                canvas.stringX(2) - canvas.stringX(1),
                canvas.stringX(6) - canvas.stringX(5));
    }

    @Test
    void cadaFilaDeTrasteQuedaDebajoDeLaAnterior() {
        ChordDiagramCanvas canvas = sized(new ChordDiagramCanvas());

        assertTrue(canvas.fretRowY(0) < canvas.fretRowY(1));
        assertTrue(canvas.fretRowY(1) < canvas.fretRowY(2));
    }

    @Test
    void identificaLaCuerdaDelClicPorSuPosicionX() {
        ChordDiagramCanvas canvas = sized(new ChordDiagramCanvas());

        for (int cuerda = 1; cuerda <= 6; cuerda++) {
            assertEquals(OptionalInt.of(cuerda), canvas.stringAt(canvas.stringX(cuerda)));
        }
    }

    @Test
    void noHayCuerdaFueraDelDiagrama() {
        ChordDiagramCanvas canvas = sized(new ChordDiagramCanvas());

        assertEquals(OptionalInt.empty(), canvas.stringAt(-100));
        assertEquals(OptionalInt.empty(), canvas.stringAt(WIDTH + 100));
    }

    @Test
    void unClicEnLaGrillaDaElTrasteAbsolutoSegunElTrasteBase() {
        ChordDiagramCanvas canvas = sized(new ChordDiagramCanvas());
        canvas.show(ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1)), Tuning.standard());

        assertEquals(OptionalInt.of(1), canvas.fretAt(canvas.fretRowY(0)));
        assertEquals(OptionalInt.of(2), canvas.fretAt(canvas.fretRowY(1)));
    }

    @Test
    void unDiagramaEnPosicionAltaEmpiezaLaGrillaEnSuTrasteBase() {
        ChordDiagramCanvas canvas = sized(new ChordDiagramCanvas());
        ChordDiagram enQuintoTraste = ChordDiagrams.withBaseFret(
                ChordDiagram.named("Am (cejilla)", List.of(5, 5, 5, 7, 7, 5)), 5);
        canvas.show(enQuintoTraste, Tuning.standard());

        assertEquals(OptionalInt.of(5), canvas.fretAt(canvas.fretRowY(0)));
        assertEquals(OptionalInt.of(6), canvas.fretAt(canvas.fretRowY(1)));
    }

    @Test
    void elEncabezadoSoloApareceCuandoElTrasteBaseEsUno() {
        ChordDiagramCanvas canvas = sized(new ChordDiagramCanvas());
        canvas.show(ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1)), Tuning.standard());
        assertTrue(canvas.hasHeader());

        canvas.show(ChordDiagrams.withBaseFret(ChordDiagram.named("X", List.of(5, 5, 5, 7, 7, 5)), 5), Tuning.standard());
        assertFalse(canvas.hasHeader());
    }

    @Test
    void identificaElEncabezadoDeCadaCuerda() {
        ChordDiagramCanvas canvas = sized(new ChordDiagramCanvas());
        canvas.show(ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1)), Tuning.standard());

        assertEquals(OptionalInt.of(6), canvas.stringAt(canvas.stringX(6)));
        assertTrue(canvas.isHeaderRow(canvas.headerY()));
        assertFalse(canvas.isHeaderRow(canvas.fretRowY(2)));
    }

    private static ChordDiagramCanvas sized(ChordDiagramCanvas canvas) {
        canvas.setSize(WIDTH, HEIGHT);
        return canvas;
    }
}
