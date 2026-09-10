package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.model.ScoreColor;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/** Paleta unica de la partitura y del panel de pistas. */
public final class ScoreColors {

    public static final Color BACKGROUND = new Color(0x1E1F22);
    public static final Color SURFACE = new Color(0x2B2D30);
    public static final Color SURFACE_HIGHLIGHT = new Color(0x35373B);
    public static final Color BORDER = new Color(0x71, 0x77, 0x80);
    public static final Color KNOB_BODY = new Color(0x7E838B);

    public static final Color STAFF_LINE = new Color(0x72, 0x76, 0x7F);
    public static final Color BAR_LINE = new Color(0x7E828A);
    public static final Color INK = new Color(0xE8EAED);
    public static final Color LABEL = new Color(0x9DA1A8);
    public static final Color MUTED_INK = new Color(0x9E, 0xA0, 0xA6);

    public static final Color ACCENT = new Color(0x3574F0);
    /** El relleno del deslizador de volumen en la mesa de mezcla, como el naranja de Guitar Pro. */
    public static final Color VOLUME_LEVEL = new Color(0xE0, 0x7A, 0x3D);
    /** El deslizador de paneo, pintado de punta a punta: solo se mueve la caja del valor. */
    public static final Color PAN_LEVEL = new Color(0xC7, 0xB5, 0x3A);
    /**
     * La linea vertical fina y roja del cursor de edicion, como en Guitar Pro. Un rojo saturado y
     * a pleno brillo para que no se confunda con los otros dos rojos de la partitura -el apagado
     * del compas incompleto y el del cambio de parametro-, y para que se distinga bien del verde
     * de la linea de reproduccion.
     */
    public static final Color CURSOR = new Color(0xFF, 0x3B, 0x30);
    /**
     * El mismo rojo del cursor, atenuado con transparencia: la parte de la linea que cruza las
     * pistas que no se estan editando, para que se vea continua de punta a punta del sistema sin
     * confundirse con el rojo pleno de la pista activa.
     */
    public static final Color CURSOR_DIMMED = new Color(0xFF, 0x3B, 0x30, 0xE5);
    /** La linea vertical fina que marca por donde va la reproduccion, como en Guitar Pro. */
    public static final Color PLAYING = new Color(0x24, 0xA2, 0x5A);
    public static final Color PLAYING_MEASURE = new Color(0xE5484D);
    public static final Color WARNING = new Color(0xE5A44A);

    /** El rectangulito rojo que anuncia un cambio de parametro sin simbolo musical propio. */
    public static final Color PARAMETER_CHANGE = new Color(0xD32F3B);
    /** El compas que no suma lo que su medida pide, salvo el que se esta editando. */
    public static final Color INCOMPLETE_MEASURE = new Color(0xE5484D);
    /** El rectangulo que resalta una seleccion multiple. */
    public static final Color SELECTION = new Color(0x35, 0x74, 0xF0, 0x50);
    /** El rectangulo gris que marca, en la otra notacion, la nota que corresponde al cursor. */
    public static final Color CORRESPONDING_NOTE = new Color(0x9D, 0xA1, 0xA8, 0xAF);
    /** La voz que no se esta editando, cuando se pide dibujarla atenuada. */
    public static final Color VOICE_INACTIVE = new Color(0x8C, 0x8F, 0x94);

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
     * incompleto, el del cursor de edicion o el que el usuario le puso a un marcador.
     */
    private static final Map<Color, Color> ON_PAPER = buildOnPaperMap();

    private ScoreColors() {
    }

    public static Color of(ScoreColor color) {
        return new Color(color.red(), color.green(), color.blue());
    }

    /**
     * "Ver > Notas con dinamica [F11]" del manual: la cabeza de la nota se lee con un gradiente
     * en vez de la tinta pareja de siempre -de {@link #MUTED_INK} para la mas suave a
     * {@link #INK} para la mas fuerte. Mirroreado en {@link #onPaper}, esa misma escala se lee
     * al reves sobre el papel, que es justo lo que pide el manual: "cuanto mas clara, mas suave;
     * cuanto mas oscura, mas fuerte".
     */
    public static Color forDynamic(Dynamic dynamic) {
        double loudness = dynamic.ordinal() / (double) (Dynamic.values().length - 1);
        return interpolated(MUTED_INK, INK, loudness);
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

    private static Color interpolated(Color from, Color to, double t) {
        return new Color(
                channel(from.getRed(), to.getRed(), t),
                channel(from.getGreen(), to.getGreen(), t),
                channel(from.getBlue(), to.getBlue(), t));
    }

    private static int channel(int from, int to, double t) {
        return (int) Math.round(from + (to - from) * t);
    }

    private static Map<Color, Color> buildOnPaperMap() {
        Map<Color, Color> onPaper = new HashMap<>();
        onPaper.put(BACKGROUND, PAGE_PAPER);
        onPaper.put(INK, PAGE_INK);
        onPaper.put(LABEL, mirrored(LABEL));
        onPaper.put(MUTED_INK, mirrored(MUTED_INK));
        onPaper.put(STAFF_LINE, mirrored(STAFF_LINE));
        onPaper.put(BAR_LINE, mirrored(BAR_LINE));
        onPaper.put(VOICE_INACTIVE, mirrored(VOICE_INACTIVE));
        onPaper.put(CORRESPONDING_NOTE, mirrored(CORRESPONDING_NOTE));
        for (Dynamic dynamic : Dynamic.values()) {
            Color forDynamic = forDynamic(dynamic);
            onPaper.putIfAbsent(forDynamic, mirrored(forDynamic));
        }
        return Map.copyOf(onPaper);
    }
}
