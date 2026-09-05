package com.gstncaruso.tabpro.ui.instruments;

import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.Color;

/** Los colores del diapason y del teclado, aparte de los de la partitura. */
public final class InstrumentColors {

    public static final Color NECK = new Color(0x3A2F28);
    public static final Color NECK_EDGE = new Color(0x54463C);
    public static final Color FRET_WIRE = new Color(0x6E6259);
    public static final Color STRING = new Color(0x9A948C);
    public static final Color NUT = new Color(0xCFCAC2);
    public static final Color INLAY = new Color(0x6B6259);
    public static final Color FRET_NUMBER = ScoreColors.MUTED_INK;

    public static final Color WHITE_KEY = new Color(0xE4E6E9);
    public static final Color BLACK_KEY = new Color(0x232427);
    public static final Color KEY_EDGE = new Color(0x151618);

    /** El color con el que se marca una nota que esta sonando, en las dos vistas. */
    public static final Color PRESSED = new Color(0xE5484D);
    public static final Color PRESSED_INK = new Color(0xFFFFFF);

    /** El color de contexto: lo que suma un modo de vista ademas del beat. */
    public static final Color CONTEXT = new Color(0x3574F0);
    public static final Color CONTEXT_INK = new Color(0xFFFFFF);

    /** El anillo que sigue al mouse, sin que haga falta hacer clic. */
    public static final Color HOVER = new Color(0xE8EAED);

    private InstrumentColors() {
    }
}
