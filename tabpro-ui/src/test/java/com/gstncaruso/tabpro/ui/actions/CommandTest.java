package com.gstncaruso.tabpro.ui.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CommandTest {

    /**
     * El manual: los botones de paso a paso muestran "Nota anterior/siguiente" parados y
     * cambian a "Compás anterior/siguiente" durante la reproducción. Command extiende
     * AbstractAction, así que renombrar dispara el PropertyChangeEvent que ya actualiza solo
     * cualquier JMenuItem o JButton armado con este comando -no hace falta tocarlos a mano.
     */
    @Test
    void renameToChangesTheLabelAComponentAlreadyBoundToItWouldShow() {
        Command command = Command.named("Nota siguiente", () -> {
        });

        command.renameTo("Compás siguiente");

        assertEquals("Compás siguiente", command.label());
    }
}
