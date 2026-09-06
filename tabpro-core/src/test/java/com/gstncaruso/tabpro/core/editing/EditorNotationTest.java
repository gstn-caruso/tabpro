package com.gstncaruso.tabpro.core.editing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import java.util.List;
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

    /**
     * La altura es la pedida, no cualquiera: Enter tiene que preservar la altura exacta de la
     * nota que ya sonaba en la cuerda del cursor -no un traste 0 por default- verificado contra
     * {@code tuning().pitchOf(nota)}, como pide el enunciado.
     */
    @Test
    void enterInStandardNotationPreservesTheExactPitchOfTheNoteAlreadyOnTheCursorsString() {
        Beat beat = Beat.of(Duration.quarter(), new Note(2, 7));
        Editor editor = editorWithFirstBeat(beat);
        editor.moveTo(0, 0, 2);
        editor.toggleNotation();
        Pitch expected = editor.currentTrack().tuning().pitchOf(new Note(2, 7));

        editor.enter();

        Note result = editor.currentBeat().noteOn(2).orElseThrow();
        assertEquals(expected, editor.currentTrack().tuning().pitchOf(result),
                "la altura tiene que ser la que estaba, ni un semitono de mas ni de menos");
    }

    /**
     * Manual, Reference p. 80: en la tablatura las flechas mueven cuerda por cuerda; en el
     * pentagrama tienen que moverse grado por grado, que para una cuerda al aire casi nunca
     * coincide con "la cuerda de al lado".
     */
    @Test
    void arrowsMoveByStringInTablatureAndByStaffDegreeInStandardNotation() {
        Editor tabEditor = new Editor(Score.blank());
        tabEditor.moveTo(0, 0, 3);
        tabEditor.moveUp();
        assertEquals(2, tabEditor.cursor().string(), "en la tablatura, arriba tiene que ir a la cuerda anterior");

        Editor staffEditor = new Editor(Score.blank());
        staffEditor.moveTo(0, 0, 3);
        staffEditor.toggleNotation();
        staffEditor.moveUp();
        assertEquals(3, staffEditor.cursor().string(),
                "en el pentagrama, arriba tiene que moverse por grado -de sol al aire (cuerda 3) al la del"
                        + " traste 2 de la misma cuerda, la nota natural mas cercana- no saltar de cuerda"
                        + " como en la tablatura");
    }

    /**
     * Decision simetrica a la de Enter: si ningun traste de ninguna cuerda alcanza el grado
     * siguiente (por ejemplo, un grado mas grave que la cuerda mas grave al aire), la flecha no
     * mueve el cursor.
     */
    @Test
    void arrowsStayPutInStandardNotationWhenNoStringCanReachTheNextDegree() {
        Editor editor = new Editor(Score.blank());
        editor.moveTo(0, 0, 6);
        editor.toggleNotation();

        editor.moveDown();

        assertEquals(6, editor.cursor().string(), "sin cuerda que llegue a la altura de abajo, el cursor se queda quieto");
    }

    private static Editor editorWithFirstBeat(Beat beat) {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                beat, Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter())));
        Track track = Track.standardGuitar("Test").withMeasure(0, measure);
        return new Editor(Score.blank().withTrack(0, track));
    }
}
