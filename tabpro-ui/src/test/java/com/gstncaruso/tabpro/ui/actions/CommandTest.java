package com.gstncaruso.tabpro.ui.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CommandTest {

    @Test
    void renameToChangesTheLabelAComponentAlreadyBoundToItWouldShow() {
        Command command = Command.named("Nota siguiente", () -> {
        });

        command.renameTo("Compás siguiente");

        assertEquals("Compás siguiente", command.label());
    }
}
