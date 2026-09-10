package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.ui.theme.Contrast;
import com.gstncaruso.tabpro.ui.theme.PaletteCheck;
import com.gstncaruso.tabpro.ui.theme.PaletteCheck.Pair;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * La paleta clara: lo que {@link PaperGraphics} deja en la hoja del Modo Pagina y del Modo
 * Pergamino, traduciendo cada color de {@link ScoreColors} con {@link ScoreColors#onPaper}
 * igual que lo hace la pintura real.
 */
class ScoreColorsPaperContrastTest {

    @Test
    void everyMarkOfThePaperPaletteReadsOverTheSheet() {
        Pair opaque = pair("tinta (INK) / hoja", ScoreColors.INK, Contrast.TEXT_MINIMUM_RATIO);
        PaletteCheck.assertEveryPairReads(List.of(
                opaque,
                pair("etiqueta (LABEL) / hoja", ScoreColors.LABEL, Contrast.TEXT_MINIMUM_RATIO),
                pair("tinta atenuada (MUTED_INK) / hoja", ScoreColors.MUTED_INK, Contrast.TEXT_MINIMUM_RATIO),
                pair("numero de compas (MEASURE_NUMBER) / hoja", ScoreColors.MEASURE_NUMBER,
                        Contrast.TEXT_MINIMUM_RATIO),
                pair("voz inactiva (VOICE_INACTIVE) / hoja", ScoreColors.VOICE_INACTIVE,
                        Contrast.TEXT_MINIMUM_RATIO),
                pair("linea del pentagrama (STAFF_LINE) / hoja", ScoreColors.STAFF_LINE,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                pair("linea de compas (BAR_LINE) / hoja", ScoreColors.BAR_LINE, Contrast.GRAPHICAL_MINIMUM_RATIO),
                pair("cursor de edicion (CURSOR) / hoja", ScoreColors.CURSOR, Contrast.GRAPHICAL_MINIMUM_RATIO),
                translucent("estela del cursor (CURSOR_DIMMED) / hoja", ScoreColors.CURSOR_DIMMED,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                pair("linea de reproduccion (PLAYING) / hoja", ScoreColors.PLAYING,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                pair("marca de pista elegida (ACCENT) / hoja", ScoreColors.ACCENT,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                pair("cambio de parametro (PARAMETER_CHANGE) / hoja", ScoreColors.PARAMETER_CHANGE,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                pair("borde de compas incompleto (INCOMPLETE_MEASURE) / hoja", ScoreColors.INCOMPLETE_MEASURE,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                pair("borde de seleccion (ACCENT) / hoja", ScoreColors.ACCENT, Contrast.GRAPHICAL_MINIMUM_RATIO),
                translucent("nota correspondiente (CORRESPONDING_NOTE) / hoja", ScoreColors.CORRESPONDING_NOTE,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("encabezado de pagina (PAGE_INK) / hoja", ScoreColors.PAGE_INK, ScoreColors.PAGE_PAPER,
                        Contrast.TEXT_MINIMUM_RATIO),
                new Pair("pie de pagina atenuado (PAGE_MUTED) / hoja", ScoreColors.PAGE_MUTED, ScoreColors.PAGE_PAPER,
                        Contrast.TEXT_MINIMUM_RATIO)));
    }

    private static Pair pair(String description, java.awt.Color color, double minimumRatio) {
        return new Pair(description, ScoreColors.onPaper(color), ScoreColors.PAGE_PAPER, minimumRatio);
    }

    private static Pair translucent(String description, java.awt.Color color, double minimumRatio) {
        java.awt.Color onSheet = ScoreColors.onPaper(color);
        return new Pair(description, PaletteCheck.compositeOver(onSheet, ScoreColors.PAGE_PAPER),
                ScoreColors.PAGE_PAPER, minimumRatio);
    }
}
