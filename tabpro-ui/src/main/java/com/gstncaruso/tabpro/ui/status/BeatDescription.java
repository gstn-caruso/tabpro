package com.gstncaruso.tabpro.ui.status;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.ui.dialogs.style.Labels;
import com.gstncaruso.tabpro.ui.i18n.Texts;

public final class BeatDescription {

    private BeatDescription() {
    }

    public static String describe(Cursor cursor, Beat beat) {
        return Texts.get("views.BeatDescription.summary",
                String.valueOf(cursor.measure() + 1), String.valueOf(cursor.beat() + 1),
                String.valueOf(cursor.string()), describe(beat));
    }

    private static String describe(Beat beat) {
        String name = nameOf(beat.duration());
        return beat.isRest() ? Texts.get("views.BeatDescription.rest", name.toLowerCase()) : name;
    }

    private static String nameOf(Duration duration) {
        String name = Labels.of(duration.value());
        return duration.dotted() ? Texts.get("views.BeatDescription.dotted", name) : name;
    }
}
