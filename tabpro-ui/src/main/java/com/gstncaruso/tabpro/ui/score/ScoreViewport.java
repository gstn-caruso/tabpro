package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.ui.page.PageMetrics;
import com.gstncaruso.tabpro.ui.page.PageSetup;

public record ScoreViewport(
        ViewMode mode, Zoom zoom, int width, VisibleTracks visibleTracks, VisibleNotations visibleNotations,
        boolean graysTheInactiveVoice, PageSetup pageSetup, boolean showsDynamicNotes) {

    public static ScoreViewport of(ViewMode mode, Zoom zoom, int width) {
        return new ScoreViewport(
                mode, zoom, width, VisibleTracks.all(), VisibleNotations.both(), true, PageSetup.defaults(), false);
    }

    public ScoreViewport withVisibleTracks(VisibleTracks visibleTracks) {
        return new ScoreViewport(
                mode, zoom, width, visibleTracks, visibleNotations, graysTheInactiveVoice, pageSetup,
                showsDynamicNotes);
    }

    public ScoreViewport withVisibleNotations(VisibleNotations visibleNotations) {
        return new ScoreViewport(
                mode, zoom, width, visibleTracks, visibleNotations, graysTheInactiveVoice, pageSetup,
                showsDynamicNotes);
    }

    public ScoreViewport withGrayingTheInactiveVoice(boolean graysTheInactiveVoice) {
        return new ScoreViewport(
                mode, zoom, width, visibleTracks, visibleNotations, graysTheInactiveVoice, pageSetup,
                showsDynamicNotes);
    }

    public ScoreViewport withPageSetup(PageSetup pageSetup) {
        return new ScoreViewport(
                mode, zoom, width, visibleTracks, visibleNotations, graysTheInactiveVoice, pageSetup,
                showsDynamicNotes);
    }

    public ScoreViewport withShowsDynamicNotes(boolean showsDynamicNotes) {
        return new ScoreViewport(
                mode, zoom, width, visibleTracks, visibleNotations, graysTheInactiveVoice, pageSetup,
                showsDynamicNotes);
    }

    public java.util.Optional<com.gstncaruso.tabpro.core.model.VoicePart> highlighted(
            com.gstncaruso.tabpro.core.model.VoicePart editedVoice) {
        return graysTheInactiveVoice ? java.util.Optional.of(editedVoice) : java.util.Optional.empty();
    }

    public PageMetrics sheet() {
        return PageMetrics.of(pageSetup);
    }

    public double factor() {
        return zoom.factor();
    }
}
