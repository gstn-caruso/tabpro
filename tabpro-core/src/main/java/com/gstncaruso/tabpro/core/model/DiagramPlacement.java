package com.gstncaruso.tabpro.core.model;

public enum DiagramPlacement {
    ABOVE_THE_STAFF,
    UNDER_THE_TITLE,
    BOTH,
    HIDDEN;

    public static DiagramPlacement of(boolean onTheScore, boolean underTheTitle) {
        if (onTheScore && underTheTitle) {
            return BOTH;
        }
        if (onTheScore) {
            return ABOVE_THE_STAFF;
        }
        if (underTheTitle) {
            return UNDER_THE_TITLE;
        }
        return HIDDEN;
    }

    public boolean showsOnTheScore() {
        return this == ABOVE_THE_STAFF || this == BOTH;
    }

    public boolean showsUnderTheTitle() {
        return this == UNDER_THE_TITLE || this == BOTH;
    }
}
