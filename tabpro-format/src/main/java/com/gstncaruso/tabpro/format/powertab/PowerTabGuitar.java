package com.gstncaruso.tabpro.format.powertab;

import java.util.List;

/**
 * Una guitarra de PowerTab: su descripcion, su afinacion (notas MIDI, de la
 * cuerda mas aguda a la mas grave) y los parametros de sonido de su canal.
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
