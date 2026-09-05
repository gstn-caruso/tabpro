package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import org.junit.jupiter.api.Test;

class TrackPanelTest {

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

        panel.removeSelectedTrack();

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
                TrackPanel.HEADER_HEIGHT + 2 * TrackPanel.ROW_HEIGHT,
                panel.gridComponent().getPreferredSize().height);
    }
}
