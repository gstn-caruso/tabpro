package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.ScoreColor;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

final class GuitarProByteWriter {

    /** The largest length that fits in the redundant length byte. */
    private static final int MAX_LENGTH_PREFIXED = 255;

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    byte[] bytes() {
        return buffer.toByteArray();
    }

    GuitarProByteWriter writeUnsignedByte(int value) {
        buffer.write(value & 0xFF);
        return this;
    }

    GuitarProByteWriter writeSignedByte(int value) {
        return writeUnsignedByte(value & 0xFF);
    }

    GuitarProByteWriter writeBoolean(boolean value) {
        return writeUnsignedByte(value ? 1 : 0);
    }

    GuitarProByteWriter writeShort(int value) {
        writeUnsignedByte(value & 0xFF);
        writeUnsignedByte((value >> 8) & 0xFF);
        return this;
    }

    GuitarProByteWriter writeInt(int value) {
        writeUnsignedByte(value & 0xFF);
        writeUnsignedByte((value >> 8) & 0xFF);
        writeUnsignedByte((value >> 16) & 0xFF);
        writeUnsignedByte((value >> 24) & 0xFF);
        return this;
    }

    /** The only field in the format that goes in big-endian order: the duration in GP5. GP4 does not use it. */
    GuitarProByteWriter writeDoubleBigEndian(double value) {
        long bits = Double.doubleToLongBits(value);
        for (int shift = 56; shift >= 0; shift -= 8) {
            writeUnsignedByte((int) (bits >> shift) & 0xFF);
        }
        return this;
    }

    /** An RGBA color; the fourth byte is unused and is written as zero. */
    GuitarProByteWriter writeColor(ScoreColor color) {
        writeUnsignedByte(color.red());
        writeUnsignedByte(color.green());
        writeUnsignedByte(color.blue());
        writeUnsignedByte(0);
        return this;
    }

    /** The key signature: one byte for the accidental count and another for the mode. */
    GuitarProByteWriter writeKeySignature(KeySignature keySignature) {
        writeSignedByte(keySignature.accidentals());
        writeUnsignedByte(keySignature.mode() == Mode.MAJOR ? 0 : 1);
        return this;
    }

    /**
     * "Byte-size string": a length byte followed by a fixed-size block. A text longer
     * than the block gets truncated: there is no way to fit it whole.
     */
    GuitarProByteWriter writeFixedString(String text, int fixedLength) {
        byte[] raw = truncated(ascii(text), fixedLength);
        writeUnsignedByte(raw.length);
        buffer.writeBytes(raw);
        for (int i = raw.length; i < fixedLength; i++) {
            writeUnsignedByte(0);
        }
        return this;
    }

    /** "Int-size string" with no extra length byte. */
    GuitarProByteWriter writeIntPrefixedString(String text) {
        byte[] raw = ascii(text);
        writeInt(raw.length);
        buffer.writeBytes(raw);
        return this;
    }

    /**
     * "Int-size string" with the redundant length byte used by the header, the markers,
     * and the chord names. That byte ranges from 0 to 255: a longer text gets truncated.
     */
    GuitarProByteWriter writeLengthPrefixedString(String text) {
        byte[] raw = truncated(ascii(text), MAX_LENGTH_PREFIXED);
        writeInt(raw.length + 1);
        writeUnsignedByte(raw.length);
        buffer.writeBytes(raw);
        return this;
    }

    GuitarProByteWriter writeVersion(String header) {
        return writeFixedString(header, 30);
    }

    private static byte[] truncated(byte[] raw, int maxLength) {
        return raw.length > maxLength ? java.util.Arrays.copyOf(raw, maxLength) : raw;
    }

    private static byte[] ascii(String text) {
        return text.getBytes(StandardCharsets.ISO_8859_1);
    }
}
