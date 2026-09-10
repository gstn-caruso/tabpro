package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.Font;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

/**
 * Guitar Pro 5 graba clave y cifra de compas con Bravura (SMuFL) en vez de dibujarlas a mano.
 * La fuente viaja en el jar como recurso: un empaquetado roto tiene que fallar aca, no en
 * produccion.
 */
class MusicFontTest {

    @Test
    void theBravuraResourceLoadsFromTheClasspathAsAFont() throws Exception {
        try (InputStream resource = MusicFontTest.class.getResourceAsStream("/fonts/Bravura.otf")) {
            assertNotNull(resource, "Bravura.otf tiene que viajar en el classpath");
            Font bravura = Font.createFont(Font.TRUETYPE_FONT, resource);
            assertEquals("Bravura", bravura.getFamily());
        }
    }
}
