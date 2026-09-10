package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MixTableModelTest {

    @Test
    void everyTrackIsVisibleInTheMultitrackViewByDefault() {
        MixTableModel model = new MixTableModel();

        assertTrue(model.isVisibleInMultitrackView(0));
        assertTrue(model.isVisibleInMultitrackView(3));
    }

    @Test
    void togglingHidesATrackAndTogglingAgainBringsItBack() {
        MixTableModel model = new MixTableModel();

        model.toggleVisibleInMultitrackView(2);
        assertFalse(model.isVisibleInMultitrackView(2));
        assertTrue(model.isVisibleInMultitrackView(0), "the other tracks are not affected");

        model.toggleVisibleInMultitrackView(2);
        assertTrue(model.isVisibleInMultitrackView(2));
    }

    @Test
    void startsWithEveryParameterExpanded() {
        MixTableModel model = new MixTableModel();

        assertFalse(model.isReduced());
    }

    @Test
    void theTopButtonsReduceAndRestoreEveryParameterAtOnce() {
        MixTableModel model = new MixTableModel();

        model.reduceAllParameters();
        assertTrue(model.isReduced());

        model.restoreAllParameters();
        assertFalse(model.isReduced());
    }
}
