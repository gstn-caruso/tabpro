package com.gstncaruso.tabpro.core.model.effects;

/** Los dedos con que se digita, con la sigla que lleva cada mano en la partitura. */
public enum Finger {
    THUMB("T", "P"),
    INDEX("1", "I"),
    MIDDLE("2", "M"),
    RING("3", "A"),
    LITTLE("4", "C");

    private final String leftHandSymbol;
    private final String rightHandSymbol;

    Finger(String leftHandSymbol, String rightHandSymbol) {
        this.leftHandSymbol = leftHandSymbol;
        this.rightHandSymbol = rightHandSymbol;
    }

    public String leftHandSymbol() {
        return leftHandSymbol;
    }

    public String rightHandSymbol() {
        return rightHandSymbol;
    }
}
