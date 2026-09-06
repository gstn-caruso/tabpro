package com.gstncaruso.tabpro.ui.page;

/** Los tamanos de papel mas comunes para imprimir la partitura, medidos en vertical. */
public enum PaperFormat {
    A4("A4", 210, 297),
    A3("A3", 297, 420),
    LETTER("Carta", 8.5 * 25.4, 11 * 25.4),
    LEGAL("Oficio", 8.5 * 25.4, 14 * 25.4);

    private final String label;
    private final double widthMillimetres;
    private final double heightMillimetres;

    PaperFormat(String label, double widthMillimetres, double heightMillimetres) {
        this.label = label;
        this.widthMillimetres = widthMillimetres;
        this.heightMillimetres = heightMillimetres;
    }

    public String label() {
        return label;
    }

    public double widthMillimetres() {
        return widthMillimetres;
    }

    public double heightMillimetres() {
        return heightMillimetres;
    }
}
