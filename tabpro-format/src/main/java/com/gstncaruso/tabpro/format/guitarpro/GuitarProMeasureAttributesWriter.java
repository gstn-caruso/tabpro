package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;

/**
 * Escribe un "master bar": los atributos de un compas que Guitar Pro guarda una sola vez
 * por compas. El espejo de {@link GuitarProMeasureAttributesReader}, pero solo para GP4:
 * la medida y la armadura solo se escriben cuando cambian respecto del compas anterior.
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

    /** GP3 escribe la cuenta de repeticion ya restada en uno, igual que GP4. */
    private static final int REPEAT_COUNT_OFFSET = 1;

    private TimeSignature previousTimeSignature;
    private KeySignature previousKeySignature;

    GuitarProMeasureAttributesWriter(TimeSignature initialTimeSignature, KeySignature initialKeySignature) {
        this.previousTimeSignature = initialTimeSignature;
        this.previousKeySignature = initialKeySignature;
    }

    void write(GuitarProByteWriter writer, TimeSignature timeSignature, MeasureAttributes attributes) {
        boolean numeratorChanged = timeSignature.beats() != previousTimeSignature.beats();
        boolean denominatorChanged = timeSignature.beatUnit() != previousTimeSignature.beatUnit();
        boolean keySignatureChanged = !attributes.keySignature().equals(previousKeySignature);

        int flags = 0;
        if (numeratorChanged) {
            flags |= FLAG_NUMERATOR;
        }
        if (denominatorChanged) {
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
        if (keySignatureChanged) {
            flags |= FLAG_KEY_SIGNATURE;
        }
        if (attributes.doubleBar()) {
            flags |= FLAG_DOUBLE_BAR;
        }

        writer.writeUnsignedByte(flags);
        if (numeratorChanged) {
            writer.writeUnsignedByte(timeSignature.beats());
        }
        if (denominatorChanged) {
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
        if (keySignatureChanged) {
            writer.writeKeySignature(attributes.keySignature());
        }

        previousTimeSignature = timeSignature;
        previousKeySignature = attributes.keySignature();
    }

    private static int endingsToMask(java.util.List<Integer> alternateEndings) {
        int mask = 0;
        for (int pass : alternateEndings) {
            mask |= 1 << (pass - 1);
        }
        return mask;
    }
}
