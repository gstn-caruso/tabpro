package com.gstncaruso.tabpro.format.tabledit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Los compases: cada uno trae su medida y su armadura en un registro de
 * tamano fijo, que puede traer relleno de mas segun como lo grabo TablEdit.
 */
class TabEditMeasureReaderTest {

    private final TabEditMeasureReader reader = new TabEditMeasureReader();

    @Test
    void leeLaMedidaYLaArmaduraDeCadaCompas() {
        TabEditFileWriter writer = new TabEditFileWriter()
                .writeShort(12) // sizeOfMeasure (8) + 4
                .writeShort(2) // measureCount
                .writeInt(0); // relleno fijo
        writeMeasure(writer, 0, false, 4, 4); // 4/4, Do mayor
        writeMeasure(writer, 3, false, 3, 4); // 3/4, La mayor (3 sostenidos)

        List<TabEditMeasure> measures = reader.read(new TabEditByteReader(writer.bytes()));

        assertEquals(2, measures.size());
        assertEquals(TimeSignature.fourFour(), measures.get(0).timeSignature());
        assertEquals(0, measures.get(0).keySignature().accidentals());
        assertEquals(new TimeSignature(3, 4), measures.get(1).timeSignature());
        assertEquals(3, measures.get(1).keySignature().accidentals());
    }

    @Test
    void unaArmaduraConBemolesYModoMenor() {
        TabEditFileWriter writer = new TabEditFileWriter().writeShort(12).writeShort(1).writeInt(0);
        writeMeasure(writer, -2, true, 4, 4); // 2 bemoles, modo menor

        TabEditMeasure measure = reader.read(new TabEditByteReader(writer.bytes())).get(0);

        assertEquals(-2, measure.keySignature().accidentals());
        assertEquals(Mode.MINOR, measure.keySignature().mode());
    }

    @Test
    void respetaElRellenoExtraDeCadaRegistro() {
        // sizeOfMeasure declarado como 12 (16-4): quedan 4 bytes de relleno por compas
        // ademas de los 8 que se interpretan, y el lector no se puede desalinear por eso.
        TabEditFileWriter writer = new TabEditFileWriter().writeShort(16).writeShort(1).writeInt(0);
        writeMeasureWithPadding(writer, 0, false, 4, 4, 4);
        writer.writeUnsignedByte(55); // marca para confirmar que la lectura sigue alineada

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
        writer.writeUnsignedByte(0); // leftWidthPadding
        writer.writeUnsignedByte(0);
        for (int i = 0; i < extraPadding; i++) {
            writer.writeUnsignedByte(0);
        }
    }
}
