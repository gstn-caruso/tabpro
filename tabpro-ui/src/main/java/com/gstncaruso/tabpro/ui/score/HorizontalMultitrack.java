package com.gstncaruso.tabpro.ui.score;

public final class HorizontalMultitrack {

    private HorizontalMultitrack() {
    }

    public static void applyTo(TrackVisibility visibleTracks, ViewMode mode, boolean forced) {
        if (forced && mode.scrollsHorizontally()) {
            visibleTracks.setMultitrack(true);
        }
    }
}
