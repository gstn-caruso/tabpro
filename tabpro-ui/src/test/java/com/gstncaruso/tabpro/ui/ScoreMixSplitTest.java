package com.gstncaruso.tabpro.ui;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.tracks.TrackPanel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

class ScoreMixSplitTest {

    private final JComponent score = new JPanel();
    private final TrackPanel mixTable = new TrackPanel(new Editor(Score.blank()));
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
                "la mesa deberia ser una franja chica, midio " + heightBefore);

        split.toggleView();
        layOut();

        int heightAfter = mixTable.getHeight();
        assertTrue(Math.abs(heightBefore - heightAfter) <= 3,
                "la franja de la mesa deberia mantenerse, media " + heightBefore + " y paso a " + heightAfter);
        assertTrue(heightAfter < 700 / 2, "la mesa no deberia ocupar la mitad de la ventana, midio " + heightAfter);
    }

    private void layOutAt(int width, int height) {
        split.component().setSize(width, height);
    }

    private void layOut() {
        split.component().doLayout();
    }
}
