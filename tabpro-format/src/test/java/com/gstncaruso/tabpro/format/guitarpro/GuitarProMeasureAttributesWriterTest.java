package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import org.junit.jupiter.api.Test;

class GuitarProMeasureAttributesWriterTest {

    private static final int FLAG_NUMERATOR = 0x01;
    private static final int FLAG_DENOMINATOR = 0x02;
    private static final int FLAG_KEY_SIGNATURE = 0x40;

    @Test
    void theFirstBarWritesItsTimeSignatureEvenIfItIsFourFour() {
        byte[] bytes = writeBars(TimeSignature.fourFour());

        assertEquals(FLAG_NUMERATOR | FLAG_DENOMINATOR | FLAG_KEY_SIGNATURE, bytes[0] & 0xFF);
        assertEquals(4, bytes[1] & 0xFF);
        assertEquals(4, bytes[2] & 0xFF);
    }

    @Test
    void theFirstBarWritesItsKeySignatureEvenIfItIsCMajor() {
        byte[] bytes = writeBars(TimeSignature.fourFour());

        assertEquals(0, bytes[3]);
        assertEquals(0, bytes[4]);
    }

    @Test
    void theSecondBarWithTheSameTimeSignatureWritesNothing() {
        byte[] bytes = writeBars(TimeSignature.fourFour(), TimeSignature.fourFour());

        assertEquals(0x00, bytes[5] & 0xFF);
    }

    @Test
    void theSecondBarWithADifferentTimeSignatureDoesWriteIt() {
        byte[] bytes = writeBars(TimeSignature.fourFour(), new TimeSignature(3, 4));

        assertEquals(FLAG_NUMERATOR, bytes[5] & 0xFF);
        assertEquals(3, bytes[6] & 0xFF);
    }

    private static byte[] writeBars(TimeSignature... timeSignatures) {
        GuitarProByteWriter writer = new GuitarProByteWriter();
        GuitarProMeasureAttributesWriter bars = new GuitarProMeasureAttributesWriter();
        MeasureAttributes attributes = MeasureAttributes.plain().withKeySignature(new KeySignature(0, Mode.MAJOR));
        for (TimeSignature timeSignature : timeSignatures) {
            bars.write(writer, timeSignature, attributes);
        }
        return writer.bytes();
    }
}
