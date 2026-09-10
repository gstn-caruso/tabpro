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
                new Pair("volume slider fill (VOLUME_LEVEL) / track panel",
                        ScoreColors.VOLUME_LEVEL, ScoreColors.SURFACE, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("volume slider fill (VOLUME_LEVEL) / chosen row",
                        ScoreColors.VOLUME_LEVEL, ScoreColors.SURFACE_HIGHLIGHT, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("pan slider fill (PAN_LEVEL) / track panel", ScoreColors.PAN_LEVEL,
                        ScoreColors.SURFACE, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("pan slider fill (PAN_LEVEL) / chosen row", ScoreColors.PAN_LEVEL,
                        ScoreColors.SURFACE_HIGHLIGHT, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("slider number box (PAGE_PAPER) / track panel", ScoreColors.PAGE_PAPER,
                        ScoreColors.SURFACE, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("slider number box (PAGE_PAPER) / chosen row", ScoreColors.PAGE_PAPER,
                        ScoreColors.SURFACE_HIGHLIGHT, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("box number (PAGE_INK) / number box (PAGE_PAPER)", ScoreColors.PAGE_INK,
                        ScoreColors.PAGE_PAPER, Contrast.TEXT_MINIMUM_RATIO),
                new Pair("muted text (MUTED_INK) / chosen row", ScoreColors.MUTED_INK,
                        ScoreColors.SURFACE_HIGHLIGHT, Contrast.TEXT_MINIMUM_RATIO)));
    }
}
