package com.gstncaruso.tabpro.ui.dialogs.markers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.ScoreColor;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import org.junit.jupiter.api.Test;

class MarkerPanelTest {

    @Test
    void startsWithTheGivenMarker() {
        Marker marker = new Marker("Solo", ScoreColor.rgb(0x00FF00));

        MarkerPanel panel = new MarkerPanel(marker);

        assertEquals(marker, panel.toMarker());
    }

    @Test
    void reflectsWhateverIsLoadedAfterwards() {
        MarkerPanel panel = new MarkerPanel(Marker.named("A"));

        panel.apply(new Marker("B", ScoreColor.rgb(0x0000FF)));

        assertEquals(new Marker("B", ScoreColor.rgb(0x0000FF)), panel.toMarker());
    }
}
