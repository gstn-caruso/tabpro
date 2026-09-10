package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.ScoreColor;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import java.nio.charset.StandardCharsets;

final class GuitarProByteReader {

    private final byte[] data;
    private int position;

    GuitarProByteReader(byte[] data) {
        this.data = data;
    }

    int position() {
        return position;
    }

    boolean hasMore() {
        return position < data.length;
    }

    void skip(int byteCount) {
        require(byteCount);
        position += byteCount;
    }

    int readUnsignedByte() {
        require(1);
        return data[position++] & 0xFF;
    }

    int readSignedByte() {
        require(1);
        return data[position++];
    }

    boolean readBoolean() {
        return readUnsignedByte() != 0;
    }

    int readShort() {
        require(2);
        int value = (data[position] & 0xFF) | ((data[position + 1] & 0xFF) << 8);
        position += 2;
        return (short) value;
    }

    int readInt() {
        require(4);
        int value = (data[position] & 0xFF)
                | ((data[position + 1] & 0xFF) << 8)
                | ((data[position + 2] & 0xFF) << 16)
                | ((data[position + 3] & 0xFF) << 24);
        position += 4;
        return value;
    }

    /** The only field in the format that comes in big-endian order: the duration in GP5. */
    double readDoubleBigEndian() {
        require(8);
        long bits = 0;
        for (int i = 0; i < 8; i++) {
            bits = (bits << 8) | (data[position + i] & 0xFFL);
        }
        position += 8;
        return Double.longBitsToDouble(bits);
    }

    /** An RGBA color; the fourth byte is unused. */
    ScoreColor readColor() {
        int red = readUnsignedByte();
        int green = readUnsignedByte();
        int blue = readUnsignedByte();
        readUnsignedByte();
        return new ScoreColor(red, green, blue);
    }

    /** The key signature: one byte for the accidental count and another for the mode. */
    KeySignature readKeySignature() {
        int accidentals = readSignedByte();
        int modeByte = readUnsignedByte();
        return new KeySignature(accidentals, modeByte == 0 ? Mode.MAJOR : Mode.MINOR);
    }

    /**
     * "Byte-size string": a length byte followed by a fixed-size block
     * holding the text and its padding.
     */
    String readFixedString(int fixedLength) {
        int length = readUnsignedByte();
        require(fixedLength);
        String text = decode(position, Math.min(length, fixedLength));
        position += fixedLength;
        return text;
    }

    /** "Int-size string" with no extra length byte: an integer followed by the text. */
    String readIntPrefixedString() {
        int length = readInt();
        require(length);
        String text = decode(position, length);
        position += length;
        return text;
    }

    /**
     * "Int-size string" with a redundant length byte: an integer (length
     * plus one, for historical compatibility), a byte with the actual
     * length, and the text. This is what Guitar Pro uses for the header,
     * markers and page templates.
     */
    String readLengthPrefixedString() {
        readInt();
        int length = readUnsignedByte();
        require(length);
        String text = decode(position, length);
        position += length;
        return text;
    }

    private String decode(int from, int length) {
        return new String(data, from, length, StandardCharsets.ISO_8859_1);
    }

    private void require(int byteCount) {
        if (byteCount < 0 || position + byteCount > data.length) {
            throw new ScoreFileException(
                    "archivo Guitar Pro truncado: se esperaban " + byteCount
                            + " bytes en la posicion " + position
                            + " pero solo quedan " + (data.length - position));
        }
    }
}
