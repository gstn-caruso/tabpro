package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.model.ScoreColor;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public final class ScoreColors {

    public static final Color BACKGROUND = new Color(0x1E1F22);
    public static final Color SURFACE = new Color(0x2B2D30);
    public static final Color SURFACE_HIGHLIGHT = new Color(0x35373B);
    public static final Color BORDER = new Color(0x71, 0x77, 0x80);
    public static final Color BEVEL_SHADE = new Color(0x7E, 0x83, 0x8B);
    public static final Color TITLE_BAR = new Color(0x7A, 0x80, 0x89);
    public static final Color TITLE_BAR_INK = new Color(0x0C, 0x0D, 0x0E);

    public static final Color STAFF_LINE = new Color(0x72, 0x76, 0x7F);
    public static final Color BAR_LINE = new Color(0x7E828A);
    public static final Color INK = new Color(0xE8EAED);
    public static final Color LABEL = new Color(0x9DA1A8);
    public static final Color MUTED_INK = new Color(0x9E, 0xA0, 0xA6);
    public static final Color MEASURE_NUMBER = new Color(0xFF, 0x65, 0x63);
    private static final Color MEASURE_NUMBER_DARKENED_TO_MEET_PAPER_CONTRAST = new Color(0xD6, 0x03, 0x00);

    public static final Color ACCENT = new Color(0x3574F0);
    public static final Color VOLUME_LEVEL = new Color(0xE0, 0x7A, 0x3D);
    public static final Color PAN_LEVEL = new Color(0xC7, 0xB5, 0x3A);
    public static final Color CURSOR = new Color(0xFF, 0x3B, 0x30);
    public static final Color CURSOR_DIMMED = new Color(0xFF, 0x3B, 0x30, 0xE5);
    public static final Color PLAYING = new Color(0x24, 0xA2, 0x5A);
    public static final Color PLAYING_MEASURE = new Color(0xE5484D);
    public static final Color WARNING = new Color(0xE5A44A);
    public static final Color EMPTY_MEASURE = new Color(0xB8, 0xBC, 0xC2);

    public static final Color PARAMETER_CHANGE = new Color(0xD32F3B);
    public static final Color TEMPO = new Color(0xFF, 0x65, 0x63);
    public static final Color INCOMPLETE_MEASURE = new Color(0xE5484D);
    public static final Color SELECTION = new Color(0xFF, 0xFF, 0x00, 0x50);
    public static final Color SELECTION_BORDER = new Color(0xFF, 0xFF, 0x00);
    private static final Color SELECTION_BORDER_DARKENED_TO_MEET_PAPER_CONTRAST = new Color(0x8C, 0x8C, 0x00);
    public static final Color CORRESPONDING_NOTE = new Color(0x9D, 0xA1, 0xA8, 0xAF);
    public static final Color VOICE_INACTIVE = new Color(0x8C, 0x8F, 0x94);

    public static final Color PAGE_PAPER = new Color(0xF6F6F2);
    public static final Color PAGE_INK = new Color(0x202124);
    public static final Color PAGE_MUTED = new Color(0x6B6E74);
    public static final Color PAGE_SHADOW = new Color(0, 0, 0, 90);

    private static final Map<Color, Color> ON_PAPER = buildOnPaperMap();

    private ScoreColors() {
    }

    public static Color of(ScoreColor color) {
        return new Color(color.red(), color.green(), color.blue());
    }

    public static Color forDynamic(Dynamic dynamic) {
        double loudness = dynamic.ordinal() / (double) (Dynamic.values().length - 1);
        return interpolated(MUTED_INK, INK, loudness);
    }

    static Color onPaper(Color color) {
        return ON_PAPER.getOrDefault(color, color);
    }

    private static Color mirrored(Color color) {
        return new Color(
                255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue(), color.getAlpha());
    }

    private static Color interpolated(Color from, Color to, double t) {
        return new Color(
                channel(from.getRed(), to.getRed(), t),
                channel(from.getGreen(), to.getGreen(), t),
                channel(from.getBlue(), to.getBlue(), t));
    }

    private static int channel(int from, int to, double t) {
        return (int) Math.round(from + (to - from) * t);
    }

    private static Map<Color, Color> buildOnPaperMap() {
        Map<Color, Color> onPaper = new HashMap<>();
        onPaper.put(BACKGROUND, PAGE_PAPER);
        onPaper.put(INK, PAGE_INK);
        onPaper.put(LABEL, mirrored(LABEL));
        onPaper.put(MUTED_INK, mirrored(MUTED_INK));
        onPaper.put(MEASURE_NUMBER, MEASURE_NUMBER_DARKENED_TO_MEET_PAPER_CONTRAST);
        onPaper.put(SELECTION_BORDER, SELECTION_BORDER_DARKENED_TO_MEET_PAPER_CONTRAST);
        onPaper.put(STAFF_LINE, mirrored(STAFF_LINE));
        onPaper.put(BAR_LINE, mirrored(BAR_LINE));
        onPaper.put(VOICE_INACTIVE, mirrored(VOICE_INACTIVE));
        onPaper.put(CORRESPONDING_NOTE, mirrored(CORRESPONDING_NOTE));
        for (Dynamic dynamic : Dynamic.values()) {
            Color forDynamic = forDynamic(dynamic);
            onPaper.putIfAbsent(forDynamic, mirrored(forDynamic));
        }
        return Map.copyOf(onPaper);
    }
}
