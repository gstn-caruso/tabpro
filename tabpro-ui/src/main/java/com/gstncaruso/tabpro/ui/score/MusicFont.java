package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.ui.font.BravuraFont;
import java.awt.Font;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class MusicFont {

    /** SMuFL U+E050 "gClef". */
    private static final int G_CLEF = 0xE050;
    /** SMuFL U+E062 "fClef". */
    private static final int F_CLEF = 0xE062;
    /** SMuFL U+E080.."E089" "timeSig0".."timeSig9". */
    private static final int TIME_SIG_DIGIT_ZERO = 0xE080;
    /** SMuFL U+E0A4 "noteheadBlack". */
    private static final int NOTEHEAD_BLACK = 0xE0A4;
    /** SMuFL U+E0A3 "noteheadHalf". */
    private static final int NOTEHEAD_HALF = 0xE0A3;
    /** SMuFL U+E0A2 "noteheadWhole". */
    private static final int NOTEHEAD_WHOLE = 0xE0A2;
    /** SMuFL U+E262 "accidentalSharp". */
    private static final int ACCIDENTAL_SHARP = 0xE262;
    /** SMuFL U+E260 "accidentalFlat". */
    private static final int ACCIDENTAL_FLAT = 0xE260;
    /** SMuFL U+E261 "accidentalNatural". */
    private static final int ACCIDENTAL_NATURAL = 0xE261;
    /** SMuFL U+E1E7 "augmentationDot". */
    private static final int AUGMENTATION_DOT = 0xE1E7;
    /** SMuFL U+E4E3 "restWhole". */
    private static final int REST_WHOLE = 0xE4E3;
    /** SMuFL U+E4E4 "restHalf". */
    private static final int REST_HALF = 0xE4E4;
    /** SMuFL U+E4E5 "restQuarter". */
    private static final int REST_QUARTER = 0xE4E5;
    /** SMuFL U+E4E6 "rest8th". */
    private static final int REST_8TH = 0xE4E6;
    /** SMuFL U+E4E7 "rest16th". */
    private static final int REST_16TH = 0xE4E7;
    /** SMuFL U+E4E8 "rest32nd". */
    private static final int REST_32ND = 0xE4E8;
    /** SMuFL U+E4E9 "rest64th". */
    private static final int REST_64TH = 0xE4E9;
    /** SMuFL U+E240 "flag8thUp". */
    private static final int FLAG_8TH_UP = 0xE240;
    /** SMuFL U+E241 "flag8thDown". */
    private static final int FLAG_8TH_DOWN = 0xE241;
    /** SMuFL U+E242 "flag16thUp". */
    private static final int FLAG_16TH_UP = 0xE242;
    /** SMuFL U+E243 "flag16thDown". */
    private static final int FLAG_16TH_DOWN = 0xE243;
    /** SMuFL U+E244 "flag32ndUp". */
    private static final int FLAG_32ND_UP = 0xE244;
    /** SMuFL U+E245 "flag32ndDown". */
    private static final int FLAG_32ND_DOWN = 0xE245;
    /** SMuFL U+E246 "flag64thUp". */
    private static final int FLAG_64TH_UP = 0xE246;
    /** SMuFL U+E247 "flag64thDown". */
    private static final int FLAG_64TH_DOWN = 0xE247;
    /** SMuFL U+E4A0 "articAccentAbove". */
    private static final int ARTIC_ACCENT_ABOVE = 0xE4A0;
    /** SMuFL U+E4A1 "articAccentBelow". */
    private static final int ARTIC_ACCENT_BELOW = 0xE4A1;
    /** SMuFL U+E4A2 "articStaccatoAbove". */
    private static final int ARTIC_STACCATO_ABOVE = 0xE4A2;
    /** SMuFL U+E4A3 "articStaccatoBelow". */
    private static final int ARTIC_STACCATO_BELOW = 0xE4A3;
    /** SMuFL U+E048 "coda". */
    private static final int CODA = 0xE048;
    /** SMuFL U+E0A9 "noteheadXBlack". */
    private static final int NOTEHEAD_X_BLACK = 0xE0A9;
    /** SMuFL U+E0DB "noteheadDiamondBlack". */
    private static final int NOTEHEAD_DIAMOND_BLACK = 0xE0DB;
    /** SMuFL U+ECA5 "metNoteQuarterUp". */
    private static final int MET_NOTE_QUARTER_UP = 0xECA5;
    /** SMuFL U+E612 "stringsUpBow". */
    private static final int STRINGS_UP_BOW = 0xE612;
    /** SMuFL U+E610 "stringsDownBow". */
    private static final int STRINGS_DOWN_BOW = 0xE610;

    private static final Font BASE = BravuraFont.base();
    private static final Map<Float, Font> SIZED = new ConcurrentHashMap<>();

    private MusicFont() {
    }

    static String trebleClef() {
        return glyph(G_CLEF);
    }

    static String bassClef() {
        return glyph(F_CLEF);
    }

    static String timeSignatureDigit(int digit) {
        return glyph(TIME_SIG_DIGIT_ZERO + digit);
    }

    static String noteheadBlack() {
        return glyph(NOTEHEAD_BLACK);
    }

    static String noteheadHalf() {
        return glyph(NOTEHEAD_HALF);
    }

    static String noteheadWhole() {
        return glyph(NOTEHEAD_WHOLE);
    }

    static String accidentalSharp() {
        return glyph(ACCIDENTAL_SHARP);
    }

    static String accidentalFlat() {
        return glyph(ACCIDENTAL_FLAT);
    }

    static String accidentalNatural() {
        return glyph(ACCIDENTAL_NATURAL);
    }

    static String augmentationDot() {
        return glyph(AUGMENTATION_DOT);
    }

    static String restWhole() {
        return glyph(REST_WHOLE);
    }

    static String restHalf() {
        return glyph(REST_HALF);
    }

    static String restQuarter() {
        return glyph(REST_QUARTER);
    }

    static String rest8th() {
        return glyph(REST_8TH);
    }

    static String rest16th() {
        return glyph(REST_16TH);
    }

    static String rest32nd() {
        return glyph(REST_32ND);
    }

    static String rest64th() {
        return glyph(REST_64TH);
    }

    static String flag8thUp() {
        return glyph(FLAG_8TH_UP);
    }

    static String flag8thDown() {
        return glyph(FLAG_8TH_DOWN);
    }

    static String flag16thUp() {
        return glyph(FLAG_16TH_UP);
    }

    static String flag16thDown() {
        return glyph(FLAG_16TH_DOWN);
    }

    static String flag32ndUp() {
        return glyph(FLAG_32ND_UP);
    }

    static String flag32ndDown() {
        return glyph(FLAG_32ND_DOWN);
    }

    static String flag64thUp() {
        return glyph(FLAG_64TH_UP);
    }

    static String flag64thDown() {
        return glyph(FLAG_64TH_DOWN);
    }

    static String articAccentAbove() {
        return glyph(ARTIC_ACCENT_ABOVE);
    }

    static String articAccentBelow() {
        return glyph(ARTIC_ACCENT_BELOW);
    }

    static String articStaccatoAbove() {
        return glyph(ARTIC_STACCATO_ABOVE);
    }

    static String articStaccatoBelow() {
        return glyph(ARTIC_STACCATO_BELOW);
    }

    static String coda() {
        return glyph(CODA);
    }

    static String noteheadXBlack() {
        return glyph(NOTEHEAD_X_BLACK);
    }

    static String noteheadDiamondBlack() {
        return glyph(NOTEHEAD_DIAMOND_BLACK);
    }

    static String metNoteQuarterUp() {
        return glyph(MET_NOTE_QUARTER_UP);
    }

    static String stringsUpBow() {
        return glyph(STRINGS_UP_BOW);
    }

    static String stringsDownBow() {
        return glyph(STRINGS_DOWN_BOW);
    }

    static Font sizedTo(double staffLineSpacing) {
        float emSquare = (float) (staffLineSpacing * 4);
        return SIZED.computeIfAbsent(emSquare, BASE::deriveFont);
    }

    private static String glyph(int codePoint) {
        return Character.toString(codePoint);
    }
}
