package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.Channel;
import java.util.List;

/**
 * Writes the table of 64 MIDI channels (4 ports of 16 channels) carried by the file
 * header. The reader always assumes port 1, so the writer only uses the channel number
 * (1 to 16) as an index, and any other port value is lost on export.
 */
final class GuitarProChannelWriter {

    private static final int CHANNEL_COUNT = Channel.PORT_COUNT * Channel.CHANNELS_PER_PORT;

    void write(GuitarProByteWriter writer, List<Channel> channelsByIndex) {
        for (int slot = 0; slot < CHANNEL_COUNT; slot++) {
            writeOne(writer, channelsByIndex.get(slot));
        }
    }

    static List<Channel> tableFor(com.gstncaruso.tabpro.core.model.Score score) {
        Channel[] table = new Channel[CHANNEL_COUNT];
        for (int i = 0; i < CHANNEL_COUNT; i++) {
            table[i] = Channel.playing(0);
        }
        for (int index = 0; index < score.trackCount(); index++) {
            Channel channel = score.track(index).channel();
            table[slotFor(channel)] = channel;
        }
        return List.of(table);
    }

    private static void writeKnob(GuitarProByteWriter writer, int midi) {
        writer.writeUnsignedByte(GuitarProMixerLevel.ofMidi(midi).step());
    }

    /** Which slot of the 64-channel array this track falls into: the reader only looks at the number, not the port. */
    static int slotFor(Channel channel) {
        return Math.clamp(channel.number(), 1, Channel.CHANNELS_PER_PORT) - 1;
    }

    private void writeOne(GuitarProByteWriter writer, Channel channel) {
        writer.writeInt(channel.program());
        writeKnob(writer, channel.volume());
        writeKnob(writer, channel.pan());
        writeKnob(writer, channel.chorus());
        writeKnob(writer, channel.reverb());
        writeKnob(writer, channel.phaser());
        writeKnob(writer, channel.tremolo());
        writer.writeShort(0);
    }
}
