package com.gstncaruso.tabpro.ui.dialogs.measure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.gstncaruso.tabpro.core.model.bars.DirectionJump;
import com.gstncaruso.tabpro.core.model.bars.DirectionSymbol;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DirectionsPanelTest {

    @Test
    void startsWithNothingWhenTheMeasureHasNoDirections() {
        DirectionsPanel panel = new DirectionsPanel(Optional.empty(), Optional.empty());

        assertNull(panel.toSymbol());
        assertNull(panel.toJump());
    }

    @Test
    void startsWithTheGivenSymbolAndJump() {
        DirectionsPanel panel = new DirectionsPanel(Optional.of(DirectionSymbol.CODA), Optional.of(DirectionJump.DA_CAPO_AL_CODA));

        assertEquals(DirectionSymbol.CODA, panel.toSymbol());
        assertEquals(DirectionJump.DA_CAPO_AL_CODA, panel.toJump());
    }

    @Test
    void offersEveryDirectionSymbol() {
        for (DirectionSymbol symbol : DirectionSymbol.values()) {
            DirectionsPanel panel = new DirectionsPanel(Optional.of(symbol), Optional.empty());
            assertEquals(symbol, panel.toSymbol());
        }
    }

    @Test
    void offersAllFourteenJumps() {
        for (DirectionJump jump : DirectionJump.values()) {
            DirectionsPanel panel = new DirectionsPanel(Optional.empty(), Optional.of(jump));
            assertEquals(jump, panel.toJump());
        }
    }
}
