package com.gstncaruso.tabpro.ui.status;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import org.junit.jupiter.api.Test;

class BeatDescriptionTest {

    @Test
    void describesTheCursorPositionOneBased() {
        Cursor cursor = new Cursor(0, 0, 0, 1);
        Beat beat = Beat.of(Duration.quarter(), new Note(1, 0));

        assertEquals("Compás 1 · Beat 1 · Cuerda 1 · Negra", BeatDescription.describe(cursor, beat));
    }

    @Test
    void namesADottedDuration() {
        Cursor cursor = new Cursor(0, 0, 0, 1);
        Beat beat = Beat.of(new Duration(NoteValue.HALF, true), new Note(1, 0));

        assertEquals("Compás 1 · Beat 1 · Cuerda 1 · Blanca con puntillo", BeatDescription.describe(cursor, beat));
    }

    @Test
    void describesARest() {
        Cursor cursor = new Cursor(0, 0, 0, 1);
        Beat beat = Beat.rest(Duration.quarter());

        assertEquals("Compás 1 · Beat 1 · Cuerda 1 · Silencio de negra", BeatDescription.describe(cursor, beat));
    }
}
