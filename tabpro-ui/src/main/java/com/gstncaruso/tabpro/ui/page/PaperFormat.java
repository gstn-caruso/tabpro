package com.gstncaruso.tabpro.ui.page;

public enum PaperFormat {
    A4(210, 297),
    A3(297, 420),
    LETTER(8.5 * 25.4, 11 * 25.4),
    LEGAL(8.5 * 25.4, 14 * 25.4);

    private final double widthMillimetres;
    private final double heightMillimetres;

    PaperFormat(double widthMillimetres, double heightMillimetres) {
        this.widthMillimetres = widthMillimetres;
        this.heightMillimetres = heightMillimetres;
    }

    public double widthMillimetres() {
        return widthMillimetres;
    }

    public double heightMillimetres() {
        return heightMillimetres;
    }
}
