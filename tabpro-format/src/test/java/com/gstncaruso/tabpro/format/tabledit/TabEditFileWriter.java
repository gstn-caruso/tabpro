package com.gstncaruso.tabpro.format.tabledit;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

final class TabEditFileWriter {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    byte[] bytes() {
        return buffer.toByteArray();
    }

    int size() {
        return buffer.size();
    }

    TabEditFileWriter writeUnsignedByte(int value) {
        buffer.write(value & 0xFF);
        return this;
    }

    TabEditFileWriter writeSignedByte(int value) {
        return writeUnsignedByte(value & 0xFF);
    }

    TabEditFileWriter writeShort(int value) {
        writeUnsignedByte(value & 0xFF);
        writeUnsignedByte((value >> 8) & 0xFF);
        return this;
    }

    TabEditFileWriter writeInt(int value) {
        writeUnsignedByte(value & 0xFF);
        writeUnsignedByte((value >> 8) & 0xFF);
        writeUnsignedByte((value >> 16) & 0xFF);
        writeUnsignedByte((value >> 24) & 0xFF);
        return this;
    }

    TabEditFileWriter writeShortString(String text) {
        byte[] raw = ascii(text);
        writeShort(raw.length);
        buffer.writeBytes(raw);
        return this;
    }

    TabEditFileWriter writeNullTerminatedString(String text) {
        buffer.writeBytes(ascii(text));
        writeUnsignedByte(0);
        return this;
    }

    TabEditFileWriter padTo(int totalSize) {
        while (buffer.size() < totalSize) {
            writeUnsignedByte(0);
        }
        return this;
    }

    private static byte[] ascii(String text) {
        return text.getBytes(StandardCharsets.ISO_8859_1);
    }
}
