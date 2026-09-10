package com.gstncaruso.tabpro.core.model.effects;

public enum HarmonicType {
    NATURAL("N.H."),
    ARTIFICIAL("A.H."),
    TAPPED("T.H."),
    PINCH("P.H."),
    SEMI("S.H.");

    private final String symbol;

    HarmonicType(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }
}
