package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.Channel;

/**
 * A Guitar Pro mixing-table value -- volume, pan, chorus, reverb, phaser, or tremolo --
 * seen as what it is in the file: one of the sixteen knob steps.
 */
record GuitarProMixerLevel(int step) {

    private static final int MIDI_PER_STEP = 8;
    private static final int STEPS = 16;

    GuitarProMixerLevel {
        step = Math.clamp(step, 0, STEPS);
    }

    static GuitarProMixerLevel ofMidi(int midi) {
        return new GuitarProMixerLevel((midi + 1) / MIDI_PER_STEP);
    }

    int midi() {
        return Math.clamp(step * MIDI_PER_STEP, 0, Channel.MAX);
    }
}
