package com.gstncaruso.tabpro.ui.instruments;

import java.awt.Color;

/**
 * Los tipos de diapason que ofrece el manual: puramente esteticos, pero cada uno se
 * ve distinto en el color de la madera, la forma de los marcadores y el ancho del
 * mastil.
 */
public enum FretboardType {
    ELECTRIC("Electrica", new Color(0x3A2F28), new Color(0x8D7665), InlayStyle.DOTS, 1.0,
            FretboardPalette.electric()),
    ACOUSTIC("Acustica", new Color(0x8A5A34), new Color(0xD4B79F), InlayStyle.DOTS, 1.1,
            new FretboardPalette(
                    new Color(0xBEBBB5),
                    new Color(0xC2BAB4),
                    new Color(0xC1BBB4),
                    new Color(0xF3A8AA),
                    InstrumentColors.PRESSED_INK,
                    new Color(0x9DBCF8),
                    InstrumentColors.CONTEXT_INK,
                    new Color(0xB6BCC5),
                    InstrumentColors.NUT)),
    CLASSICAL("Clasica", new Color(0xC9A66B), new Color(0x715322), InlayStyle.NONE, 1.25,
            new FretboardPalette(
                    new Color(0x5D5852),
                    new Color(0x62574F),
                    new Color(0x5F574F),
                    new Color(0xAF191E),
                    Color.WHITE,
                    new Color(0x0F4ECB),
                    Color.WHITE,
                    new Color(0x515967),
                    InstrumentColors.NUT)),
    BASIC("Basica", new Color(0x5B5F66), new Color(0xB1B3B7), InlayStyle.DIAMONDS, 0.9,
            new FretboardPalette(
                    new Color(0xB7B2AD),
                    new Color(0xBAB2AB),
                    new Color(0xB9B2AA),
                    new Color(0xF19C9F),
                    InstrumentColors.PRESSED_INK,
                    new Color(0x91B3F7),
                    InstrumentColors.CONTEXT_INK,
                    new Color(0xADB4BF),
                    InstrumentColors.NUT));

    private final String label;
    private final Color woodColor;
    private final Color edgeColor;
    private final InlayStyle inlayStyle;
    private final double neckWidthFactor;
    private final FretboardPalette palette;

    FretboardType(String label, Color woodColor, Color edgeColor, InlayStyle inlayStyle, double neckWidthFactor,
            FretboardPalette palette) {
        this.label = label;
        this.woodColor = woodColor;
        this.edgeColor = edgeColor;
        this.inlayStyle = inlayStyle;
        this.neckWidthFactor = neckWidthFactor;
        this.palette = palette;
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

    public Color stringColor() {
        return palette.string();
    }

    public Color fretWireColor() {
        return palette.fretWire();
    }

    public Color inlayColor() {
        return palette.inlay();
    }

    public Color markColor() {
        return palette.mark();
    }

    public Color markInkColor() {
        return palette.markInk();
    }

    public Color contextColor() {
        return palette.context();
    }

    public Color contextInkColor() {
        return palette.contextInk();
    }

    public Color hoverColor() {
        return palette.hover();
    }

    public Color nutColor() {
        return palette.nut();
    }

    @Override
    public String toString() {
        return label;
    }
}
