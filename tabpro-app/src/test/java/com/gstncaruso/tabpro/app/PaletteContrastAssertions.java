package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.app.Theme.Palette;
import com.gstncaruso.tabpro.ui.theme.Contrast;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

final class PaletteContrastAssertions {

    private PaletteContrastAssertions() {
    }

    static void assertReads(Palette palette, double textMinimumRatio, double graphicalMinimumRatio) {
        Color accent = palette.accent();
        List<String> failures = new ArrayList<>();
        check(failures, "text / background", palette.text(), palette.background(), textMinimumRatio);
        check(failures, "text / panel", palette.text(), palette.panel(), textMinimumRatio);
        check(failures, "text / raised panel", palette.text(), palette.raisedPanel(), textMinimumRatio);
        check(failures, "muted text / panel", palette.mutedText(), palette.panel(), textMinimumRatio);
        check(failures, "muted text / raised panel", palette.mutedText(), palette.raisedPanel(),
                textMinimumRatio);
        check(failures, "separator / panel", palette.separator(), palette.panel(), graphicalMinimumRatio);
        check(failures, "separator / background", palette.separator(), palette.background(), graphicalMinimumRatio);
        check(failures, "focus / panel", accent, palette.panel(), graphicalMinimumRatio);
        check(failures, "focus / background", accent, palette.background(), graphicalMinimumRatio);
        check(failures, "menu selection (black text) / focus", Color.BLACK, accent, textMinimumRatio);
        assertTrue(
                failures.isEmpty(),
                () -> failures.size() + " pair(s) do not reach the minimum contrast:\n" + String.join("\n", failures));
    }

    private static void check(List<String> failures, String description, Color foreground, Color background,
            double minimumRatio) {
        double ratio = Contrast.ratio(foreground, background);
        if (ratio < minimumRatio) {
            failures.add(String.format("%s: %.2f (needs >= %.1f)", description, ratio, minimumRatio));
        }
    }
}
