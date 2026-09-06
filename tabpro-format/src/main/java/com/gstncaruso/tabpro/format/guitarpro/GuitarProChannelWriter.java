package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.Channel;
import java.util.List;

/**
 * Escribe la tabla de 64 canales MIDI (4 puertos por 16 canales) que trae la cabecera del
 * archivo. El lector siempre asume el puerto 1: por eso el escritor solo usa el numero de
 * canal (1 a 16) como indice, y el puerto de cualquier otro valor se pierde en la
 * exportacion.
 */
final class GuitarProChannelWriter {

    private static final int CHANNEL_COUNT = Channel.PORT_COUNT * Channel.CHANNELS_PER_PORT;

    void write(GuitarProByteWriter writer, List<Channel> channelsByIndex) {
        for (int slot = 0; slot < CHANNEL_COUNT; slot++) {
            writeOne(writer, channelsByIndex.get(slot));
        }
    }

    /** Un arreglo de 64 canales por defecto, con el sonido de cada pista puesto en su lugar. */
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

    /** En que casillero del arreglo de 64 canales cae esta pista: el lector solo mira el numero, no el puerto. */
    static int slotFor(Channel channel) {
        return Math.clamp(channel.number(), 1, Channel.CHANNELS_PER_PORT) - 1;
    }

    private void writeOne(GuitarProByteWriter writer, Channel channel) {
        writer.writeInt(channel.program());
        writer.writeUnsignedByte(channel.volume());
        writer.writeUnsignedByte(channel.pan());
        writer.writeUnsignedByte(channel.chorus());
        writer.writeUnsignedByte(channel.reverb());
        writer.writeUnsignedByte(channel.phaser());
        writer.writeUnsignedByte(channel.tremolo());
        writer.writeShort(0);
    }
}
