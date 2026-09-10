package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.bars.DirectionJump;
import com.gstncaruso.tabpro.core.model.bars.DirectionSymbol;
import java.util.List;
import org.junit.jupiter.api.Test;

class GuitarProDirectionSlotsTest {

    @Test
    void theFiveSymbolsAreReadInTheOrderTheFormatDemands() {
        assertEquals(
                List.of(
                        DirectionSymbol.CODA,
                        DirectionSymbol.DOUBLE_CODA,
                        DirectionSymbol.SEGNO,
                        DirectionSymbol.SEGNO_SEGNO,
                        DirectionSymbol.FINE),
                GuitarProHeaderReader.SYMBOL_SLOTS,
                "The Guitar Pro 5 format fixes this order for the target symbols. "
                        + "Changing it makes .gp5 files read wrong in silence: the file opens, "
                        + "but each symbol lands on the wrong measure.");
    }

    @Test
    void theFourteenJumpsAreReadInTheOrderTheFormatDemands() {
        assertEquals(
                List.of(
                        DirectionJump.DA_CAPO,
                        DirectionJump.DA_CAPO_AL_CODA,
                        DirectionJump.DA_CAPO_AL_DOUBLE_CODA,
                        DirectionJump.DA_CAPO_AL_FINE,
                        DirectionJump.DA_SEGNO,
                        DirectionJump.DA_SEGNO_AL_CODA,
                        DirectionJump.DA_SEGNO_AL_DOUBLE_CODA,
                        DirectionJump.DA_SEGNO_AL_FINE,
                        DirectionJump.DA_SEGNO_SEGNO,
                        DirectionJump.DA_SEGNO_SEGNO_AL_CODA,
                        DirectionJump.DA_SEGNO_SEGNO_AL_DOUBLE_CODA,
                        DirectionJump.DA_SEGNO_SEGNO_AL_FINE,
                        DirectionJump.DA_CODA,
                        DirectionJump.DA_DOUBLE_CODA),
                GuitarProHeaderReader.JUMP_SLOTS,
                "The Guitar Pro 5 format fixes this order for the jumps. "
                        + "Changing it makes .gp5 files read wrong in silence.");
    }

    @Test
    void thereAreNineteenSlotsInTotal() {
        assertEquals(
                19,
                GuitarProHeaderReader.SYMBOL_SLOTS.size() + GuitarProHeaderReader.JUMP_SLOTS.size(),
                "The file block has nineteen two-byte slots. "
                        + "Reading fewer shifts everything that comes after.");
    }

    @Test
    void eachDirectionInTheModelHasItsSlot() {
        assertEquals(
                DirectionSymbol.values().length,
                GuitarProHeaderReader.SYMBOL_SLOTS.size(),
                "If the model adds a symbol, we have to decide which slot of the file it lands on.");
        assertEquals(
                DirectionJump.values().length,
                GuitarProHeaderReader.JUMP_SLOTS.size(),
                "If the model adds a jump, we have to decide which slot of the file it lands on.");
    }
}
