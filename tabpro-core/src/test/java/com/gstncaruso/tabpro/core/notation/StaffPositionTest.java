package com.gstncaruso.tabpro.core.notation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Pitch;
import org.junit.jupiter.api.Test;

class StaffPositionTest {

    @Test
    void openLowEOnGuitarNeedsThreeLedgerLinesBelowTreble() {
        // Sonante E2 (MIDI 40) -> escrita E3 (MIDI 52, +12): octava=52/12-1=3, clase=4 -> E sin sostenido.
        // indiceDiatonico = 2 + 7*3 = 23; referencia TREBLE (E4) = 30; step = 23-30 = -7.
        StaffPosition position = StaffPosition.of(new Pitch(40), Clef.TREBLE);
        assertEquals(-7, position.step());
        assertFalse(position.sharp());
        assertEquals(3, position.ledgerLinesBelow());
        assertEquals(0, position.ledgerLinesAbove());
        assertFalse(position.isOnLine());
    }

    @Test
    void openHighEOnGuitarSitsOnTheTopSpaceOfTreble() {
        // Sonante E4 (MIDI 64) -> escrita E5 (MIDI 76, +12): octava=76/12-1=5, clase=4 -> E sin sostenido.
        // indiceDiatonico = 2 + 7*5 = 37; referencia TREBLE = 30; step = 37-30 = 7.
        // NOTA: el enunciado sugeria step==0 (linea inferior), pero aplicando la formula de los 5 pasos
        // tal cual esta definida el resultado es 7 (el espacio superior de la clave de sol), que ademas
        // coincide con la convencion real de guitarra (la 1a cuerda al aire se escribe en el espacio
        // superior). Se sigue la definicion, no el ejemplo.
        StaffPosition position = StaffPosition.of(new Pitch(64), Clef.TREBLE);
        assertEquals(7, position.step());
        assertFalse(position.sharp());
        assertFalse(position.isOnLine());
    }

    @Test
    void openBOnGuitarSitsOnTheMiddleLineOfTreble() {
        // Sonante B3 (MIDI 59) -> escrita B4 (MIDI 71, +12): octava=71/12-1=4, clase=11 -> B sin sostenido.
        // indiceDiatonico = 6 + 7*4 = 34; referencia TREBLE = 30; step = 34-30 = 4.
        // NOTA: el enunciado sugeria step==-3; aplicando la formula da 4 (la linea del medio de la clave
        // de sol), que coincide con la convencion real (la cuerda Si al aire va en la linea del medio).
        StaffPosition position = StaffPosition.of(new Pitch(59), Clef.TREBLE);
        assertEquals(4, position.step());
        assertTrue(position.isOnLine());
    }

    @Test
    void writtenFOnTopOfTrebleIsOnTheTopLine() {
        // Sonante F4 (MIDI 65) -> escrita F5 (MIDI 77, +12): octava=77/12-1=5, clase=5 -> F sin sostenido.
        // indiceDiatonico = 3 + 7*5 = 38; referencia TREBLE = 30; step = 38-30 = 8.
        // NOTA: el enunciado sugeria step==1 (primer espacio); aplicando la formula da 8 (la linea
        // superior de la clave de sol, ya que F5 es justamente esa linea).
        StaffPosition position = StaffPosition.of(new Pitch(65), Clef.TREBLE);
        assertEquals(8, position.step());
        assertTrue(position.isOnLine());
    }

    @Test
    void fretOneOnLowStringNeedsThreeLedgerLinesBelowTreble() {
        // Sonante F2 (MIDI 41) -> escrita F3 (MIDI 53, +12): octava=53/12-1=3, clase=5 -> F sin sostenido.
        // indiceDiatonico = 3 + 7*3 = 24; referencia TREBLE = 30; step = 24-30 = -6.
        StaffPosition position = StaffPosition.of(new Pitch(41), Clef.TREBLE);
        assertEquals(-6, position.step());
        assertFalse(position.sharp());
        assertEquals(3, position.ledgerLinesBelow());
    }

    @Test
    void aSharpKeepsTheSameStepAsItsNaturalWithSharpMarked() {
        // Sonante F#2 (MIDI 42) -> escrita F#3 (MIDI 54, +12): octava=54/12-1=3, clase=6 -> F sostenido.
        // Mismo step que F2 (-6), pero con sharp marcado.
        StaffPosition position = StaffPosition.of(new Pitch(42), Clef.TREBLE);
        assertEquals(-6, position.step());
        assertTrue(position.sharp());
    }

    @Test
    void openLowEOnBassNeedsOneLedgerLineBelow() {
        // Sonante E1 (MIDI 28) -> escrita E2 (MIDI 40, +12): octava=40/12-1=2, clase=4 -> E sin sostenido.
        // indiceDiatonico = 2 + 7*2 = 16; referencia BASS (G2) = 18; step = 16-18 = -2.
        StaffPosition position = StaffPosition.of(new Pitch(28), Clef.BASS);
        assertEquals(-2, position.step());
        assertEquals(1, position.ledgerLinesBelow());
    }

    @Test
    void openGOnBassSitsOnTheTopSpace() {
        // Sonante G2 (MIDI 43) -> escrita G3 (MIDI 55, +12): octava=55/12-1=3, clase=7 -> G sin sostenido.
        // indiceDiatonico = 4 + 7*3 = 25; referencia BASS = 18; step = 25-18 = 7.
        // NOTA: el enunciado sugeria step==5; aplicando la formula da 7 (el espacio superior de la clave
        // de fa), que coincide con la convencion real del bajo de 4 cuerdas (la cuerda Sol al aire va en
        // el espacio superior).
        StaffPosition position = StaffPosition.of(new Pitch(43), Clef.BASS);
        assertEquals(7, position.step());
    }

    @Test
    void aVeryHighNoteNeedsLedgerLinesAboveTreble() {
        // Sonante E6 (MIDI 88) -> escrita E7 (MIDI 100, +12): octava=100/12-1=7, clase=4 -> E sin sostenido.
        // indiceDiatonico = 2 + 7*7 = 51; referencia TREBLE = 30; step = 51-30 = 21.
        // NOTA: el enunciado sugeria step==14 y 3 lineas adicionales arriba; aplicando la formula tal
        // cual esta definida (paso 1: escrita = sonante+12) da step=21 y, por lo tanto,
        // ledgerLinesAbove = (21-8)/2 = 6.
        StaffPosition position = StaffPosition.of(new Pitch(88), Clef.TREBLE);
        assertEquals(21, position.step());
        assertEquals(6, position.ledgerLinesAbove());
    }
}
