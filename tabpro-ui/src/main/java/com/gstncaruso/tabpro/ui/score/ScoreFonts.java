package com.gstncaruso.tabpro.ui.score;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;

final class ScoreFonts {

    private static final List<String> FAMILY_PREFERENCE =
            List.of("Times New Roman", "Liberation Serif", "Tinos", "FreeSerif");

    private static final List<String> SANS_FAMILY_PREFERENCE =
            List.of("Arial", "Liberation Sans", "Arimo", "Helvetica");

    static final String FAMILY = resolveFamily(FAMILY_PREFERENCE, Font.SERIF);

    static final String SANS_FAMILY = resolveFamily(SANS_FAMILY_PREFERENCE, Font.SANS_SERIF);

    static final Font FRET_FONT = new Font(FAMILY, Font.BOLD, 16);
    static final Font TUNING_LEGEND_FONT = new Font(FAMILY, Font.PLAIN, 9);
    static final Font MEASURE_NUMBER_FONT = new Font(FAMILY, Font.PLAIN, 10);

    static final Font EFFECT_SYMBOL_FONT = new Font(FAMILY, Font.BOLD, 9);
    static final Font EFFECT_TEXT_FONT = new Font(FAMILY, Font.ITALIC, 10);

    static final Font FINGER_FONT = new Font(FAMILY, Font.PLAIN, 9);
    static final Font BEND_FONT = new Font(FAMILY, Font.ITALIC, 9);
    static final Font GRACE_FONT = new Font(FAMILY, Font.PLAIN, 8);

    static final Font SECTION_MARK_FONT = new Font(FAMILY, Font.BOLD, 15);
    static final Font ALTERNATE_ENDING_FONT = new Font(FAMILY, Font.PLAIN, 9);
    static final Font REPEAT_COUNT_FONT = new Font(FAMILY, Font.BOLD, 10);

    static final Font TEMPO_FONT = new Font(FAMILY, Font.BOLD, 10);

    static final Font CHORD_NAME_FONT = new Font(FAMILY, Font.BOLD, 10);
    static final Font CHORD_FRET_FONT = new Font(FAMILY, Font.PLAIN, 8);

    static final Font LYRICS_FONT = new Font(FAMILY, Font.PLAIN, 10);
    static final Font TRACK_LABEL_FONT = new Font(FAMILY, Font.BOLD, 11);
    static final Font TUPLET_FONT = new Font(FAMILY, Font.ITALIC, 10);

    static final Font PAGE_TITLE_FONT = new Font(FAMILY, Font.BOLD, 20);
    static final Font PAGE_SUBTITLE_FONT = new Font(FAMILY, Font.PLAIN, 13);
    static final Font PAGE_CREDIT_FONT = new Font(FAMILY, Font.PLAIN, 10);
    static final Font PAGE_FOOTER_FONT = new Font(FAMILY, Font.PLAIN, 9);

    private ScoreFonts() {
    }

    static Font tabMarkFont(int letterHeight) {
        return new Font(SANS_FAMILY, Font.BOLD, letterHeight);
    }

    static Font octaveMarkFont(double staffLineSpacing) {
        return new Font(FAMILY, Font.ITALIC, (int) Math.round(staffLineSpacing * 1.7));
    }

    private static String resolveFamily(List<String> preference, String fallback) {
        List<String> installed =
                Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        for (String candidate : preference) {
            if (installed.contains(candidate)) {
                return candidate;
            }
        }
        return fallback;
    }
}
