package com.gstncaruso.tabpro.format.tabledit;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Lee los tipos primitivos del formato binario de TablEdit (.tef, version 3)
 * sobre un arreglo de bytes: todo little endian. No sabe nada de partituras:
 * eso es responsabilidad de los lectores de cada seccion del archivo.
 */
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

    /** TablEdit guarda los shorts sin signo: el largo de los strings, el conteo de compases, etc. */
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

    /** Un bloque de tamano fijo, para envolverlo despues en su propio {@link TabEditByteReader}. */
    byte[] readBlock(int byteCount) {
        require(byteCount);
        byte[] block = Arrays.copyOfRange(data, position, position + byteCount);
        position += byteCount;
        return block;
    }

    /**
     * El string de los metadatos de la cancion: un short sin signo con el largo,
     * y esa cantidad de caracteres. Si aparece un byte nulo antes de terminar,
     * TablEdit corta ahi mismo y no consume el resto del campo declarado.
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

    /** Un string que termina en un byte nulo, como el nombre de una pista. */
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
