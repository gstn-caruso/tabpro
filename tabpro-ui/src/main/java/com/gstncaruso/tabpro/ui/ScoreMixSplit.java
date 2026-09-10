package com.gstncaruso.tabpro.ui;

import com.gstncaruso.tabpro.ui.tracks.TrackPanel;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JSplitPane;

public final class ScoreMixSplit {

    private final JComponent score;
    private final TrackPanel mixTable;
    private final JSplitPane split;
    private boolean swapped;

    public ScoreMixSplit(JComponent score, TrackPanel mixTable) {
        this.score = score;
        this.mixTable = mixTable;
        this.split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, score, mixTable);
        split.setResizeWeight(1);
        split.setBorder(BorderFactory.createEmptyBorder());
    }

    public JSplitPane component() {
        return split;
    }

    public boolean isSwapped() {
        return swapped;
    }

    public java.awt.Component top() {
        return split.getTopComponent();
    }

    public java.awt.Component bottom() {
        return split.getBottomComponent();
    }

    public void toggleView() {
        int mixTableHeight = mixTable.getHeight();
        swapped = !swapped;
        // JSplitPane will not move a component that is already its child to the other slot
        // unless it is removed from its current one first.
        split.setTopComponent(null);
        split.setBottomComponent(null);
        if (swapped) {
            split.setTopComponent(mixTable);
            split.setBottomComponent(score);
        } else {
            split.setTopComponent(score);
            split.setBottomComponent(mixTable);
        }
        applyMixTableHeight(mixTableHeight);
    }

    public void showMixTable() {
        applyMixTableHeight(mixTable.preferredPanelHeight());
    }

    public void hideMixTable() {
        split.setDividerLocation(swapped ? 0 : split.getHeight());
    }

    private void applyMixTableHeight(int height) {
        int total = split.getHeight();
        int location = swapped ? height : Math.max(0, total - split.getDividerSize() - height);
        split.setDividerLocation(location);
    }
}
