package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * El formato viejo del diagrama de acorde: el unico que tenia GP3, y que GP4 y GP5
 * siguen escribiendo cuando el acorde no usa nada de lo que agregaron. Los bytes de
 * cada caso son los que graba Guitar Pro, tomados de archivos reales.
 */
class GuitarProChordReaderTest {

    /** El primer byte del diagrama elige entre el formato viejo y el de GP4. */
    private static final int OLD_FORMAT = 0;

    /** La mascara de cuerdas del beat, que viene justo detras del diagrama. */
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
        assertEquals(STRING_MASK, reader.readUnsignedByte(), "la mascara de cuerdas queda intacta");
    }

    /**
     * El nombre es un "int-size string": un entero con el largo del bloque y despues el
     * bloque, no un campo de tamano fijo. Leerlo como fijo se come veinte bytes de lo
     * que sigue.
     */
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
        assertEquals(STRING_MASK, reader.readUnsignedByte(), "la mascara de cuerdas queda intacta");
    }

    /**
     * Con la cejilla base en cero el acorde es solo un nombre: Guitar Pro no escribe ni
     * un traste detras. Si el lector los lee igual, se lleva puestos veinticuatro bytes.
     */
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
        assertEquals(STRING_MASK, reader.readUnsignedByte(), "la mascara de cuerdas queda intacta");
    }

    /** El formato viejo sigue siendo el mismo cuando lo escribe un archivo de GP5. */
    @Test
    void aGp5FileStillWritesTheOldChordTheSameWay() {
        GuitarProByteReader reader = reading(new GuitarProFileWriter()
                .writeUnsignedByte(OLD_FORMAT)
                .writeLengthPrefixedString("Em")
                .writeInt(0)
                .writeUnsignedByte(STRING_MASK));

        ChordDiagram chord = chords.read(reader, GuitarProVersion.GP5_10, 6);

        assertEquals("Em", chord.name());
        assertEquals(STRING_MASK, reader.readUnsignedByte(), "la mascara de cuerdas queda intacta");
    }

    private static GuitarProByteReader reading(GuitarProFileWriter written) {
        return new GuitarProByteReader(written.bytes());
    }
}
