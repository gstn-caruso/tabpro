package com.gstncaruso.tabpro.core.model.effects;

/**
 * Un punto de la curva de un bend o de una palanca: cuanto se estiro la cuerda
 * en un momento dado de la nota.
 */
public record BendPoint(int position, int quarterTones, int vibrato) {

    /** La curva se dibuja sobre una grilla que va de 0 a 60, como en Guitar Pro. */
    public static final int LAST_POSITION = 60;

    /** Tres tonos, el maximo que ofrece la ventana de bend. */
    public static final int MAX_QUARTER_TONES = 12;

    public static final int MAX_VIBRATO = 3;

    public BendPoint {
        if (position < 0 || position > LAST_POSITION) {
            throw new IllegalArgumentException("position debe estar entre 0 y " + LAST_POSITION + ": " + position);
        }
        if (quarterTones < -MAX_QUARTER_TONES || quarterTones > MAX_QUARTER_TONES) {
            throw new IllegalArgumentException("quarterTones fuera de rango: " + quarterTones);
        }
        if (vibrato < 0 || vibrato > MAX_VIBRATO) {
            throw new IllegalArgumentException("vibrato debe estar entre 0 y " + MAX_VIBRATO + ": " + vibrato);
        }
    }

    public static BendPoint at(int position, int quarterTones) {
        return new BendPoint(position, quarterTones, 0);
    }

    public double semitones() {
        return quarterTones / 2.0;
    }

    /** Que fraccion de la nota transcurrio cuando se llega a este punto. */
    public double fractionOfTheNote() {
        return position / (double) LAST_POSITION;
    }
}
