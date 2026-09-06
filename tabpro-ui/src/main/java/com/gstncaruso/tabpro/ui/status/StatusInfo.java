package com.gstncaruso.tabpro.ui.status;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.ScoreInfo;

/**
 * Todo lo que la barra de estado tiene que mostrar, calculado a partir del editor. Quien decide
 * que mostrar no es quien lo dibuja: esta clase no sabe nada de Swing.
 */
public record StatusInfo(
        int pageNumber,
        int measureNumber,
        int trackNumber,
        String trackName,
        MeasureCompleteness completeness,
        String measureDurationText,
        String title,
        String author) {

    /** Tabpro todavia no pagina la partitura: por ahora todo entra en una sola pagina. */
    private static final int ONLY_PAGE = 1;

    public static StatusInfo of(Editor editor) {
        Measure measure = editor.currentMeasure();
        ScoreInfo info = editor.score().info();
        return new StatusInfo(
                ONLY_PAGE,
                editor.cursor().measure() + 1,
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
