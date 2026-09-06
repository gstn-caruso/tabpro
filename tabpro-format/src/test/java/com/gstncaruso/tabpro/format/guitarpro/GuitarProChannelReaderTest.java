package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Channel;
import org.junit.jupiter.api.Test;

/**
 * Los bytes de esta tabla son los que graba Guitar Pro: la perilla de volumen en 13 y la
 * de paneo en 8 son los valores con que nace cualquier pista suya.
 */
class GuitarProChannelReaderTest {

    /** Cuantos bytes ocupa un canal: el instrumento, las seis perillas y dos de relleno. */
    private static final int CHANNEL_BYTES = 4 + 6 + 2;
    private static final int CHANNEL_COUNT = Channel.PORT_COUNT * Channel.CHANNELS_PER_PORT;

    private static final int UNSET = 0xFF;

    private final GuitarProChannelReader reader = new GuitarProChannelReader();

    /**
     * Las perillas de la mesa tienen dieciseis pasos, no ciento veintiocho: leerlas como
     * si fueran valores de MIDI deja toda pista importada casi muda y pegada a la
     * izquierda.
     */
    @Test
    void theKnobsComeInSixteenStepsAndTheModelUsesMidi() {
        GuitarProChannel channel = firstOf(25, 13, 8, 0, 0, 0, 0);

        assertEquals(25, channel.program());
        assertEquals(104, channel.volume());
        assertEquals(64, channel.pan());
    }

    @Test
    void theTopStepIsTheTopMidiValue() {
        GuitarProChannel channel = firstOf(25, 16, 16, 16, 16, 16, 16);

        assertEquals(Channel.MAX, channel.volume());
        assertEquals(Channel.MAX, channel.tremolo());
    }

    @Test
    void theZeroStepIsSilence() {
        assertEquals(0, firstOf(25, 0, 0, 0, 0, 0, 0).volume());
    }

    @Test
    void theOtherKnobsUseTheSameSteps() {
        GuitarProChannel channel = firstOf(25, 13, 8, 1, 2, 3, 4);

        assertEquals(8, channel.chorus());
        assertEquals(16, channel.reverb());
        assertEquals(24, channel.phaser());
        assertEquals(32, channel.tremolo());
    }

    /** Un byte en 0xFF dice que el parametro no se toco: ahi vale el valor por defecto. */
    @Test
    void anUntouchedKnobKeepsItsDefault() {
        GuitarProChannel channel = firstOf(25, UNSET, UNSET, 0, 0, 0, 0);

        assertEquals(Channel.DEFAULT_VOLUME, channel.volume());
        assertEquals(Channel.CENTER_PAN, channel.pan());
    }

    private GuitarProChannel firstOf(int program, int... knobs) {
        GuitarProFileWriter written = new GuitarProFileWriter().writeInt(program);
        for (int knob : knobs) {
            written.writeUnsignedByte(knob);
        }
        written.writeShort(0);
        for (int rest = 0; rest < (CHANNEL_COUNT - 1) * CHANNEL_BYTES; rest++) {
            written.writeUnsignedByte(0);
        }
        return reader.read(new GuitarProByteReader(written.bytes())).getFirst();
    }
}
