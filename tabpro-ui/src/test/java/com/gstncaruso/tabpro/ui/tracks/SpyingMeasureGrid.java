package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.core.editing.Editor;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

final class SpyingMeasureGrid extends MeasureGrid {
    int revalidateCalls;
    boolean fullRepaintCalled;
    final List<Rectangle> repaintedAreas = new ArrayList<>();

    SpyingMeasureGrid(Editor editor) {
        super(editor);
    }

    @Override
    public void revalidate() {
        revalidateCalls++;
        super.revalidate();
    }

    @Override
    public void repaint() {
        fullRepaintCalled = true;
        super.repaint();
    }

    @Override
    public void repaint(Rectangle area) {
        repaintedAreas.add(area);
        super.repaint(area);
    }

    void forgetCallsMadeWhileBuilding() {
        revalidateCalls = 0;
        fullRepaintCalled = false;
        repaintedAreas.clear();
    }
}
