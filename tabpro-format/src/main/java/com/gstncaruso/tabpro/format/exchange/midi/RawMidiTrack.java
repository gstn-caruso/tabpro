package com.gstncaruso.tabpro.format.exchange.midi;

import java.util.List;
import java.util.SortedMap;

/**
 * Todo lo que se pudo leer de una pista de un archivo MIDI, ya convertido a los tics propios
 * del modelo (960 por negra): su instrumento, su mezcla, y los golpes que suenan en cada tic,
 * en orden.
 */
record RawMidiTrack(
        int index,
        String name,
        int program,
        int channelNumber,
        int port,
        int volume,
        int pan,
        int reverb,
        int tremolo,
        int chorus,
        int phaser,
        boolean percussion,
        SortedMap<Long, List<RawNote>> notesByTick) {

    int noteCount() {
        return notesByTick.values().stream().mapToInt(List::size).sum();
    }
}
