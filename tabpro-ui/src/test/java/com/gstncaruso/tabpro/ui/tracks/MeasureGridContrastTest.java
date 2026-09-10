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
                new Pair("playing measure tint (PLAYING_TINT) / track panel",
                        PaletteCheck.compositeOver(MeasureGrid.PLAYING_TINT, ScoreColors.SURFACE),
                        ScoreColors.SURFACE, Contrast.GRAPHICAL_MINIMUM_RATIO)));
    }

    @Test
    void anEmptyMeasureStandsOutFromTheGrid() {
        PaletteCheck.assertEveryPairReads(List.of(
                new Pair("empty measure (EMPTY_MEASURE) / track panel", ScoreColors.EMPTY_MEASURE,
                        ScoreColors.SURFACE, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("empty measure (EMPTY_MEASURE) / chosen row", ScoreColors.EMPTY_MEASURE,
                        ScoreColors.SURFACE_HIGHLIGHT, Contrast.GRAPHICAL_MINIMUM_RATIO)));
    }
}
