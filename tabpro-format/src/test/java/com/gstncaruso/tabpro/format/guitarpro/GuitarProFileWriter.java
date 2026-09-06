package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.ScoreColor;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Armador de bytes de prueba: escribe archivos binarios minimos de Guitar Pro
 * con el mismo layout que entiende {@link GuitarProByteReader}, para poder
 * probar el lector sin depender de archivos reales.
 */
final class GuitarProFileWriter {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    byte[] bytes() {
        return buffer.toByteArray();
    }

    GuitarProFileWriter writeUnsignedByte(int value) {
        buffer.write(value & 0xFF);
        return this;
    }

    GuitarProFileWriter writeSignedByte(int value) {
        return writeUnsignedByte(value & 0xFF);
    }

    GuitarProFileWriter writeBoolean(boolean value) {
        return writeUnsignedByte(value ? 1 : 0);
    }

    GuitarProFileWriter writeShort(int value) {
        writeUnsignedByte(value & 0xFF);
        writeUnsignedByte((value >> 8) & 0xFF);
        return this;
    }

    GuitarProFileWriter writeInt(int value) {
        writeUnsignedByte(value & 0xFF);
        writeUnsignedByte((value >> 8) & 0xFF);
        writeUnsignedByte((value >> 16) & 0xFF);
        writeUnsignedByte((value >> 24) & 0xFF);
        return this;
    }

    GuitarProFileWriter writeDoubleBigEndian(double value) {
        long bits = Double.doubleToLongBits(value);
        for (int shift = 56; shift >= 0; shift -= 8) {
            writeUnsignedByte((int) (bits >> shift) & 0xFF);
        }
        return this;
    }

    GuitarProFileWriter writeColor(ScoreColor color) {
        writeUnsignedByte(color.red());
        writeUnsignedByte(color.green());
        writeUnsignedByte(color.blue());
        writeUnsignedByte(0);
        return this;
    }

    GuitarProFileWriter writeKeySignature(KeySignature keySignature) {
        writeSignedByte(keySignature.accidentals());
        writeUnsignedByte(keySignature.mode() == Mode.MAJOR ? 0 : 1);
        return this;
    }

    /** "Byte-size string": un byte de largo y un bloque de tamano fijo. */
    GuitarProFileWriter writeFixedString(String text, int fixedLength) {
        byte[] raw = ascii(text);
        writeUnsignedByte(raw.length);
        buffer.writeBytes(raw);
        for (int i = raw.length; i < fixedLength; i++) {
            writeUnsignedByte(0);
        }
        return this;
    }

    /** "Int-size string" sin byte de largo extra. */
    GuitarProFileWriter writeIntPrefixedString(String text) {
        byte[] raw = ascii(text);
        writeInt(raw.length);
        buffer.writeBytes(raw);
        return this;
    }

    /** "Int-size string" con el byte de largo redundante que usa la cabecera. */
    GuitarProFileWriter writeLengthPrefixedString(String text) {
        byte[] raw = ascii(text);
        writeInt(raw.length + 1);
        writeUnsignedByte(raw.length);
        buffer.writeBytes(raw);
        return this;
    }

    GuitarProFileWriter writeVersion(String header) {
        return writeFixedString(header, 30);
    }

    private static byte[] ascii(String text) {
        return text.getBytes(StandardCharsets.ISO_8859_1);
    }
}
