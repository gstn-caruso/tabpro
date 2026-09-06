package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Track;

/**
 * Escribe el encabezado de una pista de GP4: su afinacion, sus canales y sus banderas.
 * GP4 no tiene los campos que agrega GP5 (como se dibuja, el instrumento de RSE): esos
 * campos simplemente no existen en este formato, y por eso {@link Track#settings()}'s
 * {@code display} no se puede representar.
 */
final class GuitarProTrackWriter {

    private static final int FLAG_PERCUSSION = 0x01;
    private static final int FLAG_TWELVE_STRING = 0x02;
    private static final int FLAG_BANJO = 0x04;

    private static final int TUNING_SLOTS = 7;
    private static final int NAME_FIELD_SIZE = 40;

    /** El puerto de salida MIDI: el modelo de tabpro no lo distingue del numero de canal. */
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
