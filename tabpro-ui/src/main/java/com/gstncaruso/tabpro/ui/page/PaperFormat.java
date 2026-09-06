package com.gstncaruso.tabpro.ui.page;

/** Los tamanos de papel mas comunes para imprimir la partitura. */
public enum PaperFormat {
    A4("A4"),
    A3("A3"),
    LETTER("Carta"),
    LEGAL("Oficio");

    private final String label;

    PaperFormat(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
