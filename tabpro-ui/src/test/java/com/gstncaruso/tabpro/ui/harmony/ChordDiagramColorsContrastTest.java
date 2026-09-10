package com.gstncaruso.tabpro.ui.harmony;

import com.gstncaruso.tabpro.ui.theme.Contrast;
import com.gstncaruso.tabpro.ui.theme.PaletteCheck;
import com.gstncaruso.tabpro.ui.theme.PaletteCheck.Pair;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * El diagrama de acorde tiene su fondo propio, siempre oscuro sin importar el tema de la
 * ventana: forma parte de la paleta oscura junto con {@code ScoreColorsContrastTest}.
 */
class ChordDiagramColorsContrastTest {

    @Test
    void everyMarkOnTheDiagramReadsOverItsBackground() {
        PaletteCheck.assertEveryPairReads(List.of(
                new Pair("nombre de posicion / fondo", ChordDiagramColors.LABEL, ChordDiagramColors.BACKGROUND,
                        Contrast.TEXT_MINIMUM_RATIO),
                new Pair("digitacion en el dedo / fondo del dedo", ChordDiagramColors.FINGER_INK,
                        ChordDiagramColors.FINGER, Contrast.TEXT_MINIMUM_RATIO),
                new Pair("grilla / fondo", ChordDiagramColors.GRID, ChordDiagramColors.BACKGROUND,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("cejilla (nut) / fondo", ChordDiagramColors.NUT, ChordDiagramColors.BACKGROUND,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("cuerda al aire / fondo", ChordDiagramColors.OPEN_STRING, ChordDiagramColors.BACKGROUND,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("cuerda muda / fondo", ChordDiagramColors.MUTED_STRING, ChordDiagramColors.BACKGROUND,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("dedo / fondo", ChordDiagramColors.FINGER, ChordDiagramColors.BACKGROUND,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("cejilla con barra (barre) / fondo", ChordDiagramColors.BARRE, ChordDiagramColors.BACKGROUND,
                        Contrast.GRAPHICAL_MINIMUM_RATIO)));
    }
}
