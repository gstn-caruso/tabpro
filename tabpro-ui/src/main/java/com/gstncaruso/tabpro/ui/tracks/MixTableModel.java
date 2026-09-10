package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.ui.score.TrackVisibility;

public final class MixTableModel {

    private final TrackVisibility visibleTracks;
    private boolean reduced;

    public MixTableModel() {
        this(new TrackVisibility());
    }

    public MixTableModel(TrackVisibility visibleTracks) {
        this.visibleTracks = visibleTracks;
    }

    public boolean isVisibleInMultitrackView(int trackIndex) {
        return visibleTracks.isTurnedOn(trackIndex);
    }

    public void toggleVisibleInMultitrackView(int trackIndex) {
        setVisibleInMultitrackView(trackIndex, !isVisibleInMultitrackView(trackIndex));
    }

    public void setVisibleInMultitrackView(int trackIndex, boolean visible) {
        visibleTracks.setTurnedOn(trackIndex, visible);
    }

    public boolean isReduced() {
        return reduced;
    }

    public void reduceAllParameters() {
        reduced = true;
    }

    public void restoreAllParameters() {
        reduced = false;
    }
}
