package com.gstncaruso.tabpro.format.tabledit;

import java.util.List;

/**
 * A track's header: its tuning (MIDI number per string, string 1 the highest-pitched,
 * same as Guitar Pro), its name, and its instrument.
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
