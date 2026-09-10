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
    /** SMuFL U+E0A4 "noteheadBlack": la cabeza rellena de negra, corchea y figuras mas cortas. */
    private static final int NOTEHEAD_BLACK = 0xE0A4;
    /** SMuFL U+E0A3 "noteheadHalf": la cabeza hueca de una blanca. */
    private static final int NOTEHEAD_HALF = 0xE0A3;
    /** SMuFL U+E0A2 "noteheadWhole": la cabeza hueca de una redonda. */
    private static final int NOTEHEAD_WHOLE = 0xE0A2;
    /** SMuFL U+E262 "accidentalSharp": el sostenido. */
    private static final int ACCIDENTAL_SHARP = 0xE262;
    /** SMuFL U+E260 "accidentalFlat": el bemol. */
    private static final int ACCIDENTAL_FLAT = 0xE260;
    /** SMuFL U+E261 "accidentalNatural": el becuadro. */
    private static final int ACCIDENTAL_NATURAL = 0xE261;
    /** SMuFL U+E1E7 "augmentationDot": el puntillo que alarga una figura o un silencio. */
    private static final int AUGMENTATION_DOT = 0xE1E7;
    /** SMuFL U+E4E3 "restWhole": el silencio de redonda, colgado de la cuarta linea. */
    private static final int REST_WHOLE = 0xE4E3;
    /** SMuFL U+E4E4 "restHalf": el silencio de blanca, apoyado en la linea del medio. */
    private static final int REST_HALF = 0xE4E4;
    /** SMuFL U+E4E5 "restQuarter": el silencio de negra, centrado en el pentagrama. */
    private static final int REST_QUARTER = 0xE4E5;
    /** SMuFL U+E4E6 "rest8th": el silencio de corchea, centrado en el pentagrama. */
    private static final int REST_8TH = 0xE4E6;
    /** SMuFL U+E4E7 "rest16th": el silencio de semicorchea, centrado en el pentagrama. */
    private static final int REST_16TH = 0xE4E7;

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

    /** La cabeza rellena de negra, corchea y figuras mas cortas. */
    static String noteheadBlack() {
        return glyph(NOTEHEAD_BLACK);
    }

    /** La cabeza hueca de una blanca. */
    static String noteheadHalf() {
        return glyph(NOTEHEAD_HALF);
    }

    /** La cabeza hueca de una redonda. */
    static String noteheadWhole() {
        return glyph(NOTEHEAD_WHOLE);
    }

    /** El sostenido. */
    static String accidentalSharp() {
        return glyph(ACCIDENTAL_SHARP);
    }

    /** El bemol. */
    static String accidentalFlat() {
        return glyph(ACCIDENTAL_FLAT);
    }

    /** El becuadro. */
    static String accidentalNatural() {
        return glyph(ACCIDENTAL_NATURAL);
    }

    /** El puntillo que alarga una figura o un silencio. */
    static String augmentationDot() {
        return glyph(AUGMENTATION_DOT);
    }

    /** El silencio de redonda, colgado de la cuarta linea. */
    static String restWhole() {
        return glyph(REST_WHOLE);
    }

    /** El silencio de blanca, apoyado en la linea del medio. */
    static String restHalf() {
        return glyph(REST_HALF);
    }

    /** El silencio de negra, centrado en el pentagrama. */
    static String restQuarter() {
        return glyph(REST_QUARTER);
    }

    /** El silencio de corchea, centrado en el pentagrama. */
    static String rest8th() {
        return glyph(REST_8TH);
    }

    /** El silencio de semicorchea, centrado en el pentagrama. */
    static String rest16th() {
        return glyph(REST_16TH);
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
