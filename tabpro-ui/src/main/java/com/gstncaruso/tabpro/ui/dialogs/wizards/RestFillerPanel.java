package com.gstncaruso.tabpro.ui.dialogs.wizards;

import com.gstncaruso.tabpro.core.editing.wizards.MeasureRange;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;

/** Completar o reducir con silencios los compases del rango elegido. */
public final class RestFillerPanel extends FormPanel {

    private final MeasureRangePanel range;
    private final TrackScopePanel scope = new TrackScopePanel();

    public RestFillerPanel(int measureCount) {
        range = new MeasureRangePanel(measureCount);
        addFullWidthRow(range);
        addFullWidthRow(scope);
    }

    public MeasureRange toMeasureRange() {
        return range.toMeasureRange();
    }

    public boolean everyTrack() {
        return scope.everyTrackSelected();
    }
}
