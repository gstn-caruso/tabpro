package com.gstncaruso.tabpro.format.exchange.ascii;

import com.gstncaruso.tabpro.core.model.Duration;

/**
 * Como el import de ASCII decide cuanto dura cada nota, ya que el texto no lleva figuras. El
 * manual ofrece dos caminos: un ritmo fijo para todas las notas, o deducirlo del espaciado entre
 * columnas (cuanto mas lejos la siguiente nota, mas larga la anterior), tomando en cuenta
 * cuantos intervalos (columnas) hay entre dos negras -- la "segunda lista" del manual.
 */
public sealed interface RhythmStrategy {

    static RhythmStrategy fixed(Duration duration) {
        return new Fixed(duration);
    }

    static RhythmStrategy fromSpacing(int intervalsPerQuarterNote) {
        return new FromSpacing(intervalsPerQuarterNote);
    }

    record Fixed(Duration duration) implements RhythmStrategy {
    }

    record FromSpacing(int intervalsPerQuarterNote) implements RhythmStrategy {
        public FromSpacing {
            if (intervalsPerQuarterNote < 1) {
                throw new IllegalArgumentException("intervalsPerQuarterNote debe ser >= 1: " + intervalsPerQuarterNote);
            }
        }
    }
}
