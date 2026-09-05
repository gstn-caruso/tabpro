package com.gstncaruso.tabpro.ui;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.model.Beat;

public final class StatusText {

    private StatusText() {
    }

    public static String describe(Cursor cursor, Beat beat) {
        return "Compás " + (cursor.measure() + 1)
                + " · Beat " + (cursor.beat() + 1)
                + " · Cuerda " + cursor.string()
                + " · Negra";
    }
}
