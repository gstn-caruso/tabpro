package com.gstncaruso.tabpro.format.exchange.midi;

public record MidiTrackSummary(int index, String name, boolean percussion, int program, int channelNumber, int noteCount) {

    static MidiTrackSummary of(RawMidiTrack raw) {
        return new MidiTrackSummary(raw.index(), raw.name(), raw.percussion(), raw.program(), raw.channelNumber(), raw.noteCount());
    }
}
