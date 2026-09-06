package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Voice;
import com.gstncaruso.tabpro.core.model.VoicePart;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Guitar Pro escribe un traste cualquiera en la nota ligada -- la que continua a la
 * anterior sin volver a pulsar la cuerda -- porque el que vale es el de la nota que
 * continua. Los numeros de estos casos salen de "Tie.gp5", de la suite de PyGuitarPro:
 * ahi una ligada que continua un traste 2 viene escrita como 12.
 */
class GuitarProSoundingFretsTest {

    private final GuitarProSoundingFrets sounding = new GuitarProSoundingFrets();

    @Test
    void aTiedNoteTakesTheFretOfTheNoteItContinues() {
        Voice resolved = resolve(VoicePart.LEAD, note(1, 2), tied(1, 12));

        assertEquals(2, fretOf(resolved, 1, 1));
    }

    @Test
    void aChainOfTiedNotesKeepsTheSameFret() {
        Voice resolved = resolve(VoicePart.LEAD, note(1, 7), tied(1, 0), tied(1, 3));

        assertEquals(7, fretOf(resolved, 1, 1));
        assertEquals(7, fretOf(resolved, 2, 1));
    }

    @Test
    void everyStringRemembersItsOwnFret() {
        Voice resolved = resolve(VoicePart.LEAD, chord(note(1, 5), note(2, 9)), chord(tied(1, 0), tied(2, 0)));

        assertEquals(5, fretOf(resolved, 1, 1));
        assertEquals(9, fretOf(resolved, 1, 2));
    }

    /** La voz de bajos no le presta su traste a la principal ni al reves. */
    @Test
    void everyVoiceRemembersItsOwn() {
        resolve(VoicePart.LEAD, note(1, 5));
        Voice bass = resolve(VoicePart.BASS, note(1, 8), tied(1, 0));

        assertEquals(8, fretOf(bass, 1, 1));
    }

    /** La ligadura cruza el compas: la memoria es de toda la pista, no de un compas. */
    @Test
    void whatSoundsCrossesTheBarLine() {
        resolve(VoicePart.LEAD, note(1, 4));
        Voice next = resolve(VoicePart.LEAD, tied(1, 0));

        assertEquals(4, fretOf(next, 0, 1));
    }

    /** Un silencio no interrumpe lo que la cuerda venia sonando: no toca ninguna cuerda. */
    @Test
    void aRestDoesNotForgetWhatWasSounding() {
        Voice resolved = resolve(VoicePart.LEAD, note(1, 6), rest(), tied(1, 0));

        assertEquals(6, fretOf(resolved, 2, 1));
    }

    /** Sin nada sonando en esa cuerda la ligadura no continua nada: queda lo que dice el archivo. */
    @Test
    void aTieWithNothingToContinueKeepsWhatTheFileSays() {
        Voice resolved = resolve(VoicePart.LEAD, tied(1, 3));

        assertEquals(3, fretOf(resolved, 0, 1));
    }

    @Test
    void anUnusedVoiceStaysUnused() {
        assertEquals(Voice.unused(), sounding.resolving(VoicePart.BASS, Voice.unused()));
    }

    private Voice resolve(VoicePart part, Beat... beats) {
        return sounding.resolving(part, new Voice(Arrays.asList(beats)));
    }

    private static int fretOf(Voice voice, int beatIndex, int string) {
        return voice.beat(beatIndex).noteOn(string).orElseThrow().fret();
    }

    private static Beat note(int string, int fret) {
        return Beat.of(Duration.quarter(), new Note(string, fret));
    }

    private static Beat tied(int string, int fret) {
        return Beat.of(Duration.quarter(), new Note(string, fret).tied(true));
    }

    private static Beat chord(Beat... singles) {
        List<Note> notes = Arrays.stream(singles).flatMap(beat -> beat.notes().stream()).toList();
        return new Beat(Duration.quarter(), notes);
    }

    private static Beat rest() {
        return Beat.rest(Duration.quarter());
    }
}
