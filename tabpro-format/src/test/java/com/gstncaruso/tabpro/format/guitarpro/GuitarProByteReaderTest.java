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
    void leeEnterosLittleEndianConSigno() {
        GuitarProFileWriter writer = new GuitarProFileWriter().writeInt(-1).writeInt(305419896);
        GuitarProByteReader reader = new GuitarProByteReader(writer.bytes());

        assertEquals(-1, reader.readInt());
        assertEquals(305419896, reader.readInt());
    }

    @Test
    void leeBytesConYSinSigno() {
        GuitarProFileWriter writer = new GuitarProFileWriter().writeUnsignedByte(200).writeSignedByte(-56);
        GuitarProByteReader reader = new GuitarProByteReader(writer.bytes());

        assertEquals(200, reader.readUnsignedByte());
        assertEquals(-56, reader.readSignedByte());
    }

    @Test
    void leeShortsConSigno() {
        GuitarProByteReader reader = new GuitarProByteReader(new GuitarProFileWriter().writeShort(-2).bytes());

        assertEquals(-2, reader.readShort());
    }

    @Test
    void leeUnDoubleEnBigEndian() {
        GuitarProByteReader reader =
                new GuitarProByteReader(new GuitarProFileWriter().writeDoubleBigEndian(0.75).bytes());

        assertEquals(0.75, reader.readDoubleBigEndian());
    }

    @Test
    void leeUnColorIgnorandoElCuartoByte() {
        ScoreColor color = new ScoreColor(10, 20, 30);
        GuitarProByteReader reader = new GuitarProByteReader(new GuitarProFileWriter().writeColor(color).bytes());

        assertEquals(color, reader.readColor());
    }

    @Test
    void leeUnStringDeTamanoFijoConSuRelleno() {
        GuitarProByteReader reader =
                new GuitarProByteReader(new GuitarProFileWriter().writeFixedString("Guitarra", 40).bytes());

        assertEquals("Guitarra", reader.readFixedString(40));
        assertEquals(41, reader.position());
    }

    @Test
    void leeUnStringConPrefijoEnteroSinByteExtra() {
        GuitarProByteReader reader =
                new GuitarProByteReader(new GuitarProFileWriter().writeIntPrefixedString("hola mundo").bytes());

        assertEquals("hola mundo", reader.readIntPrefixedString());
    }

    @Test
    void leeUnStringConPrefijoEnteroYByteDeLargoRedundante() {
        GuitarProByteReader reader = new GuitarProByteReader(
                new GuitarProFileWriter().writeLengthPrefixedString("Cancion de prueba").bytes());

        assertEquals("Cancion de prueba", reader.readLengthPrefixedString());
    }

    @Test
    void saltaLaCantidadDeBytesPedida() {
        GuitarProByteReader reader =
                new GuitarProByteReader(new GuitarProFileWriter().writeInt(1).writeInt(2).bytes());

        reader.skip(4);

        assertEquals(2, reader.readInt());
    }

    @Test
    void informaSiQuedanBytesPorLeer() {
        GuitarProByteReader reader = new GuitarProByteReader(new GuitarProFileWriter().writeUnsignedByte(1).bytes());

        assertTrue(reader.hasMore());
        reader.readUnsignedByte();
        assertFalse(reader.hasMore());
    }

    @Test
    void unArchivoTruncadoFallaConMensajeClaro() {
        GuitarProByteReader reader = new GuitarProByteReader(new byte[] {1, 2});

        ScoreFileException exception = assertThrows(ScoreFileException.class, reader::readInt);

        assertTrue(exception.getMessage().contains("truncado"));
    }
}
