package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.ui.score.ScoreColors;
import com.gstncaruso.tabpro.ui.theme.Contrast;
import com.gstncaruso.tabpro.ui.theme.PaletteCheck;
import com.gstncaruso.tabpro.ui.theme.PaletteCheck.Pair;
import java.util.List;
import org.junit.jupiter.api.Test;

class MeasureGridContrastTest {

    @Test
    void theColumnThatIsPlayingStandsOutFromTheGrid() {
        PaletteCheck.assertEveryPairReads(List.of(
                new Pair("tinte del compas que suena (PLAYING_TINT) / panel de pistas",
                        PaletteCheck.compositeOver(MeasureGrid.PLAYING_TINT, ScoreColors.SURFACE),
                        ScoreColors.SURFACE, Contrast.GRAPHICAL_MINIMUM_RATIO)));
    }

    @Test
    void anEmptyMeasureStandsOutFromTheGrid() {
        PaletteCheck.assertEveryPairReads(List.of(
                new Pair("compas vacio (EMPTY_MEASURE) / panel de pistas", ScoreColors.EMPTY_MEASURE,
                        ScoreColors.SURFACE, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("compas vacio (EMPTY_MEASURE) / fila elegida", ScoreColors.EMPTY_MEASURE,
                        ScoreColors.SURFACE_HIGHLIGHT, Contrast.GRAPHICAL_MINIMUM_RATIO)));
    }
}
