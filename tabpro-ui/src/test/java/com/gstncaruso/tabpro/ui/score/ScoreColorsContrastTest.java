package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.ui.instruments.FretboardType;
import com.gstncaruso.tabpro.ui.instruments.InstrumentColors;
import com.gstncaruso.tabpro.ui.theme.Contrast;
import com.gstncaruso.tabpro.ui.theme.PaletteCheck;
import com.gstncaruso.tabpro.ui.theme.PaletteCheck.Pair;
import com.gstncaruso.tabpro.ui.tracks.TrackColors;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScoreColorsContrastTest {

    @Test
    void everyMarkOfTheDarkPaletteReadsOverItsBackground() {
        PaletteCheck.assertEveryPairReads(pairs());
    }

    private static List<Pair> pairs() {
        List<Pair> pairs = new ArrayList<>(List.of(
                new Pair("ink (INK) / background", ScoreColors.INK, ScoreColors.BACKGROUND,
                        Contrast.TEXT_MINIMUM_RATIO),
                new Pair("label (LABEL) / background", ScoreColors.LABEL, ScoreColors.BACKGROUND,
                        Contrast.TEXT_MINIMUM_RATIO),
                new Pair("muted ink (MUTED_INK) / background", ScoreColors.MUTED_INK, ScoreColors.BACKGROUND,
                        Contrast.TEXT_MINIMUM_RATIO),
                new Pair("measure number (MEASURE_NUMBER) / background", ScoreColors.MEASURE_NUMBER,
                        ScoreColors.BACKGROUND, Contrast.TEXT_MINIMUM_RATIO),
                new Pair("tempo (TEMPO) / background", ScoreColors.TEMPO, ScoreColors.BACKGROUND,
                        Contrast.TEXT_MINIMUM_RATIO),
                new Pair("inactive voice (VOICE_INACTIVE) / background", ScoreColors.VOICE_INACTIVE, ScoreColors.BACKGROUND,
                        Contrast.TEXT_MINIMUM_RATIO),
                new Pair("label (LABEL) / track panel", ScoreColors.LABEL, ScoreColors.SURFACE,
                        Contrast.TEXT_MINIMUM_RATIO),
                new Pair("muted ink (MUTED_INK) / track panel", ScoreColors.MUTED_INK, ScoreColors.SURFACE,
                        Contrast.TEXT_MINIMUM_RATIO),
                new Pair("warning (WARNING) / score background", ScoreColors.WARNING, ScoreColors.BACKGROUND,
                        Contrast.TEXT_MINIMUM_RATIO),
                new Pair("warning (WARNING) / track panel", ScoreColors.WARNING, ScoreColors.SURFACE,
                        Contrast.TEXT_MINIMUM_RATIO),
                new Pair("staff line (STAFF_LINE) / background", ScoreColors.STAFF_LINE, ScoreColors.BACKGROUND,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("bar line (BAR_LINE) / background", ScoreColors.BAR_LINE, ScoreColors.BACKGROUND,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("staff line (STAFF_LINE) / percussion panel", ScoreColors.STAFF_LINE,
                        ScoreColors.SURFACE, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("border (BORDER) / track panel", ScoreColors.BORDER, ScoreColors.SURFACE,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("title bar (TITLE_BAR) / track panel", ScoreColors.TITLE_BAR, ScoreColors.SURFACE,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("title bar ink (TITLE_BAR_INK) / title bar (TITLE_BAR)",
                        ScoreColors.TITLE_BAR_INK, ScoreColors.TITLE_BAR, Contrast.TEXT_MINIMUM_RATIO),
                new Pair("edit cursor (CURSOR) / background", ScoreColors.CURSOR, ScoreColors.BACKGROUND,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("cursor trail (CURSOR_DIMMED) / background",
                        PaletteCheck.compositeOver(ScoreColors.CURSOR_DIMMED, ScoreColors.BACKGROUND),
                        ScoreColors.BACKGROUND, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("playback line (PLAYING) / background", ScoreColors.PLAYING, ScoreColors.BACKGROUND,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("chosen track mark (ACCENT) / background", ScoreColors.ACCENT, ScoreColors.BACKGROUND,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("parameter change (PARAMETER_CHANGE) / background", ScoreColors.PARAMETER_CHANGE,
                        ScoreColors.BACKGROUND, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("incomplete measure border (INCOMPLETE_MEASURE) / background", ScoreColors.INCOMPLETE_MEASURE,
                        ScoreColors.BACKGROUND, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("selection border (SELECTION_BORDER) / background", ScoreColors.SELECTION_BORDER,
                        ScoreColors.BACKGROUND, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("ink over the selection fill (INK) / selection fill",
                        ScoreColors.INK,
                        PaletteCheck.compositeOver(ScoreColors.SELECTION, ScoreColors.BACKGROUND),
                        Contrast.TEXT_MINIMUM_RATIO),
                new Pair("corresponding note (CORRESPONDING_NOTE) / background",
                        PaletteCheck.compositeOver(ScoreColors.CORRESPONDING_NOTE, ScoreColors.BACKGROUND),
                        ScoreColors.BACKGROUND, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("marker's default color (Marker.DEFAULT_COLOR) / background",
                        ScoreColors.of(com.gstncaruso.tabpro.core.model.bars.Marker.DEFAULT_COLOR),
                        ScoreColors.BACKGROUND, Contrast.GRAPHICAL_MINIMUM_RATIO)));

        for (int track = 0; track < TrackColors.COUNT; track++) {
            Color colour = TrackColors.of(track);
            pairs.add(new Pair("track color " + track + " / track panel", colour, ScoreColors.SURFACE,
                    Contrast.GRAPHICAL_MINIMUM_RATIO));
            pairs.add(new Pair("track color " + track + " / chosen panel", colour, ScoreColors.SURFACE_HIGHLIGHT,
                    Contrast.GRAPHICAL_MINIMUM_RATIO));
        }

        pairs.addAll(instrumentPairs());
        return pairs;
    }

    private static List<Pair> instrumentPairs() {
        Color whiteKey = InstrumentColors.WHITE_KEY;
        Color blackKey = InstrumentColors.BLACK_KEY;
        List<Pair> pairs = new ArrayList<>(List.of(
                new Pair("marked note (PRESSED) / white key", InstrumentColors.PRESSED, whiteKey,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("marked note (PRESSED) / black key", InstrumentColors.PRESSED, blackKey,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("context note (CONTEXT) / white key", InstrumentColors.CONTEXT, whiteKey,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("context note (CONTEXT) / black key", InstrumentColors.CONTEXT, blackKey,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("mouse ring (HOVER) / white key", InstrumentColors.HOVER, whiteKey,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("mouse ring (HOVER) / black key", InstrumentColors.HOVER, blackKey,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("key edge (KEY_EDGE) / white key", InstrumentColors.KEY_EDGE, whiteKey,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("key edge (KEY_EDGE) / black key", InstrumentColors.KEY_EDGE, blackKey,
                        Contrast.GRAPHICAL_MINIMUM_RATIO)));
        pairs.addAll(fretboardPairs(FretboardType.ELECTRIC));
        pairs.addAll(fretboardPairs(FretboardType.ACOUSTIC));
        pairs.addAll(fretboardPairs(FretboardType.CLASSICAL));
        pairs.addAll(fretboardPairs(FretboardType.BASIC));
        return pairs;
    }

    private static List<Pair> fretboardPairs(FretboardType type) {
        Color wood = type.woodColor();
        String neck = "neck " + type.label().toLowerCase();
        return List.of(
                new Pair("digit over marked note / mark - " + type.label(), type.markInkColor(),
                        type.markColor(), Contrast.TEXT_MINIMUM_RATIO),
                new Pair("context digit / context mark - " + type.label(), type.contextInkColor(),
                        type.contextColor(), Contrast.TEXT_MINIMUM_RATIO),
                new Pair("string / " + neck, type.stringColor(), wood, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("fret / " + neck, type.fretWireColor(), wood, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("inlay / " + neck, type.inlayColor(), wood, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("edge / " + neck, type.edgeColor(), wood, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("marked note / " + neck, type.markColor(), wood, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("context note / " + neck, type.contextColor(), wood, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("focus ring / " + neck, type.hoverColor(), wood, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("caret / " + neck, type.hoverColor(), wood, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("nut / " + neck, type.nutColor(), wood, Contrast.GRAPHICAL_MINIMUM_RATIO));
    }
}
