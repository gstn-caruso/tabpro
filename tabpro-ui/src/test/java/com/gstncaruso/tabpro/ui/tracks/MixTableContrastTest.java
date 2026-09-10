package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.ui.score.ScoreColors;
import com.gstncaruso.tabpro.ui.theme.Contrast;
import com.gstncaruso.tabpro.ui.theme.PaletteCheck;
import com.gstncaruso.tabpro.ui.theme.PaletteCheck.Pair;
import java.util.List;
import org.junit.jupiter.api.Test;

class MixTableContrastTest {

    @Test
    void everyMarkOfTheMixTableReadsOverItsBackground() {
        PaletteCheck.assertEveryPairReads(List.of(
                new Pair("relleno del deslizador de volumen (VOLUME_LEVEL) / panel de pistas",
                        ScoreColors.VOLUME_LEVEL, ScoreColors.SURFACE, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("relleno del deslizador de volumen (VOLUME_LEVEL) / fila elegida",
                        ScoreColors.VOLUME_LEVEL, ScoreColors.SURFACE_HIGHLIGHT, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("relleno del deslizador de paneo (PAN_LEVEL) / panel de pistas", ScoreColors.PAN_LEVEL,
                        ScoreColors.SURFACE, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("relleno del deslizador de paneo (PAN_LEVEL) / fila elegida", ScoreColors.PAN_LEVEL,
                        ScoreColors.SURFACE_HIGHLIGHT, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("caja numerica del deslizador (PAGE_PAPER) / panel de pistas", ScoreColors.PAGE_PAPER,
                        ScoreColors.SURFACE, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("caja numerica del deslizador (PAGE_PAPER) / fila elegida", ScoreColors.PAGE_PAPER,
                        ScoreColors.SURFACE_HIGHLIGHT, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("numero de la caja (PAGE_INK) / caja numerica (PAGE_PAPER)", ScoreColors.PAGE_INK,
                        ScoreColors.PAGE_PAPER, Contrast.TEXT_MINIMUM_RATIO),
                new Pair("texto atenuado (MUTED_INK) / fila elegida", ScoreColors.MUTED_INK,
                        ScoreColors.SURFACE_HIGHLIGHT, Contrast.TEXT_MINIMUM_RATIO)));
    }
}
