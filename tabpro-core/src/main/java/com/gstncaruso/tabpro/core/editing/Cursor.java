package com.gstncaruso.tabpro.core.editing;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.VoicePart;
import java.util.Optional;

/**
 * Donde esta parada la edicion: pista, compas, voz, beat, cuerda, en que notacion se edita y,
 * mientras se navega por el pentagrama, a que altura apunta el cursor mas alla de que haya o no
 * una nota ahi.
 *
 * <p>{@code pointer} es exclusivo del pentagrama: en la tablatura la cuerda y el traste ya dicen
 * todo, asi que viaja vacio y ninguna navegacion en tablatura lo toca. Se pisa con
 * {@link #withPointer} cada vez que una flecha se mueve un grado, y se reinicia -vuelve a
 * {@code Optional.empty()}, para que la proxima lectura lo derive de nuevo de la nota o la cuerda
 * al aire- en cualquier arribo a una posicion distinta: otro beat, otro compas, otra pista o un
 * cambio de notacion. Sin esto, la segunda flecha volvia a partir de cero en vez de seguir
 * subiendo grado a grado.
 */
public record Cursor(int track, int measure, VoicePart voice, int beat, int string, Notation notation,
        Optional<Pitch> pointer) {

    public Cursor(int track, int measure, int beat, int string) {
        this(track, measure, VoicePart.LEAD, beat, string, Notation.TABLATURE, Optional.empty());
    }

    public Cursor onTrack(int track) {
        return new Cursor(track, measure, voice, beat, string, notation, Optional.empty());
    }

    public Cursor onMeasure(int measure) {
        return new Cursor(track, measure, voice, beat, string, notation, Optional.empty());
    }

    public Cursor onVoice(VoicePart voice) {
        return new Cursor(track, measure, voice, beat, string, notation, pointer);
    }

    public Cursor onBeat(int beat) {
        return new Cursor(track, measure, voice, beat, string, notation, Optional.empty());
    }

    /** Cambiar de cuerda no es llegar a otro lado -la tablatura navega asi siempre-, asi que el
     * puntero del pentagrama, si habia uno, se conserva tal cual. */
    public Cursor onString(int string) {
        return new Cursor(track, measure, voice, beat, string, notation, pointer);
    }

    public Cursor onNotation(Notation notation) {
        return new Cursor(track, measure, voice, beat, string, notation, Optional.empty());
    }

    /** La altura a la que apunta el cursor en el pentagrama, un grado despues de la flecha. */
    public Cursor withPointer(Pitch pointer) {
        return new Cursor(track, measure, voice, beat, string, notation, Optional.of(pointer));
    }

    public Cursor at(int measure, int beat) {
        return new Cursor(track, measure, voice, beat, string, notation, Optional.empty());
    }
}
