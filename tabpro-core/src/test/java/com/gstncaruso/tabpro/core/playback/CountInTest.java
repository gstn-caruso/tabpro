package com.gstncaruso.tabpro.core.playback;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import org.junit.jupiter.api.Test;

class CountInTest {

    @Test
    void whenOffAddsNoTime() {
        assertEquals(0, CountIn.off().leadInTicks(TimeSignature.fourFour()));
    }

    @Test
    void whenOnLastsAWholeBar() {
        assertEquals(TimeSignature.fourFour().ticksPerMeasure(), CountIn.on().leadInTicks(TimeSignature.fourFour()));
    }

    @Test
    void respectsTheBarTimeSignature() {
        TimeSignature threeFour = new TimeSignature(3, 4);
        assertEquals(threeFour.ticksPerMeasure(), CountIn.on().leadInTicks(threeFour));
    }
}
