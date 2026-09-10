package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Track;

/**
 * Writes the header of a GP4 track: its tuning, its channels, and its flags. GP4 lacks
 * the fields GP5 adds (how it is drawn, the RSE instrument): those fields simply do not
 * exist in this format, so {@link Track#settings()}'s {@code display} cannot be represented.
 */
final class GuitarProTrackWriter {

    private static final int FLAG_PERCUSSION = 0x01;
    private static final int FLAG_TWELVE_STRING = 0x02;
    private static final int FLAG_BANJO = 0x04;

    private static final int TUNING_SLOTS = 7;
    private static final int NAME_FIELD_SIZE = 40;

    /** The MIDI output port: the tabpro model does not distinguish it from the channel number. */
    private static final int MIDI_OUTPUT_PORT = 1;

    void write(GuitarProByteWriter writer, Track track) {
        int flags = 0;
        if (track.isPercussion()) {
            flags |= FLAG_PERCUSSION;
        }
        if (track.settings().twelveString()) {
            flags |= FLAG_TWELVE_STRING;
        }
        if (track.settings().banjoFifthString()) {
            flags |= FLAG_BANJO;
        }
        writer.writeUnsignedByte(flags);
        writer.writeFixedString(track.name(), NAME_FIELD_SIZE);
        writer.writeInt(track.stringCount());
        writeTuning(writer, track);
        writer.writeInt(MIDI_OUTPUT_PORT);
        int channelIndex = GuitarProChannelWriter.slotFor(track.channel()) + 1;
        writer.writeInt(channelIndex);
        writer.writeInt(channelIndex);
        writer.writeInt(track.settings().fretCount());
        writer.writeInt(track.settings().capo());
        writer.writeColor(track.color());
    }

    private void writeTuning(GuitarProByteWriter writer, Track track) {
        for (int slot = 0; slot < TUNING_SLOTS; slot++) {
            int midiNumber = slot < track.stringCount() ? pitchOf(track, slot).midiNumber() : 0;
            writer.writeInt(midiNumber);
        }
    }

    private Pitch pitchOf(Track track, int slot) {
        return track.tuning().pitchOfString(slot + 1);
    }
}
