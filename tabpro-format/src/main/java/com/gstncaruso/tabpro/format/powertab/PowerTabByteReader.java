package com.gstncaruso.tabpro.format.powertab;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import java.nio.charset.StandardCharsets;

/**
 * Lee los tipos primitivos del formato binario de PowerTab sobre un arreglo de
 * bytes: enteros little endian, el "MFC string" de largo variable, el conteo
 * de vector al estilo MFC, y la etiqueta de clase que el archivador de MFC
 * antepone a cada objeto guardado en un vector. No sabe nada de partituras:
 * eso es responsabilidad de los lectores de cada seccion del archivo.
 */
final class PowerTabByteReader {

    /** El archivador de MFC anuncia una clase nueva con este valor de 16 bits. */
    private static final int NEW_CLASS_TAG = 0xffff;

    /** Un objeto grande usa un identificador de 32 bits en vez de uno de 16. */
    private static final int BIG_OBJECT_TAG = 0x7fff;

    /** Bit que en el word de 16 bits marca que el tag corresponde a una clase. */
    private static final int CLASS_TAG = 0x8000;

    /** El mismo bit, ya corrido a su lugar dentro del tag de 32 bits. */
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

    /** Un entero de 16 bits sin signo, little endian. */
    int readUnsignedShort() {
        require(2);
        int value = (data[position] & 0xFF) | ((data[position + 1] & 0xFF) << 8);
        position += 2;
        return value;
    }

    /** Un entero de 32 bits, little endian. El bit mas alto se trata a mano donde hace falta. */
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
     * El conteo de un vector al estilo MFC: un entero de 16 bits, y si vale
     * 0xffff (desborda) uno de 32 bits a continuacion.
     */
    int readCount() {
        int wordCount = readUnsignedShort();
        if (wordCount != 0xffff) {
            return wordCount;
        }
        int dwordCount = readInt();
        if (dwordCount < 0) {
            throw new ScoreFileException("archivo PowerTab corrupto: conteo de vector negativo");
        }
        return dwordCount;
    }

    /**
     * "MFC string": un largo de tamano variable (1, 2 o 4 bytes segun haga
     * falta) seguido del texto en ISO 8859-1.
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
     * La etiqueta de clase que MFC antepone a cada objeto de un vector: la
     * primera vez que aparece una clase trae su esquema y su nombre: las
     * veces siguientes es solo una referencia corta. No tiene significado
     * musical, pero hay que consumirla igual para no perder la sincronia del
     * archivo.
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
            readUnsignedShort(); // esquema de la clase, no hace falta.
            int nameLength = readUnsignedShort();
            skip(nameLength);
        }
    }

    /**
     * Un vector "chico" de bytes sin signo: un byte de tamano y esa cantidad
     * de valores.
     */
    int[] readSmallVectorOfUnsignedBytes() {
        int size = readUnsignedByte();
        int[] values = new int[size];
        for (int i = 0; i < size; i++) {
            values[i] = readUnsignedByte();
        }
        return values;
    }

    /**
     * Un arreglo fijo de enteros de 32 bits, con la misma codificacion que un
     * vector chico: un byte de tamano y esa cantidad de valores, sin llenar
     * el resto del arreglo (se completa en cero, como en C++).
     */
    int[] readSmallFixedArrayOfInts(int capacity) {
        int size = readUnsignedByte();
        if (size > capacity) {
            throw new ScoreFileException(
                    "archivo PowerTab corrupto: un arreglo de simbolos trae " + size
                            + " elementos pero el maximo es " + capacity);
        }
        int[] values = new int[capacity];
        for (int i = 0; i < size; i++) {
            values[i] = readInt();
        }
        return values;
    }

    private void require(int byteCount) {
        if (byteCount < 0 || position + byteCount > data.length) {
            throw new ScoreFileException(
                    "archivo PowerTab truncado: se esperaban " + byteCount
                            + " bytes en la posicion " + position
                            + " pero solo quedan " + (data.length - position));
        }
    }
}
