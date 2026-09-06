package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.Channel;

/**
 * Un valor de la mesa de mezcla de Guitar Pro -- volumen, paneo, chorus, reverb, phaser o
 * tremolo -- visto como lo que es en el archivo: uno de los dieciseis pasos de la perilla.
 * El modelo de tabpro los maneja en los 0 a 127 de MIDI, asi que hay que traducir en las
 * dos direcciones.
 */
record GuitarProMixerLevel(int step) {

    private static final int MIDI_PER_STEP = 8;
    private static final int STEPS = 16;

    GuitarProMixerLevel {
        step = Math.clamp(step, 0, STEPS);
    }

    /** El paso al que hay que llevar la perilla para que suene asi de fuerte. */
    static GuitarProMixerLevel ofMidi(int midi) {
        return new GuitarProMixerLevel((midi + 1) / MIDI_PER_STEP);
    }

    int midi() {
        return Math.clamp(step * MIDI_PER_STEP, 0, Channel.MAX);
    }
}
