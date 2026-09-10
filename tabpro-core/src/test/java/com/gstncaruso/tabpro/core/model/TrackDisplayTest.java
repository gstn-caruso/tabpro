package com.gstncaruso.tabpro.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class TrackDisplayTest {

    @Test
    void standardDisplayKeepsTheDiagramAboveTheStaff() {
        assertFalse(TrackDisplay.standard().diagramsBelowStandardNotation());
    }

    @Test
    void diagramsBelowStandardNotationRoundTripsAndKeepsTheRest() {
        TrackDisplay original = TrackDisplay.standard();

        TrackDisplay updated = original.withDiagramsBelowStandardNotation(true);

        assertEquals(true, updated.diagramsBelowStandardNotation());
        assertEquals(original.standardNotation(), updated.standardNotation());
        assertEquals(original.tablature(), updated.tablature());
        assertEquals(original.tuningLegend(), updated.tuningLegend());
        assertEquals(original.rhythmOnTablature(), updated.rhythmOnTablature());
        assertEquals(original.diagrams(), updated.diagrams());
    }

    @Test
    void forceHorizontalBeamsRoundTripsAndKeepsTheRest() {
        TrackDisplay original = TrackDisplay.standard();

        TrackDisplay updated = original.withForceHorizontalBeams(true);

        assertEquals(true, updated.forceHorizontalBeams());
        assertEquals(original.standardNotation(), updated.standardNotation());
        assertEquals(original.tablature(), updated.tablature());
        assertEquals(original.diagramsBelowStandardNotation(), updated.diagramsBelowStandardNotation());
    }
}
