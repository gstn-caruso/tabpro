package com.gstncaruso.tabpro.format.powertab;

import com.gstncaruso.tabpro.core.model.Beat;

/**
 * A position read from PowerTab: which index of the system it is anchored to, and the
 * beat that corresponds to it. If it carries a compressed multi-measure rest,
 * measureCount says how many (0 if it is not that kind of rest); that case is not
 * assembled into measures yet, and is reported by whoever assembles the measure.
 */
record PowerTabPosition(int index, Beat beat, int multibarRestMeasureCount) {

    boolean hasMultibarRest() {
        return multibarRestMeasureCount > 0;
    }
}
