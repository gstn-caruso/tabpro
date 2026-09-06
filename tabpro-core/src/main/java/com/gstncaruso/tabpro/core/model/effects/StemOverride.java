package com.gstncaruso.tabpro.core.model.effects;

/**
 * Si la plica de este beat esta forzada a una direccion, o si sigue la regla automatica que
 * decide {@link com.gstncaruso.tabpro.core.notation.StemDirection}. El manual (linea 923) dice
 * que ademas de las barras "es posible cambiar a mano ... la direccion de la plica" desde el
 * menu Nota -misma forma que {@code LineBreak}: automatico por default, o forzado a un valor.
 */
public enum StemOverride {
    AUTOMATIC("Automático"),
    UP("Arriba"),
    DOWN("Abajo");

    private final String label;

    StemOverride(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    /** Si esta forzada, la direccion que pide; si es automatica, la que calculo el resto. */
    public boolean pointsUp(boolean automaticPointsUp) {
        return switch (this) {
            case UP -> true;
            case DOWN -> false;
            case AUTOMATIC -> automaticPointsUp;
        };
    }
}
