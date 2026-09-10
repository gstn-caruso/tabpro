package com.gstncaruso.tabpro.ui.dialogs.wizards;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StringOptionsDialogTest {

    @Test
    void everyOptionHasItsOwnTitle() {
        assertEquals("Opciones de let ring", StringOptionsDialog.titleFor(StringOptionsDialog.Option.LET_RING));
        assertEquals("Opciones de palm mute", StringOptionsDialog.titleFor(StringOptionsDialog.Option.PALM_MUTE));
        assertEquals("Opciones de dinámica", StringOptionsDialog.titleFor(StringOptionsDialog.Option.DYNAMIC));
    }
}
