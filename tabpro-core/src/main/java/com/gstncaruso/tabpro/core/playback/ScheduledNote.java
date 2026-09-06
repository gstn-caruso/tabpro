package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.effects.Velocity;

/**
 * Una nota lista para sonar: cuando empieza, cuanto dura, con que fuerza y
 * como se mueve su altura mientras suena.
 */
public record ScheduledNote(
        long startTick, long durationTicks, Pitch pitch, Velocity velocity, PitchTrajectory bend, boolean fadeIn) {

    /** La velocidad por defecto de una nota sin dinamica propia (mezzo forte). */
    private static final Velocity DEFAULT_VELOCITY = new Velocity(100);

    public ScheduledNote(long startTick, long durationTicks, Pitch pitch) {
        this(startTick, durationTicks, pitch, DEFAULT_VELOCITY, PitchTrajectory.flat(), false);
    }

    /**
     * Si la nota lleva algun efecto que hay que aplicar sobre el canal entero
     * mientras suena: correrle la altura o abrirle el volumen de a poco. Son
     * las que Guitar Pro manda al segundo canal de la pista, para no arrastrar
     * a las que suenan limpias.
     */
    public boolean carriesAnEffect() {
        return !bend.isFlat() || fadeIn;
    }

    public ScheduledNote withDurationTicks(long durationTicks) {
        return new ScheduledNote(startTick, durationTicks, pitch, velocity, bend, fadeIn);
    }

    ScheduledNote withStartTick(long startTick) {
        return new ScheduledNote(startTick, durationTicks, pitch, velocity, bend, fadeIn);
    }

    public ScheduledNote withBend(PitchTrajectory bend) {
        return new ScheduledNote(startTick, durationTicks, pitch, velocity, bend, fadeIn);
    }

    public long endTick() {
        return startTick + durationTicks;
    }
}
