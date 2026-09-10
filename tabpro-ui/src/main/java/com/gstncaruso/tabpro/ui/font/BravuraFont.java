package com.gstncaruso.tabpro.ui.font;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * La fuente musical Bravura (SMuFL), cargada una sola vez y compartida por la partitura y los
 * iconos musicales de las barras de herramientas.
 */
public final class BravuraFont {

    private static final String RESOURCE = "/fonts/Bravura.otf";
    private static final Font BASE = load();

    private BravuraFont() {
    }

    /** La fuente Bravura sin tamano asignado, para que cada usuario derive el tamano que necesite. */
    public static Font base() {
        return BASE;
    }

    private static Font load() {
        try (InputStream resource = BravuraFont.class.getResourceAsStream(RESOURCE)) {
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
