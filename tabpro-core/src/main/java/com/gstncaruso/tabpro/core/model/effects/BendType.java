package com.gstncaruso.tabpro.core.model.effects;

import java.util.List;

public enum BendType {
    BEND("Bend"),
    BEND_RELEASE("Bend y suelta"),
    BEND_RELEASE_BEND("Bend, suelta y bend"),
    PREBEND("Prebend"),
    PREBEND_RELEASE("Prebend y suelta"),
    DIP("Dip"),
    DIVE("Dive"),
    RELEASE_UP("Release up"),
    INVERTED_DIP("Inverted dip"),
    RETURN("Return"),
    RELEASE_DOWN("Release down");

    private final String label;

    BendType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static List<BendType> bendTypes() {
        return List.of(BEND, BEND_RELEASE, BEND_RELEASE_BEND, PREBEND, PREBEND_RELEASE);
    }

    public static List<BendType> tremoloBarTypes() {
        return List.of(DIP, DIVE, RELEASE_UP, INVERTED_DIP, RETURN, RELEASE_DOWN);
    }
}
