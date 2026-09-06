package com.gstncaruso.tabpro.format.tabledit;

import java.util.List;

/**
 * El encabezado de una pista: su afinacion (numero MIDI por cuerda, cuerda 1
 * la mas aguda, igual que Guitar Pro), su nombre y su instrumento.
 */
record TabEditTrackHeader(
        String name,
        int stringCount,
        List<Integer> tuningMidiNumbers,
        int midiInstrument,
        int capo,
        int pan,
        int volume,
        boolean percussion) {
}
