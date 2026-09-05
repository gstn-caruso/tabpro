package com.gstncaruso.tabpro.core.model;

import java.util.List;

/**
 * Un grupo irregular: tocar cierta cantidad de notas en el tiempo que ocupa otra
 * cantidad. Un tresillo son 3 notas en el tiempo de 2.
 */
public record Tuplet(int enters, int inTheTimeOf) {

    private static final Tuplet NONE = new Tuplet(1, 1);

    /** Los grupos que ofrece Guitar Pro. */
    public static final List<Integer> AVAILABLE = List.of(1, 3, 5, 6, 7, 9, 10, 11, 12, 13);

    public Tuplet {
        if (enters < 1 || inTheTimeOf < 1) {
            throw new IllegalArgumentException("un grupo irregular necesita cantidades positivas");
        }
    }

    public static Tuplet none() {
        return NONE;
    }

    /** El grupo entra en el tiempo de la potencia de dos anterior: 3 en 2, 5 en 4, 9 en 8. */
    public static Tuplet of(int enters) {
        if (!AVAILABLE.contains(enters)) {
            throw new IllegalArgumentException("grupo irregular no soportado: " + enters);
        }
        return enters == 1 ? NONE : new Tuplet(enters, Integer.highestOneBit(enters - 1));
    }

    public boolean isPlain() {
        return equals(NONE);
    }

    /** Cuanto dura realmente una figura dentro del grupo. */
    public long apply(long ticks) {
        return ticks * inTheTimeOf / enters;
    }
}
