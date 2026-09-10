package com.gstncaruso.tabpro.ui.status;

import com.gstncaruso.tabpro.ui.score.ScoreColors;
import com.gstncaruso.tabpro.ui.theme.Contrast;
import com.gstncaruso.tabpro.ui.theme.PaletteCheck;
import com.gstncaruso.tabpro.ui.theme.PaletteCheck.Pair;
import java.util.List;
import org.junit.jupiter.api.Test;

class StatusBarContrastTest {

    @Test
    void theBevelOfEveryPanelReadsOverTheStatusBar() {
        PaletteCheck.assertEveryPairReads(List.of(
                new Pair("tinte del bisel hundido (BEVEL_SHADE) / barra de estado", ScoreColors.BEVEL_SHADE,
                        ScoreColors.SURFACE, Contrast.GRAPHICAL_MINIMUM_RATIO)));
    }
}
