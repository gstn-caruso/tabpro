package com.gstncaruso.tabpro.ui.icons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

class SvgIconTest {

    private static final String A_MINIMAL_SVG = "/icons/rect-en-color-actual.svg";

    @Test
    void midePedidoElTamanoConElQueSeConstruye() {
        SvgIcon icon = new SvgIcon(A_MINIMAL_SVG, 18);

        assertEquals(18, icon.getIconWidth());
        assertEquals(18, icon.getIconHeight());
    }

    @Test
    void pintaConElForegroundDelComponenteQueLoPinta() {
        SvgIcon icon = new SvgIcon(A_MINIMAL_SVG, 18);
        JPanel probe = new JPanel();
        Color themeColor = new Color(10, 20, 30);
        probe.setForeground(themeColor);

        BufferedImage image = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);
        Graphics2D canvas = image.createGraphics();
        icon.paintIcon(probe, canvas, 0, 0);
        canvas.dispose();

        assertEquals(themeColor.getRGB(), image.getRGB(9, 9));
    }

    @Test
    void reportaClaroSiElRecursoNoExiste() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new SvgIcon("/icons/no-existe.svg", 18));

        assertTrue(error.getMessage().contains("/icons/no-existe.svg"), error.getMessage());
    }
}
