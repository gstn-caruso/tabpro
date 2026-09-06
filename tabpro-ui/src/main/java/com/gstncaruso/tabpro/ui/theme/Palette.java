package com.gstncaruso.tabpro.ui.theme;

import java.awt.Color;
import javax.swing.UIManager;

/**
 * Los colores de la ventana. Salen del look and feel para que la interfaz
 * acompane al tema, con un valor de respaldo por si la clave no esta.
 */
public final class Palette {

    private Palette() {
    }

    public static Color background() {
        return color("tabpro.background", new Color(0x1E1F22));
    }

    public static Color panel() {
        return color("tabpro.panel", new Color(0x2B2D30));
    }

    public static Color raisedPanel() {
        return color("tabpro.raisedPanel", new Color(0x35373B));
    }

    public static Color separator() {
        return color("tabpro.separator", new Color(0x3C3F41));
    }

    public static Color text() {
        return color("tabpro.text", new Color(0xD7D9DD));
    }

    public static Color mutedText() {
        return color("tabpro.mutedText", new Color(0x8B8F96));
    }

    public static Color accent() {
        return color("tabpro.accent", new Color(0xE8A33D));
    }

    /** El rojo con que se marca lo que no cierra: un compas incompleto, el playhead. */
    public static Color warning() {
        return color("tabpro.warning", new Color(0xE05C5C));
    }

    /** La hoja de la partitura en modo pagina. */
    public static Color paper() {
        return color("tabpro.paper", new Color(0xF6F3EC));
    }

    public static Color ink() {
        return color("tabpro.ink", new Color(0x1A1A1A));
    }

    private static Color color(String key, Color fallback) {
        Color found = UIManager.getColor(key);
        return found == null ? fallback : found;
    }
}
