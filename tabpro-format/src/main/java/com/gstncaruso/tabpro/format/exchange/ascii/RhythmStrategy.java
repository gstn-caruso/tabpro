package com.gstncaruso.tabpro.format.exchange.ascii;

import com.gstncaruso.tabpro.core.model.Duration;

/**
 * Como el import de ASCII decide cuanto dura cada nota, ya que el texto no lleva figuras. El
 * manual ofrece dos caminos: un ritmo fijo para todas las notas, o deducirlo del espaciado entre
 * columnas (cuanto mas lejos la siguiente nota, mas larga la anterior).
 */
public sealed interface RhythmStrategy {

    static RhythmStrategy fixed(Duration duration) {
        return new Fixed(duration);
    }

    static RhythmStrategy fromSpacing() {
        return FromSpacing.INSTANCE;
    }

    record Fixed(Duration duration) implements RhythmStrategy {
    }

    record FromSpacing() implements RhythmStrategy {
        private static final FromSpacing INSTANCE = new FromSpacing();
    }
}
