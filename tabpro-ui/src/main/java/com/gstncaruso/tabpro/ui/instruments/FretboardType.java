package com.gstncaruso.tabpro.ui.instruments;

import java.awt.Color;

/**
 * Los tipos de diapason que ofrece el manual: puramente esteticos, pero cada uno se
 * ve distinto en el color de la madera, la forma de los marcadores y el ancho del
 * mastil.
 */
public enum FretboardType {
    ELECTRIC("Electrica", new Color(0x3A2F28), new Color(0x54463C), InlayStyle.DOTS, 1.0),
    ACOUSTIC("Acustica", new Color(0x8A5A34), new Color(0xA9764C), InlayStyle.DOTS, 1.1),
    CLASSICAL("Clasica", new Color(0xC9A66B), new Color(0xDDBF8E), InlayStyle.NONE, 1.25),
    BASIC("Basica", new Color(0x5B5F66), new Color(0x74787F), InlayStyle.DIAMONDS, 0.9);

    private final String label;
    private final Color woodColor;
    private final Color edgeColor;
    private final InlayStyle inlayStyle;
    private final double neckWidthFactor;

    FretboardType(String label, Color woodColor, Color edgeColor, InlayStyle inlayStyle, double neckWidthFactor) {
        this.label = label;
        this.woodColor = woodColor;
        this.edgeColor = edgeColor;
        this.inlayStyle = inlayStyle;
        this.neckWidthFactor = neckWidthFactor;
    }

    public String label() {
        return label;
    }

    public Color woodColor() {
        return woodColor;
    }

    public Color edgeColor() {
        return edgeColor;
    }

    public InlayStyle inlayStyle() {
        return inlayStyle;
    }

    /** Cuanto mas ancho que el electrico se dibuja el mastil de este tipo. */
    public double neckWidthFactor() {
        return neckWidthFactor;
    }

    @Override
    public String toString() {
        return label;
    }
}
