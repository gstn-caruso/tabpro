package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.model.ScoreColor;
import java.awt.Color;
import java.util.Map;

/** Paleta unica de la partitura y del panel de pistas. */
public final class ScoreColors {

    public static final Color BACKGROUND = new Color(0x1E1F22);
    public static final Color SURFACE = new Color(0x2B2D30);
    public static final Color SURFACE_HIGHLIGHT = new Color(0x35373B);
    public static final Color BORDER = new Color(0x3C3F44);

    public static final Color STAFF_LINE = new Color(0x4E5157);
    public static final Color BAR_LINE = new Color(0x7E828A);
    public static final Color INK = new Color(0xE8EAED);
    public static final Color LABEL = new Color(0x9DA1A8);
    public static final Color MUTED_INK = new Color(0x70747B);

    public static final Color ACCENT = new Color(0x3574F0);
    public static final Color CURSOR = ACCENT;
    public static final Color PLAYING = new Color(0x35, 0x74, 0xF0, 0x3C);
    public static final Color PLAYING_MEASURE = new Color(0xE5484D);
    public static final Color WARNING = new Color(0xE5A44A);

    /** El rectangulito rojo que anuncia un cambio de parametro sin simbolo musical propio. */
    public static final Color PARAMETER_CHANGE = new Color(0xD32F3B);
    /** El compas que no suma lo que su medida pide, salvo el que se esta editando. */
    public static final Color INCOMPLETE_MEASURE = new Color(0xE5484D);
    /** El rectangulo que resalta una seleccion multiple. */
    public static final Color SELECTION = new Color(0x35, 0x74, 0xF0, 0x50);
    /** El rectangulo gris que marca, en la otra notacion, la nota que corresponde al cursor. */
    public static final Color CORRESPONDING_NOTE = new Color(0x9D, 0xA1, 0xA8, 0x60);
    /** La voz que no se esta editando, cuando se pide dibujarla atenuada. */
    public static final Color VOICE_INACTIVE = new Color(0x6B6E74);

    /** La hoja clara del Modo Pagina y del Modo Pergamino, sobre el fondo oscuro de la ventana. */
    public static final Color PAGE_PAPER = new Color(0xF6F6F2);
    public static final Color PAGE_INK = new Color(0x202124);
    public static final Color PAGE_MUTED = new Color(0x6B6E74);
    public static final Color PAGE_SHADOW = new Color(0, 0, 0, 90);

    /**
     * Que color es cada color de la partitura cuando se dibuja sobre la hoja clara en vez del
     * fondo oscuro de la pantalla. Los grises se espejan —la tinta clara que se lee sobre el fondo
     * oscuro se lee oscura sobre el papel— y lo que no esta en esta tabla se dibuja tal cual,
     * porque su color es justamente lo que dice: el rojo del cambio de parametro, el del compas
     * incompleto, el azul del cursor o el que el usuario le puso a un marcador.
     */
    private static final Map<Color, Color> ON_PAPER = Map.of(
            BACKGROUND, PAGE_PAPER,
            INK, PAGE_INK,
            LABEL, mirrored(LABEL),
            MUTED_INK, mirrored(MUTED_INK),
            STAFF_LINE, mirrored(STAFF_LINE),
            BAR_LINE, mirrored(BAR_LINE),
            VOICE_INACTIVE, mirrored(VOICE_INACTIVE),
            CORRESPONDING_NOTE, mirrored(CORRESPONDING_NOTE));

    private ScoreColors() {
    }

    public static Color of(ScoreColor color) {
        return new Color(color.red(), color.green(), color.blue());
    }

    /** Como se lee sobre la hoja clara un color elegido para la pantalla oscura. */
    static Color onPaper(Color color) {
        return ON_PAPER.getOrDefault(color, color);
    }

    /** El mismo gris del otro lado: lo que era claro queda oscuro y al reves, sin tocar la transparencia. */
    private static Color mirrored(Color color) {
        return new Color(
                255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue(), color.getAlpha());
    }
}
