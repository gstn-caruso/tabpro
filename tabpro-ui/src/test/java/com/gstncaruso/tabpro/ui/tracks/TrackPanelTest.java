package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.AwaitEdt;
import com.gstncaruso.tabpro.ui.score.TrackVisibility;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class TrackPanelTest {

    @Test
    void movingTheCursorSkipsTheHeavyGlobalViewRefresh() throws Exception {
        Editor editor = editorWithTwoMeasures();
        SpyingMeasureGrid grid = new SpyingMeasureGrid(editor);
        GlobalView globalView = new GlobalView(new MarkerZone(editor), grid);
        new TrackPanel(editor, new TrackVisibility(), globalView);
        grid.forgetCallsMadeWhileBuilding();

        javax.swing.SwingUtilities.invokeAndWait(editor::moveToNextMeasure);

        assertEquals(0, grid.revalidateCalls);
        assertFalse(grid.fullRepaintCalled);
    }

    @Test
    void editingANoteStillRefreshesTheGlobalView() throws Exception {
        Editor editor = editorWithTwoMeasures();
        SpyingMeasureGrid grid = new SpyingMeasureGrid(editor);
        GlobalView globalView = new GlobalView(new MarkerZone(editor), grid);
        new TrackPanel(editor, new TrackVisibility(), globalView);
        grid.forgetCallsMadeWhileBuilding();

        javax.swing.SwingUtilities.invokeAndWait(() -> editor.setFret(3));

        assertEquals(1, grid.revalidateCalls);
    }

    private static Editor editorWithTwoMeasures() {
        List<Measure> measures = new ArrayList<>();
        measures.add(Measure.empty(new TimeSignature(1, 4), Duration.quarter()));
        measures.add(Measure.empty(new TimeSignature(1, 4), Duration.quarter()));
        Track guitar = Track.standardGuitar("Guitarra").withMeasures(measures);
        return new Editor(new Score("Prueba", 120, List.of(guitar)));
    }

    @Test
    void listsOneRowPerTrack() {
        Editor editor = new Editor(Score.blank());
        TrackPanel panel = new TrackPanel(editor);

        assertEquals(1, panel.rows().size());
    }

    @Test
    void addsARowWhenATrackIsAdded() {
        Editor editor = new Editor(Score.blank());
        TrackPanel panel = new TrackPanel(editor);

        panel.addBass();
        AwaitEdt.flush();

        assertEquals(2, panel.rows().size());
        assertEquals("Bajo", editor.score().track(1).name());
        assertEquals(4, editor.score().track(1).tuning().stringCount());
    }

    @Test
    void namesTheSecondTrackOfAKindApart() {
        Editor editor = new Editor(Score.blank());
        TrackPanel panel = new TrackPanel(editor);

        panel.addGuitar();

        assertEquals("Guitarra 2", editor.score().track(1).name());
    }

    @Test
    void dropsTheRowWhenATrackIsRemoved() {
        Editor editor = new Editor(Score.blank());
        TrackPanel panel = new TrackPanel(editor);
        panel.addBass();
        AwaitEdt.flush();

        panel.removeSelectedTrack();
        AwaitEdt.flush();

        assertEquals(1, panel.rows().size());
        assertEquals(1, editor.score().trackCount());
    }

    @Test
    void growsTallerWithEveryTrack() {
        Editor editor = new Editor(Score.blank());
        TrackPanel panel = new TrackPanel(editor);
        int oneTrack = panel.preferredPanelHeight();

        panel.addBass();

        assertTrue(panel.preferredPanelHeight() > oneTrack);
    }

    @Test
    void theGridIsAsTallAsTheListOfTracks() {
        Editor editor = new Editor(Score.blank());
        TrackPanel panel = new TrackPanel(editor);
        panel.addBass();

        assertEquals(
                MeasureGrid.NUMBERS_HEIGHT + 2 * TrackPanel.ROW_HEIGHT,
                panel.gridComponent().getPreferredSize().height);
    }

    @Test
    void theHeaderIsOneThirdTallerThanADataRowLikeInGuitarPro5() {
        assertEquals(Math.round(TrackPanel.ROW_HEIGHT * 1.3f), TrackPanel.HEADER_HEIGHT);
    }
}
