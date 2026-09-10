package com.gstncaruso.tabpro.format.tabledit;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

final class TabEditByteReader {

    private final byte[] data;
    private int position;

    TabEditByteReader(byte[] data) {
        this.data = data;
    }

    int position() {
        return position;
    }

    boolean hasMore() {
        return position < data.length;
    }

    int remaining() {
        return data.length - position;
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

    /** TablEdit stores shorts unsigned: string lengths, measure counts, and the like. */
    int readUnsignedShort() {
        require(2);
        int value = (data[position] & 0xFF) | ((data[position + 1] & 0xFF) << 8);
        position += 2;
        return value;
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

    /** A fixed-size block, to later wrap in its own {@link TabEditByteReader}. */
    byte[] readBlock(int byteCount) {
        require(byteCount);
        byte[] block = Arrays.copyOfRange(data, position, position + byteCount);
        position += byteCount;
        return block;
    }

    /**
     * The song metadata string: an unsigned short with the length, and that many
     * characters. If a null byte appears before the end, TablEdit cuts it off right
     * there and does not consume the rest of the declared field.
     */
    String readShortString() {
        int length = readUnsignedShort();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int byteRead = readUnsignedByte();
            if (byteRead == 0) {
                break;
            }
            text.append((char) byteRead);
        }
        return text.toString();
    }

    /** A string that ends in a null byte, like a track's name. */
    String readNullTerminatedString(int maxLength) {
        StringBuilder text = new StringBuilder();
        int byteRead;
        while ((byteRead = readUnsignedByte()) != 0) {
            if (text.length() >= maxLength) {
                break;
            }
            text.append((char) byteRead);
        }
        return text.toString();
    }

    String decode(byte[] bytes) {
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }

    private void require(int byteCount) {
        if (byteCount < 0 || position + byteCount > data.length) {
            throw new ScoreFileException(
                    "archivo de TablEdit truncado: se esperaban " + byteCount
                            + " bytes en la posicion " + position
                            + " pero solo quedan " + (data.length - position));
        }
    }
}
