package com.gstncaruso.tabpro.ui.dialogs.markers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Score;
import org.junit.jupiter.api.Test;

class MarkerTableModelTest {

    @Test
    void isEmptyForAFreshScore() {
        MarkerTableModel model = new MarkerTableModel(Score.blank());

        assertEquals(0, model.getRowCount());
    }
}
