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
                throw new IllegalStateException(RESOURCE + " was not found on the classpath");
            }
            return Font.createFont(Font.TRUETYPE_FONT, resource);
        } catch (FontFormatException e) {
            throw new IllegalStateException("Bravura.otf does not have a valid font format", e);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + RESOURCE, e);
        }
    }
}
