package com.gstncaruso.tabpro.format.tabledit;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads each track's header: a fixed-size record from which the string count, the
 * instrument, the capo, the pan and volume, the tuning, and the name are needed. The
 * clamp (clef, grand staff brace, double string, pedal steel, rhythm track) has nowhere
 * to live in the tabpro model and is discarded along with the rest of the padding.
 */
final class TabEditTrackReader {

    /** The MIDI instrument TablEdit uses to mark "this is a percussion track". */
    private static final int PERCUSSION_MIDI_INSTRUMENT = 96;

    private static final int TUNING_SLOTS = 12;

    /** A reference MIDI number: the raw tuning byte is how much lower that string sounds. */
    private static final int TUNING_REFERENCE_MIDI_NUMBER = 96;

    List<TabEditTrackHeader> read(TabEditByteReader input) {
        int maxTrackSize = input.readUnsignedShort();
        int trackCount = input.readUnsignedShort();

        List<TabEditTrackHeader> tracks = new ArrayList<>(trackCount);
        for (int i = 0; i < trackCount; i++) {
            tracks.add(readOne(new TabEditByteReader(input.readBlock(maxTrackSize)), maxTrackSize));
        }
        return tracks;
    }

    private TabEditTrackHeader readOne(TabEditByteReader record, int maxTrackSize) {
        int stringCount = record.readUnsignedByte();
        record.skip(7);
        int midiInstrument = record.readUnsignedByte();
        record.skip(2);
        record.skip(1); // transposition: has nowhere to live in the tabpro model.
        int capo = record.readUnsignedByte();
        record.skip(1);
        record.skip(1); // middle-C offset: only affects staff drawing.
        record.skip(1); // clef, grand staff, brace: same.
        record.skip(1);
        int pan = record.readUnsignedByte();
        int volume = record.readUnsignedByte();
        record.skip(1); // double string, track let ring, pedal steel, rhythm track: same.

        List<Integer> tuning = new ArrayList<>(stringCount);
        for (int string = 0; string < stringCount; string++) {
            tuning.add(TUNING_REFERENCE_MIDI_NUMBER - record.readUnsignedByte());
        }
        record.skip(TUNING_SLOTS - stringCount);

        String name = record.readNullTerminatedString(maxTrackSize);

        return new TabEditTrackHeader(
                name.isBlank() ? "Pista" : name, stringCount, List.copyOf(tuning), midiInstrument, capo, pan, volume,
                midiInstrument == PERCUSSION_MIDI_INSTRUMENT);
    }
}
