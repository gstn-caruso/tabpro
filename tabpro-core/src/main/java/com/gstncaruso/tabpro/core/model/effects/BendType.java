package com.gstncaruso.tabpro.core.model.effects;

/** La forma basica de una curva de bend, que elige el simbolo en la tablatura. */
public enum BendType {
    BEND("Bend"),
    BEND_RELEASE("Bend y suelta"),
    BEND_RELEASE_BEND("Bend, suelta y bend"),
    PREBEND("Prebend"),
    PREBEND_RELEASE("Prebend y suelta");

    private final String label;

    BendType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
