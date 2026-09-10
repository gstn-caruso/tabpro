package com.gstncaruso.tabpro.ui.icons;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GlyphIconTest {

    @Test
    void necesitaAlMenosUnRenglonDeGlifos() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new GlyphIcon(18));

        assertTrue(error.getMessage().contains("glifo"), error.getMessage());
    }
}
