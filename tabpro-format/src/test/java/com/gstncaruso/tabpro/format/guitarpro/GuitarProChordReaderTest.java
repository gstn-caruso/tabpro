package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class GuitarProChordReaderTest {

    private static final int OLD_FORMAT = 0;

    private static final int STRING_MASK = 0x7C;

    private final GuitarProChordReader chords = new GuitarProChordReader();

    @Test
    void anOldChordBringsItsNameAndTheFretOfEveryString() {
        GuitarProByteReader reader = reading(new GuitarProFileWriter()
                .writeUnsignedByte(OLD_FORMAT)
                .writeLengthPrefixedString("C")
                .writeInt(1)
                .writeInt(0).writeInt(0).writeInt(1).writeInt(1).writeInt(2).writeInt(0)
                .writeUnsignedByte(STRING_MASK));

        ChordDiagram chord = chords.read(reader, GuitarProVersion.GP3, 6);

        assertEquals("C", chord.name());
        assertEquals(1, chord.baseFret());
        assertEquals(List.of(0, 0, 1, 1, 2, 0), chord.frets());
        assertEquals(STRING_MASK, reader.readUnsignedByte(), "the string mask stays intact");
    }

    @Test
    void theNameOfAnOldChordIsAsLongAsItSays() {
        GuitarProByteReader reader = reading(new GuitarProFileWriter()
                .writeUnsignedByte(OLD_FORMAT)
                .writeLengthPrefixedString("Cmaj7")
                .writeInt(1)
                .writeInt(0).writeInt(0).writeInt(0).writeInt(0).writeInt(0).writeInt(0)
                .writeUnsignedByte(STRING_MASK));

        ChordDiagram chord = chords.read(reader, GuitarProVersion.GP3, 6);

        assertEquals("Cmaj7", chord.name());
        assertEquals(STRING_MASK, reader.readUnsignedByte(), "the string mask stays intact");
    }

    @Test
    void anOldChordWithoutABaseFretHasNoFretsAtAll() {
        GuitarProByteReader reader = reading(new GuitarProFileWriter()
                .writeUnsignedByte(OLD_FORMAT)
                .writeLengthPrefixedString("C")
                .writeInt(0)
                .writeUnsignedByte(STRING_MASK));

        ChordDiagram chord = chords.read(reader, GuitarProVersion.GP3, 6);

        assertEquals("C", chord.name());
        assertEquals(Collections.nCopies(6, ChordDiagram.MUTED), chord.frets());
        assertEquals(STRING_MASK, reader.readUnsignedByte(), "the string mask stays intact");
    }

    @Test
    void aGp5FileStillWritesTheOldChordTheSameWay() {
        GuitarProByteReader reader = reading(new GuitarProFileWriter()
                .writeUnsignedByte(OLD_FORMAT)
                .writeLengthPrefixedString("Em")
                .writeInt(0)
                .writeUnsignedByte(STRING_MASK));

        ChordDiagram chord = chords.read(reader, GuitarProVersion.GP5_10, 6);

        assertEquals("Em", chord.name());
        assertEquals(STRING_MASK, reader.readUnsignedByte(), "the string mask stays intact");
    }

    private static GuitarProByteReader reading(GuitarProFileWriter written) {
        return new GuitarProByteReader(written.bytes());
    }
}
