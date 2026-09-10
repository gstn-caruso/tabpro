package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StaffToTabGapTest {

    @Test
    void theGapIsFourAndAHalfStaffLineSpacingsAsInGuitarPro5() {
        assertEquals(36, ScoreLayout.STAFF_TO_TAB_GAP);
    }
}
