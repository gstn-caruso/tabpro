package com.gstncaruso.tabpro.format.exchange.midi;

/**
 * Lo que hace falta para que el usuario elija una pista del archivo MIDI en el import paso a
 * paso: su indice (para pedirla despues con {@code MidiScoreImporter}), su nombre, si es de
 * percusion, su instrumento de General MIDI, el canal que usa y cuantas notas tiene.
 */
public record MidiTrackSummary(int index, String name, boolean percussion, int program, int channelNumber, int noteCount) {

    static MidiTrackSummary of(RawMidiTrack raw) {
        return new MidiTrackSummary(raw.index(), raw.name(), raw.percussion(), raw.program(), raw.channelNumber(), raw.noteCount());
    }
}
