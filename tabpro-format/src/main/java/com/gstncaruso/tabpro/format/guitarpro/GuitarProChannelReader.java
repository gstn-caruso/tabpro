package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.Channel;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the table of 64 MIDI channels (4 ports of 16 channels) carried by the file
 * header. Guitar Pro stores -1 in a byte when a parameter was left untouched; it is
 * treated here as its default value.
 */
final class GuitarProChannelReader {

    private static final int CHANNEL_COUNT = Channel.PORT_COUNT * Channel.CHANNELS_PER_PORT;
    private static final int UNSET = 0xFF;

    List<GuitarProChannel> read(GuitarProByteReader reader) {
        List<GuitarProChannel> channels = new ArrayList<>(CHANNEL_COUNT);
        for (int i = 0; i < CHANNEL_COUNT; i++) {
            channels.add(readOne(reader));
        }
        return channels;
    }

    private GuitarProChannel readOne(GuitarProByteReader reader) {
        int program = reader.readInt();
        int volume = effectByte(reader.readUnsignedByte(), Channel.DEFAULT_VOLUME);
        int pan = effectByte(reader.readUnsignedByte(), Channel.CENTER_PAN);
        int chorus = effectByte(reader.readUnsignedByte(), 0);
        int reverb = effectByte(reader.readUnsignedByte(), 0);
        int phaser = effectByte(reader.readUnsignedByte(), 0);
        int tremolo = effectByte(reader.readUnsignedByte(), 0);
        reader.skip(2);
        int clampedProgram = program < 0 || program > Channel.MAX ? 0 : program;
        return new GuitarProChannel(clampedProgram, volume, pan, chorus, reverb, phaser, tremolo);
    }

    private int effectByte(int value, int whenUnset) {
        return value == UNSET ? whenUnset : new GuitarProMixerLevel(value).midi();
    }
}
