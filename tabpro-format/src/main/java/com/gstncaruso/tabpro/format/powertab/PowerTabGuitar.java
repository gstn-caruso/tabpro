package com.gstncaruso.tabpro.format.powertab;

import java.util.List;

/**
 * A PowerTab guitar: its description, its tuning (MIDI notes, from the
 * highest-pitched string to the lowest), and its channel's sound parameters.
 */
record PowerTabGuitar(
        String description,
        List<Integer> tuningMidiNotes,
        int preset,
        int initialVolume,
        int pan,
        int reverb,
        int chorus,
        int tremolo,
        int phaser,
        int capo) {
}
