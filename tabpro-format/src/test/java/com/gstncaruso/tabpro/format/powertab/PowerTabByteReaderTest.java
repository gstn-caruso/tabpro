package com.gstncaruso.tabpro.format.powertab;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import org.junit.jupiter.api.Test;

class PowerTabByteReaderTest {

    @Test
    void readsUnsignedBytesAndShorts() {
        PowerTabByteReader reader = new PowerTabByteReader(new byte[] {(byte) 0xFF, 0x01, 0x02});

        assertEquals(255, reader.readUnsignedByte());
        assertEquals(0x0201, reader.readUnsignedShort());
    }

    @Test
    void readsALittleEndianInt() {
        PowerTabByteReader reader = new PowerTabByteReader(new byte[] {0x04, 0x00, 0x00, 0x00});

        assertEquals(4, reader.readInt());
    }

    @Test
    void readsAShortMfcString() {
        byte[] data = {0x05, 'H', 'o', 'l', 'a', '!'};

        assertEquals("Hola!", new PowerTabByteReader(data).readMfcString());
    }

    @Test
    void anEmptyMfcStringHasNoBytesAfterTheLength() {
        assertEquals("", new PowerTabByteReader(new byte[] {0x00}).readMfcString());
    }

    @Test
    void readsACountThatFitsInAWord() {
        PowerTabByteReader reader = new PowerTabByteReader(new byte[] {0x02, 0x00});

        assertEquals(2, reader.readCount());
    }

    @Test
    void readsACountThatOverflowsToADword() {
        byte[] data = {(byte) 0xFF, (byte) 0xFF, 0x00, 0x00, 0x01, 0x00};

        assertEquals(0x10000, new PowerTabByteReader(data).readCount());
    }

    @Test
    void readsASmallVectorOfUnsignedBytes() {
        PowerTabByteReader reader = new PowerTabByteReader(new byte[] {0x03, 10, 20, 30});

        assertArrayEquals(new int[] {10, 20, 30}, reader.readSmallVectorOfUnsignedBytes());
    }

    @Test
    void aSmallFixedArrayLeavesUnusedSlotsInZero() {
        byte[] data = {0x01, 0x2A, 0x00, 0x00, 0x00};

        assertArrayEquals(new int[] {42, 0}, new PowerTabByteReader(data).readSmallFixedArrayOfInts(2));
    }

    @Test
    void aSmallFixedArrayLargerThanItsCapacityIsReported() {
        byte[] data = {0x03, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        assertThrows(ScoreFileException.class,
                () -> new PowerTabByteReader(data).readSmallFixedArrayOfInts(2));
    }

    /** Una clase nueva trae 0xffff, el esquema, el largo del nombre y el nombre. */
    @Test
    void skipsANewClassTag() {
        byte[] data = {
            (byte) 0xFF, (byte) 0xFF, // NEW_CLASS_TAG
            0x01, 0x00, // esquema
            0x03, 0x00, // largo del nombre
            'F', 'o', 'o', // nombre
            0x42, // el primer byte del objeto en si
        };
        PowerTabByteReader reader = new PowerTabByteReader(data);

        reader.readClassInformation();

        assertEquals(0x42, reader.readUnsignedByte());
    }

    /** Una referencia corta a una clase ya vista no trae nada mas: solo el word. */
    @Test
    void skipsAPlainObjectTagWithNoClassInformation() {
        byte[] data = {0x00, 0x00, 0x42};
        PowerTabByteReader reader = new PowerTabByteReader(data);

        reader.readClassInformation();

        assertEquals(0x42, reader.readUnsignedByte());
    }

    @Test
    void aTruncatedReadIsReported() {
        assertThrows(ScoreFileException.class, () -> new PowerTabByteReader(new byte[] {0x01}).readInt());
    }
}
