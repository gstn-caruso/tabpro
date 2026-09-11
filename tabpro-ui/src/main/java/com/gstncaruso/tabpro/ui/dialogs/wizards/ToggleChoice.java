package com.gstncaruso.tabpro.ui.dialogs.wizards;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Optional;

public enum ToggleChoice {
    NO_CHANGE(Texts.get("score_dialogs.shared.noChange")),
    ON(Texts.get("score_dialogs.ToggleChoice.on")),
    OFF(Texts.get("score_dialogs.ToggleChoice.off"));

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
