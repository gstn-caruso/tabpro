package com.gstncaruso.tabpro.format.powertab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.NoteValue;
import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

/** Las posiciones se arman a mano, byte a byte, siguiendo el layout de position.cpp de powertabeditor. */
class PowerTabPositionReaderTest {

    private static final int FLAG_DOTTED = 0x01;
    private static final int FLAG_DOUBLE_DOTTED = 0x02;
    private static final int FLAG_REST = 0x04;

    private final PowerTabPositionReader reader = new PowerTabPositionReader();

    @Test
    void readsARest() {
        PowerTabPosition position = read(5, durationData(4, FLAG_REST), new int[0], 0);

        assertEquals(5, position.index());
        assertEquals(NoteValue.QUARTER, position.beat().duration().value());
        assertTrue(position.beat().isRest());
        assertFalse(position.hasMultibarRest());
    }

    @Test
    void readsADottedNoteWithOneNote() {
        PowerTabPosition position = read(0, durationData(8, FLAG_DOTTED), new int[0], 1);

        assertEquals(NoteValue.EIGHTH, position.beat().duration().value());
        assertTrue(position.beat().duration().dotted());
        assertFalse(position.beat().isRest());
        assertEquals(1, position.beat().notes().size());
    }

    /** El modelo de tabpro no distingue doble puntillo: se aproxima a uno solo. */
    @Test
    void aDoubleDottedNoteIsApproximatedAsDotted() {
        PowerTabPosition position = read(0, durationData(2, FLAG_DOUBLE_DOTTED), new int[0], 0);

        assertEquals(NoteValue.HALF, position.beat().duration().value());
        assertTrue(position.beat().duration().dotted());
    }

    @Test
    void reportsAMultibarRest() {
        int symbol = ('j' << 24) | 3;
        PowerTabPosition position = read(0, durationData(1, FLAG_REST), new int[] {symbol}, 0);

        assertTrue(position.hasMultibarRest());
        assertEquals(3, position.multibarRestMeasureCount());
    }

    private static int durationData(int durationType, int flags) {
        return (durationType << 24) | flags;
    }

    private PowerTabPosition read(int index, int data, int[] symbols, int noteCount) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(index);
        out.write(0);
        out.write(0); // beaming, sin uso.
        writeInt(out, data);
        out.write(symbols.length);
        for (int symbol : symbols) {
            writeInt(out, symbol);
        }
        // el conteo de notas, al estilo MFC (word).
        out.write(noteCount & 0xFF);
        out.write((noteCount >>> 8) & 0xFF);
        for (int i = 0; i < noteCount; i++) {
            out.write(0x00); // sin tag de clase (referencia corta).
            out.write(0x00);
            writeNote(out, i, 2);
        }
        return reader.read(new PowerTabByteReader(out.toByteArray()));
    }

    private static void writeNote(ByteArrayOutputStream out, int string0Based, int fret) {
        out.write((string0Based << 5) | fret);
        out.write(0x00);
        out.write(0x00);
        out.write(0x00); // sin simbolos complejos.
    }

    private static void writeInt(ByteArrayOutputStream out, int value) {
        out.write(value & 0xFF);
        out.write((value >>> 8) & 0xFF);
        out.write((value >>> 16) & 0xFF);
        out.write((value >>> 24) & 0xFF);
    }
}
