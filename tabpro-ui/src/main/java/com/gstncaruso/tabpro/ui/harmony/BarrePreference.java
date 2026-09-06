package com.gstncaruso.tabpro.ui.harmony;

import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;

/**
 * Las casillas "forzar cejilla" / "prohibir cejilla" de la zona B: un filtro mas sobre la
 * lista de diagramas posibles, ademas del de complejidad.
 */
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
