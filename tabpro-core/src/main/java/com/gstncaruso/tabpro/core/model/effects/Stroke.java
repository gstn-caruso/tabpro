package com.gstncaruso.tabpro.core.model.effects;

import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.NoteValue;

/** El rasgueo: las cuerdas suenan una despues de la otra y no todas juntas. */
public record Stroke(StrokeDirection direction, NoteValue speed, boolean rasgueado) {

    public static Stroke of(StrokeDirection direction) {
        return new Stroke(direction, NoteValue.THIRTY_SECOND, false);
    }

    /** Cuanto se demora cada cuerda respecto de la anterior. */
    public long delayTicks() {
        return new Duration(speed, false).ticks();
    }
}
