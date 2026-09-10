package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.ScoreColor;
import org.junit.jupiter.api.Test;

class GuitarProByteReaderTest {

    @Test
    void readsSignedLittleEndianIntegers() {
        GuitarProFileWriter writer = new GuitarProFileWriter().writeInt(-1).writeInt(305419896);
        GuitarProByteReader reader = new GuitarProByteReader(writer.bytes());

        assertEquals(-1, reader.readInt());
        assertEquals(305419896, reader.readInt());
    }

    @Test
    void readsSignedAndUnsignedBytes() {
        GuitarProFileWriter writer = new GuitarProFileWriter().writeUnsignedByte(200).writeSignedByte(-56);
        GuitarProByteReader reader = new GuitarProByteReader(writer.bytes());

        assertEquals(200, reader.readUnsignedByte());
        assertEquals(-56, reader.readSignedByte());
    }

    @Test
    void readsSignedShorts() {
        GuitarProByteReader reader = new GuitarProByteReader(new GuitarProFileWriter().writeShort(-2).bytes());

        assertEquals(-2, reader.readShort());
    }

    @Test
    void readsADoubleInBigEndian() {
        GuitarProByteReader reader =
                new GuitarProByteReader(new GuitarProFileWriter().writeDoubleBigEndian(0.75).bytes());

        assertEquals(0.75, reader.readDoubleBigEndian());
    }

    @Test
    void readsAColorIgnoringTheFourthByte() {
        ScoreColor color = new ScoreColor(10, 20, 30);
        GuitarProByteReader reader = new GuitarProByteReader(new GuitarProFileWriter().writeColor(color).bytes());

        assertEquals(color, reader.readColor());
    }

    @Test
    void readsAFixedSizeStringWithItsPadding() {
        GuitarProByteReader reader =
                new GuitarProByteReader(new GuitarProFileWriter().writeFixedString("Guitar", 40).bytes());

        assertEquals("Guitar", reader.readFixedString(40));
        assertEquals(41, reader.position());
    }

    @Test
    void readsAnIntPrefixedStringWithoutAnExtraByte() {
        GuitarProByteReader reader =
                new GuitarProByteReader(new GuitarProFileWriter().writeIntPrefixedString("hello world").bytes());

        assertEquals("hello world", reader.readIntPrefixedString());
    }

    @Test
    void readsALengthPrefixedStringWithARedundantLengthByte() {
        GuitarProByteReader reader = new GuitarProByteReader(
                new GuitarProFileWriter().writeLengthPrefixedString("Test song").bytes());

        assertEquals("Test song", reader.readLengthPrefixedString());
    }

    @Test
    void skipsTheRequestedNumberOfBytes() {
        GuitarProByteReader reader =
                new GuitarProByteReader(new GuitarProFileWriter().writeInt(1).writeInt(2).bytes());

        reader.skip(4);

        assertEquals(2, reader.readInt());
    }

    @Test
    void reportsWhetherBytesAreLeftToRead() {
        GuitarProByteReader reader = new GuitarProByteReader(new GuitarProFileWriter().writeUnsignedByte(1).bytes());

        assertTrue(reader.hasMore());
        reader.readUnsignedByte();
        assertFalse(reader.hasMore());
    }

    @Test
    void aTruncatedFileFailsWithAClearMessage() {
        GuitarProByteReader reader = new GuitarProByteReader(new byte[] {1, 2});

        ScoreFileException exception = assertThrows(ScoreFileException.class, reader::readInt);

        assertTrue(exception.getMessage().contains("truncado"));
    }
}
