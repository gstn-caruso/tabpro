package com.gstncaruso.tabpro.core.model.effects;

import java.util.List;

public enum BendType {
    BEND,
    BEND_RELEASE,
    BEND_RELEASE_BEND,
    PREBEND,
    PREBEND_RELEASE,
    DIP,
    DIVE,
    RELEASE_UP,
    INVERTED_DIP,
    RETURN,
    RELEASE_DOWN;

    public static List<BendType> bendTypes() {
        return List.of(BEND, BEND_RELEASE, BEND_RELEASE_BEND, PREBEND, PREBEND_RELEASE);
    }

    public static List<BendType> tremoloBarTypes() {
        return List.of(DIP, DIVE, RELEASE_UP, INVERTED_DIP, RETURN, RELEASE_DOWN);
    }
}
