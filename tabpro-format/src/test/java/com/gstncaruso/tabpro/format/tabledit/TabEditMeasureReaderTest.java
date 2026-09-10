package com.gstncaruso.tabpro.format.tabledit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import java.util.List;
import org.junit.jupiter.api.Test;

class TabEditMeasureReaderTest {

    private final TabEditMeasureReader reader = new TabEditMeasureReader();

    @Test
    void readsTheTimeSignatureAndKeySignatureOfEachBar() {
        TabEditFileWriter writer = new TabEditFileWriter()
                .writeShort(12)
                .writeShort(2)
                .writeInt(0);
        writeMeasure(writer, 0, false, 4, 4);
        writeMeasure(writer, 3, false, 3, 4);

        List<TabEditMeasure> measures = reader.read(new TabEditByteReader(writer.bytes()));

        assertEquals(2, measures.size());
        assertEquals(TimeSignature.fourFour(), measures.get(0).timeSignature());
        assertEquals(0, measures.get(0).keySignature().accidentals());
        assertEquals(new TimeSignature(3, 4), measures.get(1).timeSignature());
        assertEquals(3, measures.get(1).keySignature().accidentals());
    }

    @Test
    void aKeySignatureWithFlatsAndMinorMode() {
        TabEditFileWriter writer = new TabEditFileWriter().writeShort(12).writeShort(1).writeInt(0);
        writeMeasure(writer, -2, true, 4, 4);

        TabEditMeasure measure = reader.read(new TabEditByteReader(writer.bytes())).get(0);

        assertEquals(-2, measure.keySignature().accidentals());
        assertEquals(Mode.MINOR, measure.keySignature().mode());
    }

    @Test
    void respectsTheExtraPaddingOfEachRecord() {
        TabEditFileWriter writer = new TabEditFileWriter().writeShort(16).writeShort(1).writeInt(0);
        writeMeasureWithPadding(writer, 0, false, 4, 4, 4);
        writer.writeUnsignedByte(55);

        TabEditByteReader input = new TabEditByteReader(writer.bytes());
        List<TabEditMeasure> measures = reader.read(input);

        assertEquals(1, measures.size());
        assertEquals(55, input.readUnsignedByte());
    }

    private static void writeMeasure(
            TabEditFileWriter writer, int keySignature, boolean minorKey, int numerator, int denominator) {
        writeMeasureWithPadding(writer, keySignature, minorKey, numerator, denominator, 0);
    }

    private static void writeMeasureWithPadding(
            TabEditFileWriter writer, int keySignature, boolean minorKey, int numerator, int denominator,
            int extraPadding) {
        int flags = minorKey ? (1 << 5) : 0;
        writer.writeUnsignedByte(flags);
        writer.writeUnsignedByte(0);
        writer.writeSignedByte(keySignature);
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(denominator);
        writer.writeUnsignedByte(numerator);
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(0);
        for (int i = 0; i < extraPadding; i++) {
            writer.writeUnsignedByte(0);
        }
    }
}
