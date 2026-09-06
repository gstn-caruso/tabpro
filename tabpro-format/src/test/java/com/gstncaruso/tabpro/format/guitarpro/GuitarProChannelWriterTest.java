package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Track;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * La otra mitad de la escala de la mesa: el archivo espera las perillas en sus dieciseis
 * pasos, asi que escribir el valor de MIDI tal cual deja un archivo que ningun Guitar Pro
 * entiende.
 */
class GuitarProChannelWriterTest {

    private static final int CHANNEL_COUNT = Channel.PORT_COUNT * Channel.CHANNELS_PER_PORT;

    /** El instrumento ocupa los cuatro bytes previos a la primera perilla. */
    private static final int PROGRAM_BYTES = 4;

    private final GuitarProChannelWriter writer = new GuitarProChannelWriter();

    @Test
    void theKnobsGoOutInTheirSixteenSteps() {
        GuitarProByteReader written = write(Channel.playing(Track.GUITAR_PROGRAM)
                .withVolume(104)
                .withPan(Channel.CENTER_PAN));

        written.skip(PROGRAM_BYTES);
        assertEquals(13, written.readUnsignedByte(), "el volumen de siempre de Guitar Pro");
        assertEquals(8, written.readUnsignedByte(), "el paneo al centro");
    }

    @Test
    void theTopMidiValueIsTheTopStep() {
        GuitarProByteReader written = write(Channel.playing(Track.GUITAR_PROGRAM).withVolume(Channel.MAX));

        written.skip(PROGRAM_BYTES);
        assertEquals(16, written.readUnsignedByte());
    }

    @Test
    void silenceIsTheZeroStep() {
        GuitarProByteReader written = write(Channel.playing(Track.GUITAR_PROGRAM).withVolume(0));

        written.skip(PROGRAM_BYTES);
        assertEquals(0, written.readUnsignedByte());
    }

    private GuitarProByteReader write(Channel channel) {
        List<Channel> table = new java.util.ArrayList<>(
                Collections.nCopies(CHANNEL_COUNT, Channel.playing(0)));
        table.set(0, channel);
        GuitarProByteWriter bytes = new GuitarProByteWriter();
        writer.write(bytes, table);
        return new GuitarProByteReader(bytes.bytes());
    }
}
