package com.gstncaruso.tabpro.core.editing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import org.junit.jupiter.api.Test;

/**
 * Manual, linea 764: "you can enter notes either on the tablature or on the standard notation
 * display. Each note added in a notation is automatically added in the other one." La tabla de
 * atajos (Reference, pp. 79-80) tiene dos filas de Enter -"Add a Note" en el pentagrama, "Next
 * Note" en la tablatura- que es la misma tecla decidiendo dos cosas distintas segun
 * {@link Cursor#notation()}.
 */
class EditorNotationTest {

    /**
     * El mismo Enter, dos efectos: en la tablatura avanza sin tocar la nota; en el pentagrama
     * agrega la nota ahi mismo sin avanzar. Si algun dia alguien vuelve a cablear "nav.nextNote"
     * directo a moveRight, este test lo agarra.
     */
    @Test
    void enterAddsInStandardNotationButAdvancesInTablature() {
        Editor tabEditor = new Editor(Score.blank());
        tabEditor.enter();
        assertEquals(1, tabEditor.cursor().beat(), "en la tablatura, Enter tiene que avanzar a la nota siguiente");
        assertTrue(tabEditor.score().track(0).measure(0).beat(0).isRest(), "en la tablatura, Enter no agrega nada");

        Editor staffEditor = new Editor(Score.blank());
        staffEditor.toggleNotation();
        staffEditor.enter();
        assertEquals(0, staffEditor.cursor().beat(), "en el pentagrama, Enter no avanza: agrega la nota ahi mismo");
        assertFalse(staffEditor.currentBeat().isRest(), "en el pentagrama, Enter tiene que agregar una nota");
    }

    /**
     * La altura donde esta el cursor, sin nota todavia, es la de la cuerda al aire: Enter tiene
     * que agregar exactamente esa altura -no una nota vacia ni un traste cualquiera.
     */
    @Test
    void enterInStandardNotationAddsTheNoteAtTheStringsOpenPitchWhenTheBeatIsSilent() {
        Editor editor = new Editor(Score.blank());
        editor.moveTo(0, 0, 3);
        editor.toggleNotation();

        editor.enter();

        Note added = editor.currentBeat().noteOn(3).orElseThrow();
        assertEquals(3, added.string());
        assertEquals(0, added.fret());
        assertEquals(editor.currentTrack().tuning().pitchOfString(3), editor.currentTrack().tuning().pitchOf(added));
    }

    /**
     * Decision para cuando ninguna cuerda alcanza la altura del cursor (por ejemplo, una nota
     * escrita a mano muy por encima del limite de trastes de la afinacion): Enter no hace nada,
     * igual que AutomaticFingering deja la nota como estaba cuando ninguna cuerda la alcanza.
     */
    @Test
    void enterDoesNothingInStandardNotationWhenNoStringCanReachTheCursorsPitch() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(50);
        editor.toggleNotation();
        boolean couldUndoBefore = editor.canUndo();

        editor.enter();

        Note note = editor.currentBeat().noteOn(1).orElseThrow();
        assertEquals(50, note.fret(), "sin cuerda que alcance esa altura, Enter no toca la nota que ya estaba");
        assertEquals(couldUndoBefore, editor.canUndo(), "sin cambios de verdad, el historial de deshacer no se tiene que mover");
    }
}
