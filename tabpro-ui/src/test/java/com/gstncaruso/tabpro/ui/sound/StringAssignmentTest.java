package com.gstncaruso.tabpro.ui.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class StringAssignmentTest {

    private static final int SIX_STRINGS = 6;

    @Test
    void theFirstChannelCanBeTheHighestString() {
        StringAssignment assignment = StringAssignment.FIRST_CHANNEL_IS_THE_HIGHEST_STRING;

        assertEquals(OptionalInt.of(1), assignment.stringFor(0, SIX_STRINGS));
        assertEquals(OptionalInt.of(6), assignment.stringFor(5, SIX_STRINGS));
    }

    @Test
    void theFirstChannelCanBeTheLowestString() {
        StringAssignment assignment = StringAssignment.FIRST_CHANNEL_IS_THE_LOWEST_STRING;

        assertEquals(OptionalInt.of(6), assignment.stringFor(0, SIX_STRINGS));
        assertEquals(OptionalInt.of(1), assignment.stringFor(5, SIX_STRINGS));
    }

    @Test
    void aChannelBeyondTheStringsChoosesNothing() {
        assertTrue(StringAssignment.FIRST_CHANNEL_IS_THE_HIGHEST_STRING.stringFor(9, SIX_STRINGS).isEmpty());
        assertTrue(StringAssignment.FIRST_CHANNEL_IS_THE_LOWEST_STRING.stringFor(9, SIX_STRINGS).isEmpty());
    }

    @Test
    void withoutChannelDetectionTheStringComesFromThePitch() {
        assertTrue(StringAssignment.NO_CHANNEL_DETECTION.stringFor(0, SIX_STRINGS).isEmpty());
    }
}
