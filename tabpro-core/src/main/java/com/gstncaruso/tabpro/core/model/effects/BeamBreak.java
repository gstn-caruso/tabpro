package com.gstncaruso.tabpro.core.model.effects;

/**
 * Si el agrupamiento por barra de union corta justo antes de este beat. El manual (linea 923)
 * dice que Guitar Pro agrupa las barras automaticamente segun la duracion y el compas, "pero es
 * posible cambiar a mano las barras... usando el menu Nota" -misma forma que {@code LineBreak}
 * en {@code MeasureAttributes}: automatico por default, o forzado a lo que pida el usuario.
 */
public enum BeamBreak {
    AUTOMATIC("Automático"),
    FORCED("Forzar corte"),
    PREVENTED("Impedir corte");

    private final String label;

    BeamBreak(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
