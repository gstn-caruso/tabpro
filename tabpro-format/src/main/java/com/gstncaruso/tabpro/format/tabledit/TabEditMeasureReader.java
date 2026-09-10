package com.gstncaruso.tabpro.format.tabledit;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the list of measures: each one is a fixed-size record (declared in the file
 * with 4 extra bytes), of which only the first 8 are needed for the time signature and
 * key signature. The rest is padding that does not need to be understood: reading the
 * whole block at once is enough to not lose alignment.
 */
final class TabEditMeasureReader {

    private static final int MINOR_KEY_BIT = 5;

    List<TabEditMeasure> read(TabEditByteReader input) {
        int measureRecordSize = input.readUnsignedShort() - 4;
        int measureCount = input.readUnsignedShort();
        input.skip(4); // fixed padding, always zero

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
        // The rest of the record (left-padding width and whatever else) is not needed
        // for the time signature or key signature, and is already consumed by reading
        // the whole block.

        TimeSignature timeSignature = new TimeSignature(numerator, denominator);
        KeySignature keySignature =
                new KeySignature(keySignatureAccidentals, minorKey ? Mode.MINOR : Mode.MAJOR);
        return new TabEditMeasure(timeSignature, keySignature);
    }
}
