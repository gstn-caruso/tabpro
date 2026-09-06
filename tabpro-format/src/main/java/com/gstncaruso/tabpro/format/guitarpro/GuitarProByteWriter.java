package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.ScoreColor;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Escribe los tipos primitivos del formato binario de Guitar Pro: el espejo de
 * {@link GuitarProByteReader}. No sabe nada de partituras: eso es responsabilidad de los
 * escritores de cada seccion del archivo.
 */
final class GuitarProByteWriter {

    /** El largo mas grande que entra en el byte de longitud redundante. */
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

    /** El unico campo del formato que va en big endian: la duracion en GP5. GP4 no lo usa. */
    GuitarProByteWriter writeDoubleBigEndian(double value) {
        long bits = Double.doubleToLongBits(value);
        for (int shift = 56; shift >= 0; shift -= 8) {
            writeUnsignedByte((int) (bits >> shift) & 0xFF);
        }
        return this;
    }

    /** Un color RGBA; el cuarto byte no se usa y se escribe en cero. */
    GuitarProByteWriter writeColor(ScoreColor color) {
        writeUnsignedByte(color.red());
        writeUnsignedByte(color.green());
        writeUnsignedByte(color.blue());
        writeUnsignedByte(0);
        return this;
    }

    /** La armadura: un byte con la cantidad de alteraciones y otro con el modo. */
    GuitarProByteWriter writeKeySignature(KeySignature keySignature) {
        writeSignedByte(keySignature.accidentals());
        writeUnsignedByte(keySignature.mode() == Mode.MAJOR ? 0 : 1);
        return this;
    }

    /**
     * "Byte-size string": un byte de longitud y un bloque de tamano fijo. Un texto mas
     * largo que el bloque se trunca: no hay forma de que entre entero.
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

    /** "Int-size string" sin byte de longitud extra. */
    GuitarProByteWriter writeIntPrefixedString(String text) {
        byte[] raw = ascii(text);
        writeInt(raw.length);
        buffer.writeBytes(raw);
        return this;
    }

    /**
     * "Int-size string" con el byte de largo redundante que usa la cabecera, los marcadores
     * y los nombres de acorde. Ese byte es de 0 a 255: un texto mas largo se trunca.
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
