package com.gstncaruso.tabpro.format.powertab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.effects.BendType;
import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.model.effects.SlideType;
import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

/** Las notas se arman a mano, byte a byte, siguiendo el layout de note.cpp de powertabeditor. */
class PowerTabNoteReaderTest {

    private final PowerTabNoteReader reader = new PowerTabNoteReader();

    @Test
    void readsTheStringAndFret() {
        // cuerda 2 (0-based) = cuerda 4 en el modelo (1-based); traste 5.
        Note note = readNote(stringData(2, 5), 0);

        assertEquals(4, note.string());
        assertEquals(5, note.fret());
        assertTrue(note.effects().isEmpty());
    }

    @Test
    void readsATiedNote() {
        Note note = readNote(stringData(0, 0), 0x01);

        assertTrue(note.tied());
    }

    @Test
    void readsAGhostedHammerOn() {
        Note note = readNote(stringData(0, 0), 0x08 | 0x80);

        assertTrue(note.has(Ornament.GHOST));
        assertTrue(note.has(Ornament.HAMMER_ON_PULL_OFF));
    }

    @Test
    void readsANaturalHarmonicFromASimpleFlag() {
        Note note = readNote(stringData(0, 0), 0x40);

        assertEquals(java.util.Optional.of(HarmonicType.NATURAL), note.effects().harmonic());
    }

    @Test
    void readsAnArtificialHarmonicFromAComplexSymbol() {
        int symbol = ('h' << 24);
        Note note = readNote(stringData(0, 0), 0, symbol);

        assertEquals(java.util.Optional.of(HarmonicType.ARTIFICIAL), note.effects().harmonic());
    }

    @Test
    void readsATappedHarmonicFromAComplexSymbol() {
        int symbol = ('f' << 24) | 15;
        Note note = readNote(stringData(0, 0), 0, symbol);

        assertEquals(java.util.Optional.of(HarmonicType.TAPPED), note.effects().harmonic());
    }

    @Test
    void readsATrillFromAComplexSymbol() {
        int symbol = ('g' << 24) | (12 << 16);
        Note note = readNote(stringData(0, 0), 0, symbol);

        assertEquals(12, note.effects().trill().orElseThrow().fret());
    }

    @Test
    void readsABendFromAComplexSymbol() {
        // bendAndHold (2) con altura 3 en cuartos de tono.
        int bentPitch = 3;
        int symbol = ('e' << 24) | (2 << 20) | (bentPitch << 4);
        Note note = readNote(stringData(0, 0), 0, symbol);

        var bend = note.effects().bend().orElseThrow();
        assertEquals(BendType.BEND, bend.type());
        assertEquals(3, bend.peakQuarterTones());
    }

    @Test
    void slideOutOfWinsOverSlideInto() {
        int slideIntoFromBelow = 1;
        int slideOutLegato = 2;
        int symbol = ('d' << 24) | (slideIntoFromBelow << 16) | (slideOutLegato << 8);
        Note note = readNote(stringData(0, 0), 0, symbol);

        assertEquals(java.util.Optional.of(SlideType.LEGATO), note.effects().slide());
    }

    @Test
    void slideIntoAppliesWhenThereIsNoSlideOut() {
        int slideIntoFromAbove = 2;
        int symbol = ('d' << 24) | (slideIntoFromAbove << 16);
        Note note = readNote(stringData(0, 0), 0, symbol);

        assertEquals(java.util.Optional.of(SlideType.IN_FROM_ABOVE), note.effects().slide());
    }

    private Note readNote(int stringData, int simpleData, int... symbols) {
        byte[] data = bytesFor(stringData, simpleData, symbols);
        return reader.read(new PowerTabByteReader(data), 3);
    }

    private static int stringData(int string0Based, int fret) {
        return (string0Based << 5) | fret;
    }

    private static byte[] bytesFor(int stringData, int simpleData, int[] symbols) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(stringData);
        out.write(simpleData & 0xFF);
        out.write((simpleData >>> 8) & 0xFF);
        out.write(symbols.length);
        for (int symbol : symbols) {
            out.write(symbol & 0xFF);
            out.write((symbol >>> 8) & 0xFF);
            out.write((symbol >>> 16) & 0xFF);
            out.write((symbol >>> 24) & 0xFF);
        }
        return out.toByteArray();
    }
}
