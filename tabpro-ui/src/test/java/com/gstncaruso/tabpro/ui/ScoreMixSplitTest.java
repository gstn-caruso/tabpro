package com.gstncaruso.tabpro.ui;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.i18n.TextsDefaultNames;
import com.gstncaruso.tabpro.ui.tracks.TrackPanel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

class ScoreMixSplitTest {

    private final JComponent score = new JPanel();
    private final TrackPanel mixTable = new TrackPanel(new Editor(Score.blank(new TextsDefaultNames())));
    private final ScoreMixSplit split = new ScoreMixSplit(score, mixTable);

    @Test
    void theScoreStartsOnTopAndTheMixTableAtTheBottom() {
        assertSame(score, split.top());
        assertSame(mixTable, split.bottom());
    }

    @Test
    void togglingViewsPutsTheMixTableOnTopAndTheScoreAtTheBottom() {
        split.toggleView();

        assertSame(mixTable, split.top());
        assertSame(score, split.bottom());
    }

    @Test
    void togglingTwiceLeavesEverythingAsItWas() {
        split.toggleView();
        split.toggleView();

        assertSame(score, split.top());
        assertSame(mixTable, split.bottom());
    }

    @Test
    void theMixTableKeepsItsSmallStripAfterToggling() {
        layOutAt(900, 700);
        split.showMixTable();
        layOut();
        int heightBefore = mixTable.getHeight();
        assertTrue(heightBefore > 0 && heightBefore < 700 / 2,
                "the mix table should be a small strip, measured " + heightBefore);

        split.toggleView();
        layOut();

        int heightAfter = mixTable.getHeight();
        assertTrue(Math.abs(heightBefore - heightAfter) <= 3,
                "the mix table's strip should stay put, measured " + heightBefore + " and became " + heightAfter);
        assertTrue(heightAfter < 700 / 2, "the mix table should not take up half the window, measured " + heightAfter);
    }

    private void layOutAt(int width, int height) {
        split.component().setSize(width, height);
    }

    private void layOut() {
        split.component().doLayout();
    }
}
