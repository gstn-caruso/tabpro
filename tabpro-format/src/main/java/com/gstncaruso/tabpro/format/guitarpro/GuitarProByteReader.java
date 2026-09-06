package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.ScoreColor;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import java.nio.charset.StandardCharsets;

/**
 * Lee los tipos primitivos del formato binario de Guitar Pro sobre un arreglo
 * de bytes: enteros little endian, colores, y los dos sabores de string que
 * usa el formato. No sabe nada de partituras: eso es responsabilidad de los
 * lectores de cada seccion del archivo.
 */
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

    /** El unico campo del formato que viene en big endian: la duracion en GP5. */
    double readDoubleBigEndian() {
        require(8);
        long bits = 0;
        for (int i = 0; i < 8; i++) {
            bits = (bits << 8) | (data[position + i] & 0xFFL);
        }
        position += 8;
        return Double.longBitsToDouble(bits);
    }

    /** Un color RGBA; el cuarto byte no se usa. */
    ScoreColor readColor() {
        int red = readUnsignedByte();
        int green = readUnsignedByte();
        int blue = readUnsignedByte();
        readUnsignedByte();
        return new ScoreColor(red, green, blue);
    }

    /** La armadura: un byte con la cantidad de alteraciones y otro con el modo. */
    KeySignature readKeySignature() {
        int accidentals = readSignedByte();
        int modeByte = readUnsignedByte();
        return new KeySignature(accidentals, modeByte == 0 ? Mode.MAJOR : Mode.MINOR);
    }

    /**
     * "Byte-size string": un byte de longitud seguido de un bloque de tamano
     * fijo que contiene el texto y su relleno.
     */
    String readFixedString(int fixedLength) {
        int length = readUnsignedByte();
        require(fixedLength);
        String text = decode(position, Math.min(length, fixedLength));
        position += fixedLength;
        return text;
    }

    /** "Int-size string" sin byte de longitud extra: un entero y el texto. */
    String readIntPrefixedString() {
        int length = readInt();
        require(length);
        String text = decode(position, length);
        position += length;
        return text;
    }

    /**
     * "Int-size string" con byte de longitud redundante: un entero (largo mas
     * uno, por compatibilidad historica), un byte con el largo real, y el
     * texto. Es la que usa Guitar Pro para el encabezado, los marcadores y
     * las plantillas de pagina.
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
