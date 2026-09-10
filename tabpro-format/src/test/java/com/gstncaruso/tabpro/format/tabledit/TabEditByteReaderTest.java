package com.gstncaruso.tabpro.format.tabledit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import org.junit.jupiter.api.Test;

class TabEditByteReaderTest {

    @Test
    void readsSignedAndUnsignedBytes() {
        TabEditByteReader reader =
                new TabEditByteReader(new TabEditFileWriter().writeUnsignedByte(200).writeSignedByte(-56).bytes());

        assertEquals(200, reader.readUnsignedByte());
        assertEquals(-56, reader.readSignedByte());
    }

    @Test
    void readsAnUnsignedLittleEndianShort() {
        TabEditByteReader reader = new TabEditByteReader(new TabEditFileWriter().writeShort(0xFFEE).bytes());

        assertEquals(0xFFEE, reader.readUnsignedShort());
    }

    @Test
    void readsASignedLittleEndianInt() {
        TabEditByteReader reader = new TabEditByteReader(new TabEditFileWriter().writeInt(-1).writeInt(305419896).bytes());

        assertEquals(-1, reader.readInt());
        assertEquals(305419896, reader.readInt());
    }

    @Test
    void skipsTheRequestedNumberOfBytes() {
        TabEditByteReader reader = new TabEditByteReader(new TabEditFileWriter().writeInt(1).writeInt(2).bytes());

        reader.skip(4);

        assertEquals(2, reader.readInt());
    }

    @Test
    void readsAFixedSizeBlockAsASubArray() {
        TabEditByteReader reader =
                new TabEditByteReader(new TabEditFileWriter().writeUnsignedByte(1).writeUnsignedByte(2).writeUnsignedByte(3).bytes());

        byte[] block = reader.readBlock(2);

        assertArrayEquals(new byte[] {1, 2}, block);
        assertEquals(3, reader.readUnsignedByte());
    }

    @Test
    void readsAShortLengthPrefixedString() {
        TabEditByteReader reader = new TabEditByteReader(new TabEditFileWriter().writeShortString("Song").bytes());

        assertEquals("Song", reader.readShortString());
    }

    @Test
    void aShortLengthPrefixedStringIsCutAtTheFirstNull() {
        TabEditFileWriter writer = new TabEditFileWriter().writeShort(10);
        writer.writeUnsignedByte('h').writeUnsignedByte('e').writeUnsignedByte('y').writeUnsignedByte('!');
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(99);
        TabEditByteReader reader = new TabEditByteReader(writer.bytes());

        assertEquals("hey!", reader.readShortString());
        assertEquals(99, reader.readUnsignedByte());
    }

    @Test
    void readsANullTerminatedStringWithinAMaximum() {
        TabEditFileWriter writer = new TabEditFileWriter();
        writer.writeUnsignedByte('h').writeUnsignedByte('i').writeUnsignedByte(0).writeUnsignedByte(7);
        TabEditByteReader reader = new TabEditByteReader(writer.bytes());

        assertEquals("hi", reader.readNullTerminatedString(256));
        assertEquals(7, reader.readUnsignedByte());
    }

    @Test
    void reportsWhetherBytesAreLeftToRead() {
        TabEditByteReader reader = new TabEditByteReader(new TabEditFileWriter().writeUnsignedByte(1).bytes());

        assertTrue(reader.hasMore());
        reader.readUnsignedByte();
        assertFalse(reader.hasMore());
    }

    @Test
    void reportsHowManyBytesAreLeftToRead() {
        TabEditByteReader reader = new TabEditByteReader(new TabEditFileWriter().writeInt(1).writeInt(2).bytes());

        assertEquals(8, reader.remaining());
        reader.readInt();
        assertEquals(4, reader.remaining());
    }

    @Test
    void aTruncatedFileFailsWithAClearMessage() {
        TabEditByteReader reader = new TabEditByteReader(new byte[] {1, 2});

        ScoreFileException exception = assertThrows(ScoreFileException.class, reader::readInt);

        assertTrue(exception.getMessage().contains("truncado"));
    }
}
