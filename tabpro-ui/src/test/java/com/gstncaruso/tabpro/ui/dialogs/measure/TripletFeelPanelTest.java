package com.gstncaruso.tabpro.ui.dialogs.measure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.bars.TripletFeel;
import org.junit.jupiter.api.Test;

class TripletFeelPanelTest {

    @Test
    void startsWithTheGivenFeel() {
        TripletFeelPanel panel = new TripletFeelPanel(TripletFeel.EIGHTH);

        assertEquals(TripletFeel.EIGHTH, panel.toTripletFeel());
    }

    @Test
    void canBeSetBackToNone() {
        TripletFeelPanel panel = new TripletFeelPanel(TripletFeel.SIXTEENTH);

        panel.apply(TripletFeel.NONE);

        assertEquals(TripletFeel.NONE, panel.toTripletFeel());
    }
}
