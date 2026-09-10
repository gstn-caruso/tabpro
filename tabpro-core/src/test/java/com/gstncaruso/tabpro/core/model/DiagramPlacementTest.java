package com.gstncaruso.tabpro.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DiagramPlacementTest {

    @Test
    void neitherFlagHidesTheDiagram() {
        assertEquals(DiagramPlacement.HIDDEN, DiagramPlacement.of(false, false));
    }

    @Test
    void onlyOnTheScoreShowsAboveTheStaff() {
        assertEquals(DiagramPlacement.ABOVE_THE_STAFF, DiagramPlacement.of(true, false));
    }

    @Test
    void onlyUnderTheTitleShowsThereOnly() {
        assertEquals(DiagramPlacement.UNDER_THE_TITLE, DiagramPlacement.of(false, true));
    }

    @Test
    void bothFlagsShowInBothPlaces() {
        assertEquals(DiagramPlacement.BOTH, DiagramPlacement.of(true, true));
    }
}
