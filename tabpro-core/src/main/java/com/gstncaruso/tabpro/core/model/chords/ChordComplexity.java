package com.gstncaruso.tabpro.core.model.chords;

/**
 * Que tan dificil es tocar un diagrama, para poder filtrar la lista que ofrece el buscador
 * de posiciones: Simple, Media o Todas (que en este enum es "hasta Complex inclusive").
 */
public enum ChordComplexity {
    SIMPLE,
    MEDIUM,
    COMPLEX;

    /** Si un diagrama de esta dificultad entra dentro del filtro que pidio el usuario. */
    public boolean accepts(ChordComplexity actual) {
        return actual.ordinal() <= this.ordinal();
    }
}
