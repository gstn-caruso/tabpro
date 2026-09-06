package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendPoint;
import com.gstncaruso.tabpro.core.model.effects.BendType;
import java.util.List;
import org.junit.jupiter.api.Test;

/** La curva se escribe en las mismas unidades en que la lee el formato: 25 por cuarto de tono. */
class GuitarProBendWriterTest {

    private static final int WHOLE_TONE_IN_QUARTER_TONES = 4;
    private static final int WHOLE_TONE_RAW = 100;

    private final GuitarProBendWriter writer = new GuitarProBendWriter();

    @Test
    void unTonoEnteroSeEscribeComoCien() {
        Bend bend = new Bend(BendType.BEND, List.of(
                BendPoint.at(0, 0),
                BendPoint.at(BendPoint.LAST_POSITION, WHOLE_TONE_IN_QUARTER_TONES)));

        GuitarProByteWriter bytes = new GuitarProByteWriter();
        writer.write(bytes, bend);
        GuitarProByteReader reader = new GuitarProByteReader(bytes.bytes());

        reader.readSignedByte(); // tipo
        assertEquals(WHOLE_TONE_RAW, reader.readInt(), "la profundidad general");
        assertEquals(2, reader.readInt());
        reader.readInt();
        assertEquals(0, reader.readInt());
        reader.readUnsignedByte();
        assertEquals(BendPoint.LAST_POSITION, reader.readInt());
        assertEquals(WHOLE_TONE_RAW, reader.readInt());
    }
}
