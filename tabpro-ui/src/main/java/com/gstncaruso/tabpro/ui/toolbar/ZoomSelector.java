package com.gstncaruso.tabpro.ui.toolbar;

import com.gstncaruso.tabpro.ui.actions.Commands;
import com.gstncaruso.tabpro.ui.score.Zoom;
import com.gstncaruso.tabpro.ui.score.ZoomHolder;
import java.util.OptionalInt;
import javax.swing.JComboBox;

/**
 * Guitar Pro 5, manual pagina 14, fila 1: el zoom es un combo editable con el porcentaje visible
 * («100% ▾»), con los valores predefinidos del manual y la posibilidad de tipear uno.
 */
public final class ZoomSelector extends JComboBox<String> {

    private final ZoomHolder zoomHolder;

    public ZoomSelector(ZoomHolder zoomHolder, Commands commands) {
        super(presetLabels());
        this.zoomHolder = zoomHolder;
        addActionListener(event -> applyEnteredZoom());
        refresh();
    }

    private void applyEnteredZoom() {
        String entered = String.valueOf(getEditor().getItem());
        parsedPercentOf(entered).ifPresentOrElse(
                percent -> zoomHolder.setZoom(new Zoom(percent)),
                this::refresh);
    }

    private static OptionalInt parsedPercentOf(String text) {
        try {
            return OptionalInt.of(Integer.parseInt(text.strip().replace("%", "")));
        } catch (NumberFormatException notANumber) {
            return OptionalInt.empty();
        }
    }

    private void refresh() {
        getEditor().setItem(labelOf(zoomHolder.zoom()));
    }

    private static String[] presetLabels() {
        return Zoom.presets().stream().map(percent -> percent + "%").toArray(String[]::new);
    }

    private static String labelOf(Zoom zoom) {
        return zoom.percent() + "%";
    }
}
