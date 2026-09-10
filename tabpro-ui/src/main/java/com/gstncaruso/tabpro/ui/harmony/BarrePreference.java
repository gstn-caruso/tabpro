package com.gstncaruso.tabpro.ui.harmony;

import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;

public enum BarrePreference {
    ANY,
    FORCE,
    FORBID;

    public boolean accepts(ChordDiagram diagram) {
        return switch (this) {
            case ANY -> true;
            case FORCE -> diagram.requiresBarre();
            case FORBID -> !diagram.requiresBarre();
        };
    }
}
