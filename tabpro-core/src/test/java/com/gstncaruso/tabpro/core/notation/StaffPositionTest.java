package com.gstncaruso.tabpro.core.notation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Pitch;
import org.junit.jupiter.api.Test;

class StaffPositionTest {

    @Test
    void openLowEOnGuitarNeedsThreeLedgerLinesBelowTreble() {
        StaffPosition position = StaffPosition.of(new Pitch(40), Clef.TREBLE);
        assertEquals(-7, position.step());
        assertFalse(position.sharp());
        assertEquals(3, position.ledgerLinesBelow());
        assertEquals(0, position.ledgerLinesAbove());
        assertFalse(position.isOnLine());
    }

    @Test
    void openHighEOnGuitarSitsOnTheTopSpaceOfTreble() {
        StaffPosition position = StaffPosition.of(new Pitch(64), Clef.TREBLE);
        assertEquals(7, position.step());
        assertFalse(position.sharp());
        assertFalse(position.isOnLine());
    }

    @Test
    void openBOnGuitarSitsOnTheMiddleLineOfTreble() {
        StaffPosition position = StaffPosition.of(new Pitch(59), Clef.TREBLE);
        assertEquals(4, position.step());
        assertTrue(position.isOnLine());
    }

    @Test
    void writtenFOnTopOfTrebleIsOnTheTopLine() {
        StaffPosition position = StaffPosition.of(new Pitch(65), Clef.TREBLE);
        assertEquals(8, position.step());
        assertTrue(position.isOnLine());
    }

    @Test
    void fretOneOnLowStringNeedsThreeLedgerLinesBelowTreble() {
        StaffPosition position = StaffPosition.of(new Pitch(41), Clef.TREBLE);
        assertEquals(-6, position.step());
        assertFalse(position.sharp());
        assertEquals(3, position.ledgerLinesBelow());
    }

    @Test
    void aSharpKeepsTheSameStepAsItsNaturalWithSharpMarked() {
        StaffPosition position = StaffPosition.of(new Pitch(42), Clef.TREBLE);
        assertEquals(-6, position.step());
        assertTrue(position.sharp());
    }

    @Test
    void openLowEOnBassNeedsOneLedgerLineBelow() {
        StaffPosition position = StaffPosition.of(new Pitch(28), Clef.BASS);
        assertEquals(-2, position.step());
        assertEquals(1, position.ledgerLinesBelow());
    }

    @Test
    void openGOnBassSitsOnTheTopSpace() {
        StaffPosition position = StaffPosition.of(new Pitch(43), Clef.BASS);
        assertEquals(7, position.step());
    }

    @Test
    void aVeryHighNoteNeedsLedgerLinesAboveTreble() {
        StaffPosition position = StaffPosition.of(new Pitch(88), Clef.TREBLE);
        assertEquals(21, position.step());
        assertEquals(6, position.ledgerLinesAbove());
    }

    @Test
    void shiftingBySevenStepsMovesOneOctaveOnTheStaffWithoutChangingTheSharpFlag() {
        StaffPosition position = StaffPosition.of(new Pitch(42), Clef.TREBLE);

        StaffPosition shiftedDown = position.shiftedBySteps(-7);
        StaffPosition shiftedUp = position.shiftedBySteps(7);

        assertEquals(position.step() - 7, shiftedDown.step());
        assertEquals(position.step() + 7, shiftedUp.step());
        assertEquals(position.sharp(), shiftedDown.sharp());
        assertEquals(position.sharp(), shiftedUp.sharp());
    }

    @Test
    void shiftingByZeroStepsLeavesThePositionExactlyAsItWas() {
        StaffPosition position = StaffPosition.of(new Pitch(64), Clef.TREBLE);

        assertEquals(position, position.shiftedBySteps(0));
    }

    @Test
    void shiftingUpAndThenDownByTheSameAmountReturnsToTheOriginalPosition() {
        StaffPosition position = StaffPosition.of(new Pitch(59), Clef.BASS);

        assertEquals(position, position.shiftedBySteps(14).shiftedBySteps(-14));
    }
}
