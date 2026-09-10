package com.gstncaruso.tabpro.core.editing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Como en Guitar Pro 5 y en cualquier editor: mover el cursor sin Shift limpia la seleccion
 * vieja; con Shift (acá, {@link Editor#whileExtendingSelection}) la sigue extendiendo.
 */
class EditorSelectionTest {

    private final Editor editor = new Editor(
            new Score("Prueba", 120, List.of(Track.standardGuitar("Guitarra"), Track.standardBass("Bajo"))));

    @Test
    void movingTheCursorWithoutExtendingClearsAnyActiveSelection() {
        editor.startSelection(false);

        editor.moveRight();

        assertTrue(editor.selection().isEmpty());
    }
}
