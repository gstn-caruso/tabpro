package com.gstncaruso.tabpro.ui.status;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.ui.score.Pagination;

/**
 * Todo lo que la barra de estado tiene que mostrar, calculado a partir del editor y de como quedo
 * repartida la partitura en hojas. Quien decide que mostrar no es quien lo dibuja: esta clase no
 * sabe nada de Swing.
 */
public record StatusInfo(
        int pageNumber,
        int pageCount,
        int measureNumber,
        int measureCount,
        int trackNumber,
        String trackName,
        MeasureCompleteness completeness,
        String measureDurationText,
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
                titleOf(info),
                authorOf(info));
    }

    private static String titleOf(ScoreInfo info) {
        return info.title().isBlank() ? "Sin título" : info.title();
    }

    private static String authorOf(ScoreInfo info) {
        if (!info.credits().isBlank()) {
            return info.credits();
        }
        return info.artist();
    }
}
