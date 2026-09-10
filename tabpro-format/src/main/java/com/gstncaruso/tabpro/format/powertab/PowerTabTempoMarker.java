package com.gstncaruso.tabpro.format.powertab;

/**
 * A PowerTab tempo marker. tabpro only stores a single tempo for the whole score (not a
 * change partway through), so of every marker in the file only the first one that is a
 * standard marker (quarter note = so many) is used; the listesso or
 * "accelerando/ritardando" kinds, and the description, have no place in the model.
 */
record PowerTabTempoMarker(int system, int position, int type, int beatsPerMinute) {

    static final int STANDARD_MARKER = 1;

    boolean isStandardMarker() {
        return type == STANDARD_MARKER;
    }
}
