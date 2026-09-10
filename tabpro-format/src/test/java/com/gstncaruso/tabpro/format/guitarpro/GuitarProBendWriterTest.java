package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendPoint;
import com.gstncaruso.tabpro.core.model.effects.BendType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GuitarProBendWriterTest {

    private static final int WHOLE_TONE_IN_QUARTER_TONES = 4;
    private static final int WHOLE_TONE_RAW = 100;

    private final GuitarProBendWriter writer = new GuitarProBendWriter();

    @Test
    void aWholeToneIsWrittenAsOneHundred() {
        Bend bend = new Bend(BendType.BEND, List.of(
                BendPoint.at(0, 0),
                BendPoint.at(BendPoint.LAST_POSITION, WHOLE_TONE_IN_QUARTER_TONES)));

        GuitarProByteWriter bytes = new GuitarProByteWriter();
        writer.write(bytes, bend);
        GuitarProByteReader reader = new GuitarProByteReader(bytes.bytes());

        reader.readSignedByte();
        assertEquals(WHOLE_TONE_RAW, reader.readInt(), "the overall depth");
        assertEquals(2, reader.readInt());
        reader.readInt();
        assertEquals(0, reader.readInt());
        reader.readUnsignedByte();
        assertEquals(BendPoint.LAST_POSITION, reader.readInt());
        assertEquals(WHOLE_TONE_RAW, reader.readInt());
    }

    @Test
    void eachTremoloBarTypeIsWrittenWithItsOwnCode() {
        Map<BendType, Integer> tremoloBarCodes = Map.of(
                BendType.DIP, 6,
                BendType.DIVE, 7,
                BendType.RELEASE_UP, 8,
                BendType.INVERTED_DIP, 9,
                BendType.RETURN, 10,
                BendType.RELEASE_DOWN, 11);

        tremoloBarCodes.forEach((type, expectedCode) -> {
            Bend bend = Bend.of(type, WHOLE_TONE_IN_QUARTER_TONES);
            GuitarProByteWriter bytes = new GuitarProByteWriter();
            writer.write(bytes, bend);

            int actualCode = new GuitarProByteReader(bytes.bytes()).readSignedByte();
            assertEquals(expectedCode, actualCode, "type " + type + " must be written with its own code");
        });
    }
}
