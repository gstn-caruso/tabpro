package com.gstncaruso.tabpro.ui.harmony;

import com.gstncaruso.tabpro.ui.theme.Contrast;
import com.gstncaruso.tabpro.ui.theme.PaletteCheck;
import com.gstncaruso.tabpro.ui.theme.PaletteCheck.Pair;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChordDiagramColorsContrastTest {

    @Test
    void everyMarkOnTheDiagramReadsOverItsBackground() {
        PaletteCheck.assertEveryPairReads(List.of(
                new Pair("position name / background", ChordDiagramColors.LABEL, ChordDiagramColors.BACKGROUND,
                        Contrast.TEXT_MINIMUM_RATIO),
                new Pair("fingering on the finger / finger background", ChordDiagramColors.FINGER_INK,
                        ChordDiagramColors.FINGER, Contrast.TEXT_MINIMUM_RATIO),
                new Pair("grid / background", ChordDiagramColors.GRID, ChordDiagramColors.BACKGROUND,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("nut / background", ChordDiagramColors.NUT, ChordDiagramColors.BACKGROUND,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("open string / background", ChordDiagramColors.OPEN_STRING, ChordDiagramColors.BACKGROUND,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("muted string / background", ChordDiagramColors.MUTED_STRING, ChordDiagramColors.BACKGROUND,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("finger / background", ChordDiagramColors.FINGER, ChordDiagramColors.BACKGROUND,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("barre / background", ChordDiagramColors.BARRE, ChordDiagramColors.BACKGROUND,
                        Contrast.GRAPHICAL_MINIMUM_RATIO)));
    }
}
