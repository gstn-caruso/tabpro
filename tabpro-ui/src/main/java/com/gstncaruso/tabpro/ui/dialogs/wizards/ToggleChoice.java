package com.gstncaruso.tabpro.ui.dialogs.wizards;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Optional;

public enum ToggleChoice {
    NO_CHANGE("score_dialogs.shared.noChange"),
    ON("score_dialogs.ToggleChoice.on"),
    OFF("score_dialogs.ToggleChoice.off");

    private final String labelKey;

    ToggleChoice(String labelKey) {
        this.labelKey = labelKey;
    }

    public String label() {
        return Texts.get(labelKey);
    }

    public Optional<Boolean> asChange() {
        return switch (this) {
            case NO_CHANGE -> Optional.empty();
            case ON -> Optional.of(true);
            case OFF -> Optional.of(false);
        };
    }
}
