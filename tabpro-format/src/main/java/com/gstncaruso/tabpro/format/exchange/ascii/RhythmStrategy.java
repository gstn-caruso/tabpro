package com.gstncaruso.tabpro.format.exchange.ascii;

import com.gstncaruso.tabpro.core.model.Duration;

/**
 * How ASCII import decides how long each note lasts, since the text carries no note values.
 * The manual offers two paths: a fixed rhythm for every note, or deriving it from the spacing
 * between columns (the farther the next note, the longer the previous one), based on how many
 * intervals (columns) fall between two quarter notes -- the manual's "second list".
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
