package com.gstncaruso.tabpro.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import org.junit.jupiter.api.Test;

class StatusTextTest {

    @Test
    void describesTheCursorPositionOneBased() {
        Cursor cursor = new Cursor(0, 0, 0, 1);
        Beat beat = Beat.of(Duration.quarter(), new Note(1, 0));

        assertEquals("Compás 1 · Beat 1 · Cuerda 1 · Negra", StatusText.describe(cursor, beat));
    }
}
