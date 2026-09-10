package com.gstncaruso.tabpro.ui.icons;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SvgIconTest {

    private static final String A_MINIMAL_SVG = "/icons/rect-en-color-actual.svg";

    @Test
    void midePedidoElTamanoConElQueSeConstruye() {
        SvgIcon icon = new SvgIcon(A_MINIMAL_SVG, 18);

        assertEquals(18, icon.getIconWidth());
        assertEquals(18, icon.getIconHeight());
    }
}
