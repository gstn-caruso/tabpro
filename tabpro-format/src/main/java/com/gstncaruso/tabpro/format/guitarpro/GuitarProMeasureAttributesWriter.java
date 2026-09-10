package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;

/**
 * Writes a "master bar": the attributes of a measure that Guitar Pro stores once per
 * measure. The mirror of {@link GuitarProMeasureAttributesReader}, but only for GP4: the
 * time signature and key signature are only written when they change from the previous
 * measure.
 *
 * <p>The first measure is the exception, since it has no previous one: there they are
 * always written. A reader does "if the flag is set, read the value; if not, carry over
 * the previous measure's", and the first measure has none to carry over.
 */
final class GuitarProMeasureAttributesWriter {

    private static final int FLAG_NUMERATOR = 0x01;
    private static final int FLAG_DENOMINATOR = 0x02;
    private static final int FLAG_REPEAT_OPEN = 0x04;
    private static final int FLAG_REPEAT_COUNT = 0x08;
    private static final int FLAG_ALTERNATE_ENDINGS_PRE_GP5 = 0x10;
    private static final int FLAG_MARKER = 0x20;
    private static final int FLAG_KEY_SIGNATURE = 0x40;
    private static final int FLAG_DOUBLE_BAR = 0x80;

    /** GP3 writes the repeat count already reduced by one, just like GP4. */
    private static final int REPEAT_COUNT_OFFSET = 1;

    private TimeSignature previousTimeSignature;
    private KeySignature previousKeySignature;

    void write(GuitarProByteWriter writer, TimeSignature timeSignature, MeasureAttributes attributes) {
        boolean writesNumerator = isFirstMeasure() || timeSignature.beats() != previousTimeSignature.beats();
        boolean writesDenominator = isFirstMeasure() || timeSignature.beatUnit() != previousTimeSignature.beatUnit();
        boolean writesKeySignature = isFirstMeasure() || !attributes.keySignature().equals(previousKeySignature);

        int flags = 0;
        if (writesNumerator) {
            flags |= FLAG_NUMERATOR;
        }
        if (writesDenominator) {
            flags |= FLAG_DENOMINATOR;
        }
        if (attributes.repeatOpen()) {
            flags |= FLAG_REPEAT_OPEN;
        }
        if (attributes.repeatCloses()) {
            flags |= FLAG_REPEAT_COUNT;
        }
        if (attributes.hasAlternateEndings()) {
            flags |= FLAG_ALTERNATE_ENDINGS_PRE_GP5;
        }
        if (attributes.marker().isPresent()) {
            flags |= FLAG_MARKER;
        }
        if (writesKeySignature) {
            flags |= FLAG_KEY_SIGNATURE;
        }
        if (attributes.doubleBar()) {
            flags |= FLAG_DOUBLE_BAR;
        }

        writer.writeUnsignedByte(flags);
        if (writesNumerator) {
            writer.writeUnsignedByte(timeSignature.beats());
        }
        if (writesDenominator) {
            writer.writeUnsignedByte(timeSignature.beatUnit());
        }
        if (attributes.repeatCloses()) {
            writer.writeUnsignedByte(attributes.repeatCount() - REPEAT_COUNT_OFFSET);
        }
        if (attributes.hasAlternateEndings()) {
            writer.writeUnsignedByte(endingsToMask(attributes.alternateEndings()));
        }
        if (attributes.marker().isPresent()) {
            Marker marker = attributes.marker().get();
            writer.writeLengthPrefixedString(marker.name());
            writer.writeColor(marker.color());
        }
        if (writesKeySignature) {
            writer.writeKeySignature(attributes.keySignature());
        }

        previousTimeSignature = timeSignature;
        previousKeySignature = attributes.keySignature();
    }

    private boolean isFirstMeasure() {
        return previousTimeSignature == null;
    }

    private static int endingsToMask(java.util.List<Integer> alternateEndings) {
        int mask = 0;
        for (int pass : alternateEndings) {
            mask |= 1 << (pass - 1);
        }
        return mask;
    }
}
