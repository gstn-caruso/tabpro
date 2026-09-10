package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.ui.theme.Contrast;
import com.gstncaruso.tabpro.ui.theme.PaletteCheck;
import com.gstncaruso.tabpro.ui.theme.PaletteCheck.Pair;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScoreColorsPaperContrastTest {

    @Test
    void everyMarkOfThePaperPaletteReadsOverTheSheet() {
        Pair opaque = pair("ink (INK) / sheet", ScoreColors.INK, Contrast.TEXT_MINIMUM_RATIO);
        PaletteCheck.assertEveryPairReads(List.of(
                opaque,
                pair("label (LABEL) / sheet", ScoreColors.LABEL, Contrast.TEXT_MINIMUM_RATIO),
                pair("muted ink (MUTED_INK) / sheet", ScoreColors.MUTED_INK, Contrast.TEXT_MINIMUM_RATIO),
                pair("measure number (MEASURE_NUMBER) / sheet", ScoreColors.MEASURE_NUMBER,
                        Contrast.TEXT_MINIMUM_RATIO),
                pair("tempo (TEMPO) / sheet", ScoreColors.TEMPO, Contrast.TEXT_MINIMUM_RATIO),
                pair("inactive voice (VOICE_INACTIVE) / sheet", ScoreColors.VOICE_INACTIVE,
                        Contrast.TEXT_MINIMUM_RATIO),
                pair("staff line (STAFF_LINE) / sheet", ScoreColors.STAFF_LINE,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                pair("bar line (BAR_LINE) / sheet", ScoreColors.BAR_LINE, Contrast.GRAPHICAL_MINIMUM_RATIO),
                pair("edit cursor (CURSOR) / sheet", ScoreColors.CURSOR, Contrast.GRAPHICAL_MINIMUM_RATIO),
                translucent("cursor trail (CURSOR_DIMMED) / sheet", ScoreColors.CURSOR_DIMMED,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                pair("playback line (PLAYING) / sheet", ScoreColors.PLAYING,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                pair("chosen track mark (ACCENT) / sheet", ScoreColors.ACCENT,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                pair("parameter change (PARAMETER_CHANGE) / sheet", ScoreColors.PARAMETER_CHANGE,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                pair("incomplete measure border (INCOMPLETE_MEASURE) / sheet", ScoreColors.INCOMPLETE_MEASURE,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                pair("selection border (SELECTION_BORDER) / sheet", ScoreColors.SELECTION_BORDER,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                translucentOverPaper("ink over the selection fill (PAGE_INK) / selection fill",
                        ScoreColors.SELECTION, Contrast.TEXT_MINIMUM_RATIO),
                translucent("corresponding note (CORRESPONDING_NOTE) / sheet", ScoreColors.CORRESPONDING_NOTE,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("page header (PAGE_INK) / sheet", ScoreColors.PAGE_INK, ScoreColors.PAGE_PAPER,
                        Contrast.TEXT_MINIMUM_RATIO),
                new Pair("muted page footer (PAGE_MUTED) / sheet", ScoreColors.PAGE_MUTED, ScoreColors.PAGE_PAPER,
                        Contrast.TEXT_MINIMUM_RATIO),
                pair("marker's default color (Marker.DEFAULT_COLOR) / sheet",
                        ScoreColors.of(com.gstncaruso.tabpro.core.model.bars.Marker.DEFAULT_COLOR),
                        Contrast.GRAPHICAL_MINIMUM_RATIO)));
    }

    private static Pair pair(String description, java.awt.Color color, double minimumRatio) {
        return new Pair(description, ScoreColors.onPaper(color), ScoreColors.PAGE_PAPER, minimumRatio);
    }

    private static Pair translucent(String description, java.awt.Color color, double minimumRatio) {
        java.awt.Color onSheet = ScoreColors.onPaper(color);
        return new Pair(description, PaletteCheck.compositeOver(onSheet, ScoreColors.PAGE_PAPER),
                ScoreColors.PAGE_PAPER, minimumRatio);
    }

    private static Pair translucentOverPaper(String description, java.awt.Color fill, double minimumRatio) {
        java.awt.Color composedFill = PaletteCheck.compositeOver(ScoreColors.onPaper(fill), ScoreColors.PAGE_PAPER);
        return new Pair(description, ScoreColors.onPaper(ScoreColors.INK), composedFill, minimumRatio);
    }
}
