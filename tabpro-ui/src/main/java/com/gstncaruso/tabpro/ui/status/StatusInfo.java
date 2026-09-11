package com.gstncaruso.tabpro.ui.status;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.ui.dialogs.style.Labels;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.score.Pagination;

public record StatusInfo(
        int pageNumber,
        int pageCount,
        int measureNumber,
        int measureCount,
        int trackNumber,
        String trackName,
        MeasureCompleteness completeness,
        String measureDurationText,
        String measureBeatsRatioText,
        String title,
        String author) {

    public static StatusInfo of(Editor editor, Pagination pagination) {
        Measure measure = editor.currentMeasure();
        ScoreInfo info = editor.score().info();
        return new StatusInfo(
                pagination.pageOf(editor.cursor().measure()),
                pagination.pageCount(),
                editor.cursor().measure() + 1,
                editor.score().measureCount(),
                editor.cursor().track() + 1,
                editor.currentTrack().name(),
                MeasureCompleteness.of(measure),
                MeasureDurationText.of(measure),
                MeasureBeatsRatioText.of(measure),
                titleOf(info),
                authorOf(info));
    }

    private static String titleOf(ScoreInfo info) {
        return info.title().isBlank() ? Texts.get("library.score.untitled") : info.title();
    }

    private static String authorOf(ScoreInfo info) {
        if (!Labels.creditsOf(info).isBlank()) {
            return Labels.creditsOf(info);
        }
        return info.artist();
    }
}
