package com.gstncaruso.tabpro.ui.tracks;

/** Como se ve un parametro de la mesa de mezcla: como potenciometro o como numero. */
public enum DisplayMode {
    KNOB,
    NUMBER;

    public DisplayMode toggled() {
        return this == KNOB ? NUMBER : KNOB;
    }
}
