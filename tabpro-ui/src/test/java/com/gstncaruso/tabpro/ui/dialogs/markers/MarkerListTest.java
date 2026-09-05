package com.gstncaruso.tabpro.ui.dialogs.markers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import java.util.List;
import org.junit.jupiter.api.Test;

class MarkerListTest {

    @Test
    void isEmptyForAFreshScore() {
        assertTrue(MarkerList.collect(Score.blank()).isEmpty());
    }

    @Test
    void collectsMarkersInMeasureOrder() {
        Editor editor = new Editor(Score.blank());
        editor.insertMeasure();
        editor.moveToFirstMeasure();
        editor.setMarker(Marker.named("Intro"));
        editor.moveToLastMeasure();
        editor.setMarker(Marker.named("Estribillo"));

        List<MarkerList.Positioned> markers = MarkerList.collect(editor.score());

        assertEquals(2, markers.size());
        assertEquals("Intro", markers.get(0).marker().name());
        assertEquals(0, markers.get(0).measureIndex());
        assertEquals("Estribillo", markers.get(1).marker().name());
        assertEquals(1, markers.get(1).measureIndex());
    }

    @Test
    void labelShowsOneBasedMeasureNumber() {
        MarkerList.Positioned positioned = new MarkerList.Positioned(2, Marker.named("Solo"));

        assertEquals("Compas 3: Solo", positioned.label());
    }
}
