package com.gstncaruso.tabpro.ui.instruments;

import java.awt.Color;

record FretboardPalette(
        Color string,
        Color fretWire,
        Color inlay,
        Color mark,
        Color markInk,
        Color context,
        Color contextInk,
        Color hover) {

    static FretboardPalette electric() {
        return new FretboardPalette(
                InstrumentColors.STRING,
                InstrumentColors.FRET_WIRE,
                InstrumentColors.INLAY,
                InstrumentColors.PRESSED,
                InstrumentColors.PRESSED_INK,
                InstrumentColors.CONTEXT,
                InstrumentColors.CONTEXT_INK,
                InstrumentColors.HOVER);
    }
}
