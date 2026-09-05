package com.gstncaruso.tabpro.ui.dialogs.wizards;

import java.util.Optional;

/** Si un asistente tiene que prender, apagar o dejar como esta un efecto. */
public enum ToggleChoice {
    NO_CHANGE("Sin cambios"),
    ON("Activar"),
    OFF("Desactivar");

    private final String label;

    ToggleChoice(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public Optional<Boolean> asChange() {
        return switch (this) {
            case NO_CHANGE -> Optional.empty();
            case ON -> Optional.of(true);
            case OFF -> Optional.of(false);
        };
    }
}
