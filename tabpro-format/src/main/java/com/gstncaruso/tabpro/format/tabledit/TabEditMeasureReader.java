package com.gstncaruso.tabpro.format.tabledit;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import java.util.ArrayList;
import java.util.List;

/**
 * Lee la lista de compases: cada uno es un registro de tamano fijo (declarado
 * en el archivo con 4 bytes de mas), del que solo hacen falta los primeros 8
 * para la medida y la armadura. El resto es relleno que no hace falta
 * entender: leer el bloque entero de una vez alcanza para no desalinearse.
 */
final class TabEditMeasureReader {

    private static final int MINOR_KEY_BIT = 5;

    List<TabEditMeasure> read(TabEditByteReader input) {
        int measureRecordSize = input.readUnsignedShort() - 4;
        int measureCount = input.readUnsignedShort();
        input.skip(4); // relleno fijo, siempre en cero

        List<TabEditMeasure> measures = new ArrayList<>(measureCount);
        for (int i = 0; i < measureCount; i++) {
            measures.add(readOne(new TabEditByteReader(input.readBlock(measureRecordSize))));
        }
        return measures;
    }

    private TabEditMeasure readOne(TabEditByteReader record) {
        int flags = record.readUnsignedByte();
        boolean minorKey = (flags & (1 << MINOR_KEY_BIT)) != 0;
        record.skip(1);
        int keySignatureAccidentals = record.readSignedByte();
        record.skip(1);
        int denominator = record.readUnsignedByte();
        int numerator = record.readUnsignedByte();
        // El resto del registro (ancho de relleno a la izquierda y lo que sobre) no hace
        // falta para la medida ni la armadura, y ya quedo consumido al leer el bloque entero.

        TimeSignature timeSignature = new TimeSignature(numerator, denominator);
        KeySignature keySignature =
                new KeySignature(keySignatureAccidentals, minorKey ? Mode.MINOR : Mode.MAJOR);
        return new TabEditMeasure(timeSignature, keySignature);
    }
}
