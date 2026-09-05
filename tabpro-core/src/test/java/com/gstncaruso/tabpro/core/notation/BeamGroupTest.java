package com.gstncaruso.tabpro.core.notation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BeamGroupTest {

    @Test
    void aSingleBeatGroupIsSingle() {
        BeamGroup group = new BeamGroup(2, 2);
        assertTrue(group.isSingle());
        assertEquals(1, group.size());
    }

    @Test
    void aMultiBeatGroupIsNotSingle() {
        BeamGroup group = new BeamGroup(2, 4);
        assertFalse(group.isSingle());
        assertEquals(3, group.size());
    }

    @Test
    void containsChecksTheInclusiveRange() {
        BeamGroup group = new BeamGroup(2, 4);
        assertFalse(group.contains(1));
        assertTrue(group.contains(2));
        assertTrue(group.contains(3));
        assertTrue(group.contains(4));
        assertFalse(group.contains(5));
    }

    @Test
    void rejectsALastBeatBeforeTheFirstBeat() {
        assertThrows(IllegalArgumentException.class, () -> new BeamGroup(3, 2));
    }

    @Test
    void rejectsANegativeFirstBeat() {
        assertThrows(IllegalArgumentException.class, () -> new BeamGroup(-1, 2));
    }
}
