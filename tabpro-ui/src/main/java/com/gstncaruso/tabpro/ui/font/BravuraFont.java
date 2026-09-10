package com.gstncaruso.tabpro.ui.font;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public final class BravuraFont {

    private static final String RESOURCE = "/fonts/Bravura.otf";
    private static final Font BASE = load();

    private BravuraFont() {
    }

    public static Font base() {
        return BASE;
    }

    private static Font load() {
        try (InputStream resource = BravuraFont.class.getResourceAsStream(RESOURCE)) {
            if (resource == null) {
                throw new IllegalStateException("No se encontró " + RESOURCE + " en el classpath");
            }
            return Font.createFont(Font.TRUETYPE_FONT, resource);
        } catch (FontFormatException e) {
            throw new IllegalStateException("Bravura.otf no tiene un formato de fuente valido", e);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer " + RESOURCE, e);
        }
    }
}
