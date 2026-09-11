package com.gstncaruso.tabpro.format.powertab;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

final class PowerTabByteReader {

    /** The MFC archiver announces a new class with this 16-bit value. */
    private static final int NEW_CLASS_TAG = 0xffff;

    /** A large object uses a 32-bit identifier instead of a 16-bit one. */
    private static final int BIG_OBJECT_TAG = 0x7fff;

    /** The bit that in the 16-bit word marks the tag as belonging to a class. */
    private static final int CLASS_TAG = 0x8000;

    /** The same bit, already shifted to its place within the 32-bit tag. */
    private static final long BIG_CLASS_TAG = 0x80000000L;

    private final byte[] data;
    private int position;

    PowerTabByteReader(byte[] data) {
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

    boolean readBoolean() {
        return readUnsignedByte() != 0;
    }

    /** An unsigned 16-bit integer, little endian. */
    int readUnsignedShort() {
        require(2);
        int value = (data[position] & 0xFF) | ((data[position + 1] & 0xFF) << 8);
        position += 2;
        return value;
    }

    /** A 32-bit integer, little endian. The top bit is handled by hand where needed. */
    int readInt() {
        require(4);
        int value = (data[position] & 0xFF)
                | ((data[position + 1] & 0xFF) << 8)
                | ((data[position + 2] & 0xFF) << 16)
                | ((data[position + 3] & 0xFF) << 24);
        position += 4;
        return value;
    }

    /**
     * The count of an MFC-style vector: a 16-bit integer, and if it is 0xffff
     * (overflow) a 32-bit one right after.
     */
    int readCount() {
        int wordCount = readUnsignedShort();
        if (wordCount != 0xffff) {
            return wordCount;
        }
        int dwordCount = readInt();
        if (dwordCount < 0) {
            throw ScoreFileException.damaged("corrupt PowerTab file: negative vector count");
        }
        return dwordCount;
    }

    /**
     * "MFC string": a variable-size length (1, 2, or 4 bytes as needed) followed by the
     * text in ISO 8859-1.
     */
    String readMfcString() {
        int length = readMfcStringLength();
        if (length == 0) {
            return "";
        }
        require(length);
        String text = new String(data, position, length, StandardCharsets.ISO_8859_1);
        position += length;
        return text;
    }

    private int readMfcStringLength() {
        int byteLength = readUnsignedByte();
        if (byteLength < 0xff) {
            return byteLength;
        }
        int wordLength = readUnsignedShort();
        if (wordLength < 0xffff) {
            return wordLength;
        }
        return readInt();
    }

    /**
     * The class tag MFC prepends to every object of a vector: the first time a class
     * appears it carries its schema and its name; the following times it is only a
     * short reference. It has no musical meaning, but it must be consumed all the same
     * to not lose the file's sync.
     */
    void readClassInformation() {
        int wordTag = readUnsignedShort();
        long objTag;
        if (wordTag == BIG_OBJECT_TAG) {
            objTag = readInt() & 0xFFFFFFFFL;
        } else {
            objTag = (((long) (wordTag & CLASS_TAG)) << 16) | (wordTag & ~CLASS_TAG);
        }

        if ((objTag & BIG_CLASS_TAG) == 0) {
            return;
        }

        if (wordTag == NEW_CLASS_TAG) {
            readUnsignedShort(); // class schema, not needed.
            int nameLength = readUnsignedShort();
            skip(nameLength);
        }
    }

    /**
     * An MFC-style vector of complete objects: the count, and for each element its
     * class tag followed by the object itself.
     */
    <T> List<T> readVector(Function<PowerTabByteReader, T> readOne) {
        int count = readCount();
        List<T> items = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            readClassInformation();
            items.add(readOne.apply(this));
        }
        return items;
    }

    /**
     * An MFC-style vector that gets discarded: same as {@link #readVector}, but keeping
     * nothing. Returns the count, so whoever asks can decide whether a non-empty vector
     * is enough to reject the file.
     */
    int skipVector(Consumer<PowerTabByteReader> skipOne) {
        int count = readCount();
        for (int i = 0; i < count; i++) {
            readClassInformation();
            skipOne.accept(this);
        }
        return count;
    }

    /** A "small" vector of unsigned bytes: a size byte and that many values. */
    int[] readSmallVectorOfUnsignedBytes() {
        int size = readUnsignedByte();
        int[] values = new int[size];
        for (int i = 0; i < size; i++) {
            values[i] = readUnsignedByte();
        }
        return values;
    }

    /**
     * A fixed array of 32-bit integers, with the same encoding as a small vector: a
     * size byte and that many values, without filling the rest of the array (it is
     * padded with zero, as in C++).
     */
    int[] readSmallFixedArrayOfInts(int capacity) {
        int size = readUnsignedByte();
        if (size > capacity) {
            throw ScoreFileException.damaged(
                    "corrupt PowerTab file: a symbol array holds " + size + " items but the maximum is " + capacity);
        }
        int[] values = new int[capacity];
        for (int i = 0; i < size; i++) {
            values[i] = readInt();
        }
        return values;
    }

    private void require(int byteCount) {
        if (byteCount < 0 || position + byteCount > data.length) {
            throw ScoreFileException.damaged(
                    "truncated PowerTab file: expected " + byteCount
                            + " bytes at position " + position
                            + " but only " + (data.length - position) + " remain");
        }
    }
}
