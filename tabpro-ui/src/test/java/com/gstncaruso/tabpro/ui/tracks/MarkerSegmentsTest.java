package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import java.util.List;
import org.junit.jupiter.api.Test;

class MarkerSegmentsTest {

    @Test
    void aScoreWithoutMarkersHasNoSegments() {
        Editor editor = new Editor(Score.blank());

        assertTrue(MarkerSegments.of(editor.score()).isEmpty());
    }

    @Test
    void oneMarkerCoversFromItsMeasureToTheEnd() {
        Editor editor = fourMeasureEditor();
        editor.moveToNextMeasure();
        editor.setMarker(Marker.named("Estribillo"));

        List<MarkerSegments.Segment> segments = MarkerSegments.of(editor.score());

        assertEquals(1, segments.size());
        assertEquals(1, segments.get(0).fromMeasure());
        assertEquals(4, segments.get(0).toMeasureExclusive());
        assertEquals("Estribillo", segments.get(0).marker().name());
    }

    @Test
    void eachMarkerStopsWhereTheNextOneStarts() {
        Editor editor = fourMeasureEditor();
        editor.setMarker(Marker.named("Intro"));
        editor.moveTo(2, 0, 1);
        editor.setMarker(Marker.named("Solo"));

        List<MarkerSegments.Segment> segments = MarkerSegments.of(editor.score());

        assertEquals(2, segments.size());
        assertEquals(0, segments.get(0).fromMeasure());
        assertEquals(2, segments.get(0).toMeasureExclusive());
        assertEquals("Intro", segments.get(0).marker().name());
        assertEquals(2, segments.get(1).fromMeasure());
        assertEquals(4, segments.get(1).toMeasureExclusive());
        assertEquals("Solo", segments.get(1).marker().name());
    }

    @Test
    void aMarkerOnTheLastMeasureCoversOnlyThatOne() {
        Editor editor = fourMeasureEditor();
        editor.moveTo(3, 0, 1);
        editor.setMarker(Marker.named("Final"));

        List<MarkerSegments.Segment> segments = MarkerSegments.of(editor.score());

        assertEquals(3, segments.get(0).fromMeasure());
        assertEquals(4, segments.get(0).toMeasureExclusive());
    }

    private Editor fourMeasureEditor() {
        Editor editor = new Editor(Score.blank());
        editor.insertMeasure();
        editor.insertMeasure();
        editor.insertMeasure();
        editor.moveToFirstMeasure();
        return editor;
    }
}
