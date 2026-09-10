package com.gstncaruso.tabpro.ui.toolbar;

import com.gstncaruso.tabpro.ui.actions.Command;
import com.gstncaruso.tabpro.ui.actions.Commands;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.score.Zoom;
import com.gstncaruso.tabpro.ui.score.ZoomHolder;
import java.awt.Dimension;
import java.util.OptionalInt;
import javax.swing.JComboBox;

public final class ZoomSelector extends JComboBox<String> {

    private final ZoomHolder zoomHolder;

    public ZoomSelector(ZoomHolder zoomHolder, Commands commands) {
        super(presetLabels());
        this.zoomHolder = zoomHolder;
        setEditable(true);
        getAccessibleContext().setAccessibleName(Texts.get("menus.toolbar.zoom"));
        setToolTipText(tooltipFrom(commands));
        addActionListener(event -> applyEnteredZoom());
        zoomHolder.onZoomChange(this::refresh);
        refresh();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    private static String tooltipFrom(Commands commands) {
        Command zoomIn = commands.get("view.zoomIn");
        Command zoomOut = commands.get("view.zoomOut");
        return zoomIn.description() + " [" + zoomIn.acceleratorText() + "]  ·  "
                + zoomOut.description() + " [" + zoomOut.acceleratorText() + "]";
    }

    private void applyEnteredZoom() {
        String entered = String.valueOf(getEditor().getItem());
        parsedPercentOf(entered).ifPresentOrElse(
                percent -> zoomHolder.setZoom(new Zoom(percent)),
                this::refresh);
    }

    private static OptionalInt parsedPercentOf(String text) {
        try {
            int percent = Integer.parseInt(text.strip().replace("%", ""));
            return percent >= Zoom.MIN_PERCENT && percent <= Zoom.MAX_PERCENT
                    ? OptionalInt.of(percent) : OptionalInt.empty();
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
