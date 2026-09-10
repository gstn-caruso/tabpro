package com.gstncaruso.tabpro.core.editing;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void thereIsNoSelectionBeforeAnythingSelectsAnything() {
        assertTrue(editor.selection().isEmpty());
    }

    @Test
    void startingASelectionCoversOnlyTheCurrentBeat() {
        editor.startSelection(false);

        Selection selection = editor.selection().orElseThrow();

        assertEquals(0, selection.fromBeat());
        assertEquals(0, selection.toBeat());
    }

    @Test
    void extendingSelectionSeveralTimesCoversEveryBeatInBetween() {
        editor.whileExtendingSelection(editor::moveRight);
        editor.whileExtendingSelection(editor::moveRight);
        editor.whileExtendingSelection(editor::moveRight);

        Selection selection = editor.selection().orElseThrow();

        assertEquals(0, selection.fromBeat());
        assertEquals(3, selection.toBeat());
    }

    /**
     * En el limite del pentagrama, moveRight() no solo mueve el cursor: tambien inserta un
     * compas nuevo al final de la pista (el mismo camino que usa {@link Editor#change}, no
     * {@link Editor#cursor()} a secas). Tiene que limpiar la seleccion igual que cualquier otro
     * movimiento sin extender.
     */
    @Test
    void movingRightPastTheLastMeasureStillClearsTheSelection() {
        editor.moveRight();
        editor.moveRight();
        editor.moveRight();
        editor.startSelection(false);

        editor.moveRight();

        assertTrue(editor.selection().isEmpty());
    }
}
