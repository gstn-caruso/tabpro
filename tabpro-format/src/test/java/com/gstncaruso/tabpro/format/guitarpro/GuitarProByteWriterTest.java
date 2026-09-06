package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.ScoreColor;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import org.junit.jupiter.api.Test;

/**
 * Las primitivas de escritura son el espejo de {@link GuitarProByteReader}: lo que una
 * escribe, la otra lo tiene que leer igual.
 */
class GuitarProByteWriterTest {

    @Test
    void escribeYReleeEnterosLittleEndianConSigno() {
        GuitarProByteWriter writer = new GuitarProByteWriter().writeInt(-1).writeInt(305419896);
        GuitarProByteReader reader = new GuitarProByteReader(writer.bytes());

        assertEquals(-1, reader.readInt());
        assertEquals(305419896, reader.readInt());
    }

    @Test
    void escribeYReleeBytesConYSinSigno() {
        GuitarProByteWriter writer = new GuitarProByteWriter().writeUnsignedByte(200).writeSignedByte(-56);
        GuitarProByteReader reader = new GuitarProByteReader(writer.bytes());

        assertEquals(200, reader.readUnsignedByte());
        assertEquals(-56, reader.readSignedByte());
    }

    @Test
    void escribeYReleeUnBooleano() {
        GuitarProByteReader reader =
                new GuitarProByteReader(new GuitarProByteWriter().writeBoolean(true).writeBoolean(false).bytes());

        assertEquals(true, reader.readBoolean());
        assertEquals(false, reader.readBoolean());
    }

    @Test
    void escribeYReleeUnDoubleEnBigEndian() {
        GuitarProByteReader reader =
                new GuitarProByteReader(new GuitarProByteWriter().writeDoubleBigEndian(0.75).bytes());

        assertEquals(0.75, reader.readDoubleBigEndian());
    }

    @Test
    void escribeYReleeUnColorIgnorandoElCuartoByte() {
        ScoreColor color = new ScoreColor(10, 20, 30);
        GuitarProByteReader reader = new GuitarProByteReader(new GuitarProByteWriter().writeColor(color).bytes());

        assertEquals(color, reader.readColor());
    }

    @Test
    void escribeYReleeUnaArmadura() {
        KeySignature keySignature = new KeySignature(-3, Mode.MINOR);
        GuitarProByteReader reader =
                new GuitarProByteReader(new GuitarProByteWriter().writeKeySignature(keySignature).bytes());

        assertEquals(keySignature, reader.readKeySignature());
    }

    @Test
    void escribeYReleeUnStringDeTamanoFijoConSuRelleno() {
        GuitarProByteReader reader =
                new GuitarProByteReader(new GuitarProByteWriter().writeFixedString("Guitarra", 40).bytes());

        assertEquals("Guitarra", reader.readFixedString(40));
        assertEquals(41, reader.position());
    }

    /** Un nombre mas largo que el bloque no puede desbordarlo: se trunca. */
    @Test
    void unStringDeTamanoFijoMasLargoQueElBloqueSeTrunca() {
        String largo = "Un nombre de pista demasiado largo para entrar";
        GuitarProByteReader reader = new GuitarProByteReader(new GuitarProByteWriter().writeFixedString(largo, 10).bytes());

        assertEquals(largo.substring(0, 10), reader.readFixedString(10));
    }

    @Test
    void escribeYReleeUnStringConPrefijoEnteroSinByteExtra() {
        GuitarProByteReader reader =
                new GuitarProByteReader(new GuitarProByteWriter().writeIntPrefixedString("hola mundo").bytes());

        assertEquals("hola mundo", reader.readIntPrefixedString());
    }

    @Test
    void escribeYReleeUnStringConPrefijoEnteroYByteDeLargoRedundante() {
        GuitarProByteReader reader = new GuitarProByteReader(
                new GuitarProByteWriter().writeLengthPrefixedString("Cancion de prueba").bytes());

        assertEquals("Cancion de prueba", reader.readLengthPrefixedString());
    }

    @Test
    void escribeYReleeLaVersion() {
        GuitarProByteReader reader =
                new GuitarProByteReader(new GuitarProByteWriter().writeVersion("FICHIER GUITAR PRO v4.06").bytes());

        assertEquals(GuitarProVersion.GP4, GuitarProVersion.parse(reader.readFixedString(30)));
    }
}
