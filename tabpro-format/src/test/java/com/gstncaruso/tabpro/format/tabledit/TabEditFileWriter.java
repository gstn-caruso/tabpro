package com.gstncaruso.tabpro.format.tabledit;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Armador de bytes de prueba: escribe archivos binarios minimos de TablEdit
 * (formato TEF3) con el mismo layout que entiende {@link TabEditByteReader},
 * para poder probar el lector sin depender de archivos reales de TablEdit.
 */
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

    /** El string con prefijo de largo corto que usa TablEdit para los metadatos de la cancion. */
    TabEditFileWriter writeShortString(String text) {
        byte[] raw = ascii(text);
        writeShort(raw.length);
        buffer.writeBytes(raw);
        return this;
    }

    /** Un string terminado en un byte nulo, sin prefijo de largo. */
    TabEditFileWriter writeNullTerminatedString(String text) {
        buffer.writeBytes(ascii(text));
        writeUnsignedByte(0);
        return this;
    }

    /** Rellena con ceros hasta que el buffer mida exactamente esa cantidad de bytes. */
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
