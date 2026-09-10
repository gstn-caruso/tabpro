package com.gstncaruso.tabpro.ui.score;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Los glifos musicales grabados de Bravura (SMuFL) con los que Guitar Pro 5 escribe clave y
 * cifra de compas, expuestos por nombre en vez del codepoint suelto de cada painter.
 */
final class MusicFont {

    private static final String RESOURCE = "/fonts/Bravura.otf";

    /** SMuFL U+E050 "gClef": la clave de sol, con su baseline sobre la linea de Sol. */
    private static final int G_CLEF = 0xE050;
    /** SMuFL U+E062 "fClef": la clave de fa, con su baseline sobre la linea de Fa. */
    private static final int F_CLEF = 0xE062;
    /** SMuFL U+E080.."E089" "timeSig0".."timeSig9": los digitos de una cifra de compas. */
    private static final int TIME_SIG_DIGIT_ZERO = 0xE080;

    private static final Font BASE = load();
    private static final Map<Float, Font> SIZED = new ConcurrentHashMap<>();

    private MusicFont() {
    }

    /** La clave de sol. */
    static String trebleClef() {
        return glyph(G_CLEF);
    }

    /** La clave de fa. */
    static String bassClef() {
        return glyph(F_CLEF);
    }

    /** Un digito (0-9) de una cifra de compas, para armarla glifo por glifo. */
    static String timeSignatureDigit(int digit) {
        return glyph(TIME_SIG_DIGIT_ZERO + digit);
    }

    /**
     * Bravura al tamano de un pentagrama de cuatro espacios -la convencion SMuFL del em
     * cuadrado- para el spacing de lineas dado, cacheada para no derivarla en cada trazo.
     */
    static Font sizedTo(double staffLineSpacing) {
        float emSquare = (float) (staffLineSpacing * 4);
        return SIZED.computeIfAbsent(emSquare, BASE::deriveFont);
    }

    private static String glyph(int codePoint) {
        return Character.toString(codePoint);
    }

    private static Font load() {
        try (InputStream resource = MusicFont.class.getResourceAsStream(RESOURCE)) {
            if (resource == null) {
                throw new IllegalStateException("No se encontro " + RESOURCE + " en el classpath");
            }
            return Font.createFont(Font.TRUETYPE_FONT, resource);
        } catch (FontFormatException e) {
            throw new IllegalStateException("Bravura.otf no tiene un formato de fuente valido", e);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer " + RESOURCE, e);
        }
    }
}
