package com.gstncaruso.tabpro.core.model.bars;

import com.gstncaruso.tabpro.core.model.ScoreColor;

/** El cartel que identifica una parte de la partitura: Intro, Estribillo, Solo. */
public record Marker(String name, ScoreColor color) {

    public static final ScoreColor DEFAULT_COLOR = ScoreColor.rgb(0xFF0000);

    public Marker {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("un marcador necesita un nombre");
        }
    }

    public static Marker named(String name) {
        return new Marker(name, DEFAULT_COLOR);
    }
}
