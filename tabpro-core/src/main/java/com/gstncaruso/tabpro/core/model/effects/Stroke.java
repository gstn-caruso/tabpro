package com.gstncaruso.tabpro.core.model.effects;

import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.NoteValue;

public record Stroke(StrokeDirection direction, NoteValue speed, boolean rasgueado) {

    public static Stroke of(StrokeDirection direction) {
        return new Stroke(direction, NoteValue.THIRTY_SECOND, false);
    }

    public long delayTicks() {
        return new Duration(speed, false).ticks();
    }
}
