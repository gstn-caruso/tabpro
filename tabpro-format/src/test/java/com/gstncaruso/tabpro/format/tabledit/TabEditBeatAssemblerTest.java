package com.gstncaruso.tabpro.format.tabledit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.effects.NoteEffects;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * TablEdit ubica cada nota o silencio en una posicion absoluta de la grilla;
 * tabpro pide una secuencia de beats consecutivos. Este es el paso que arma
 * esa secuencia, rellenando con silencio cualquier lugar de la grilla que el
 * archivo haya dejado sin marcar.
 */
class TabEditBeatAssemblerTest {

    private static final TabEditMeasure FOUR_FOUR = new TabEditMeasure(TimeSignature.fourFour(), KeySignature.cMajor());
    private final TabEditBeatAssembler assembler = new TabEditBeatAssembler();

    @Test
    void cuatroNegrasSeguidasLlenanElCompasSinRelleno() {
        List<TabEditEvent> events = new ArrayList<>();
        for (int gridPosition = 0; gridPosition < 16; gridPosition += 4) {
            events.add(noteAt(0, gridPosition, gridPosition / 4, NoteValue.QUARTER, VoicePart.LEAD));
        }

        Measure measure = assembler.assembleTrack(0, List.of(FOUR_FOUR), events).get(0);

        assertEquals(4, measure.beats().size());
        assertTrue(measure.isComplete());
        assertEquals(List.of(0, 1, 2, 3), fretsOf(measure));
    }

    @Test
    void unHuecoEnLaGrillaSeRellenaConUnSilencio() {
        // Una negra en la posicion 0 (ocupa los lugares 0 a 3 de la grilla), y recien la
        // siguiente nota en el lugar 12: faltan los lugares 4 a 11, media redonda de silencio.
        List<TabEditEvent> events = List.of(
                noteAt(0, 0, 5, NoteValue.QUARTER, VoicePart.LEAD),
                noteAt(0, 12, 7, NoteValue.QUARTER, VoicePart.LEAD));

        Measure measure = assembler.assembleTrack(0, List.of(FOUR_FOUR), events).get(0);

        assertEquals(3, measure.beats().size());
        assertTrue(measure.beats().get(1).isRest());
        assertEquals(NoteValue.HALF, measure.beats().get(1).duration().value());
        assertTrue(measure.isComplete());
    }

    @Test
    void variasNotasEnLaMismaPosicionFormanUnAcorde() {
        List<TabEditEvent> events = List.of(
                noteAt(0, 0, 0, NoteValue.QUARTER, VoicePart.LEAD, 1),
                noteAt(0, 0, 2, NoteValue.QUARTER, VoicePart.LEAD, 2));

        Measure measure = assembler.assembleTrack(0, List.of(FOUR_FOUR), events).get(0);

        assertEquals(2, measure.beat(0).notes().size());
    }

    @Test
    void laVozSecundariaQuedaSinUsarSiNoTraeEventos() {
        List<TabEditEvent> events = List.of(noteAt(0, 0, 0, NoteValue.QUARTER, VoicePart.LEAD));

        Measure measure = assembler.assembleTrack(0, List.of(FOUR_FOUR), events).get(0);

        assertFalse(measure.usesTwoVoices());
    }

    @Test
    void laVozSecundariaSeArmaIgualQueLaPrincipal() {
        List<TabEditEvent> events = List.of(
                noteAt(0, 0, 0, NoteValue.QUARTER, VoicePart.LEAD),
                noteAt(0, 0, 5, NoteValue.QUARTER, VoicePart.BASS));

        Measure measure = assembler.assembleTrack(0, List.of(FOUR_FOUR), events).get(0);

        assertTrue(measure.usesTwoVoices());
    }

    @Test
    void unCompasSinEventosQuedaComoUnSoloSilencioCompleto() {
        Measure measure = assembler.assembleTrack(0, List.of(FOUR_FOUR), List.of()).get(0);

        assertEquals(1, measure.beats().size());
        assertTrue(measure.beats().get(0).isRest());
        assertTrue(measure.isComplete());
    }

    @Test
    void unaNotaLigadaLlegaLigadaHastaElModeloFinal() {
        List<TabEditEvent> events = List.of(noteAt(0, 0, 0, NoteValue.QUARTER, VoicePart.LEAD, 1, true));

        Measure measure = assembler.assembleTrack(0, List.of(FOUR_FOUR), events).get(0);

        assertTrue(measure.beat(0).notes().get(0).tied());
    }

    @Test
    void tapSlapYFadeInDeLaNotaQuedanEnElBeatEntero() {
        TabEditPosition position = new TabEditPosition(0, 0, 0, 0);
        Duration duration = new Duration(NoteValue.QUARTER, false);
        Note note = new Note(1, 3, false, NoteEffects.none());
        TabEditEvent event = new TabEditNoteEvent(position, duration, VoicePart.LEAD, note, true, false, false);

        Measure measure = assembler.assembleTrack(0, List.of(FOUR_FOUR), List.of(event)).get(0);

        assertTrue(measure.beat(0).effects().tapping());
    }

    private static List<Integer> fretsOf(Measure measure) {
        List<Integer> frets = new ArrayList<>();
        for (Beat beat : measure.beats()) {
            for (Note note : beat.notes()) {
                frets.add(note.fret());
            }
        }
        return frets;
    }

    private static TabEditEvent noteAt(int trackIndex, int gridPosition, int fret, NoteValue value, VoicePart voice) {
        return noteAt(trackIndex, gridPosition, fret, value, voice, 1);
    }

    private static TabEditEvent noteAt(
            int trackIndex, int gridPosition, int fret, NoteValue value, VoicePart voice, int string) {
        return noteAt(trackIndex, gridPosition, fret, value, voice, string, false);
    }

    private static TabEditEvent noteAt(
            int trackIndex, int gridPosition, int fret, NoteValue value, VoicePart voice, int string, boolean tied) {
        TabEditPosition position = new TabEditPosition(0, gridPosition, string - 1, trackIndex);
        Duration duration = new Duration(value, false);
        Note note = new Note(string, fret, tied, NoteEffects.none());
        return new TabEditNoteEvent(position, duration, voice, note, false, false, false);
    }
}
