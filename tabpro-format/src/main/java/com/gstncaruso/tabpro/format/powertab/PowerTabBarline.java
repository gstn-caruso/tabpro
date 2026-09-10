package com.gstncaruso.tabpro.format.powertab;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;

/**
 * A PowerTab barline: which position of the system it is anchored to, its type (plain
 * measure, repeat, double bar...), and the key signature and time signature that govern
 * from there on. Every barline carries its own complete value: unlike Guitar Pro,
 * PowerTab does not mark a "change" with a flag.
 */
record PowerTabBarline(int position, int type, int repeatCount, TimeSignature timeSignature, KeySignature keySignature) {

    static final int BAR = 0;
    static final int DOUBLE_BAR = 1;
    static final int FREE_TIME_BAR = 2;
    static final int REPEAT_START = 3;
    static final int REPEAT_END = 4;
    static final int DOUBLE_BAR_FINE = 5;

    boolean isDoubleBar() {
        return type == DOUBLE_BAR || type == DOUBLE_BAR_FINE;
    }

    boolean isRepeatStart() {
        return type == REPEAT_START;
    }

    boolean isRepeatEnd() {
        return type == REPEAT_END;
    }
}
