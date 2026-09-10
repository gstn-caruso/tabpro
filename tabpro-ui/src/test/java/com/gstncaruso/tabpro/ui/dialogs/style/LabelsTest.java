package com.gstncaruso.tabpro.ui.dialogs.style;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.NoteValue;
import org.junit.jupiter.api.Test;

/**
 * El unico punto que traduce un tipo del dominio a su texto en castellano para
 * mostrarlo en un combo o una lista: ningun combo debe apoyarse en toString().
 */
class LabelsTest {

    @Test
    void traduceLaFiguraDeNota() {
        assertEquals("Negra", Labels.of(NoteValue.QUARTER));
    }
}
