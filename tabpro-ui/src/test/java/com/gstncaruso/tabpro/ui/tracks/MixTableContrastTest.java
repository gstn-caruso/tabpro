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
                new Pair("cuerpo de la perilla (KNOB_BODY) / panel de pistas", ScoreColors.KNOB_BODY,
                        ScoreColors.SURFACE, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("cuerpo de la perilla (KNOB_BODY) / fila elegida", ScoreColors.KNOB_BODY,
                        ScoreColors.SURFACE_HIGHLIGHT, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("texto atenuado (MUTED_INK) / fila elegida", ScoreColors.MUTED_INK,
                        ScoreColors.SURFACE_HIGHLIGHT, Contrast.TEXT_MINIMUM_RATIO)));
    }
}
