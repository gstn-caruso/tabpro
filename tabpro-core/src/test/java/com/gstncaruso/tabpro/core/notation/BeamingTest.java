package com.gstncaruso.tabpro.core.notation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.effects.BeamBreak;
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import java.util.List;
import org.junit.jupiter.api.Test;

class BeamingTest {

    private static final Duration EIGHTH = new Duration(NoteValue.EIGHTH, false);
    private static final Duration SIXTEENTH = new Duration(NoteValue.SIXTEENTH, false);
    private static final Duration HALF_DOTTED = new Duration(NoteValue.HALF, true);
    private static final Duration QUARTER = Duration.quarter();

    private static Beat note(Duration duration) {
        return Beat.of(duration, new Note(1, 0));
    }

    private static Beat note(Duration duration, BeamBreak beamBreak) {
        return note(duration).withEffects(BeatEffects.none().withBeamBreak(beamBreak));
    }

    private static Beat rest(Duration duration) {
        return Beat.rest(duration);
    }

    private static Beat rest(Duration duration, BeamBreak beamBreak) {
        return rest(duration).withEffects(BeatEffects.none().withBeamBreak(beamBreak));
    }

    @Test
    void beamCountIsZeroForQuarterHalfAndWhole() {
        assertEquals(0, Beaming.beamCount(NoteValue.QUARTER));
        assertEquals(0, Beaming.beamCount(NoteValue.HALF));
        assertEquals(0, Beaming.beamCount(NoteValue.WHOLE));
    }

    @Test
    void beamCountGrowsWithShorterFigures() {
        assertEquals(1, Beaming.beamCount(NoteValue.EIGHTH));
        assertEquals(2, Beaming.beamCount(NoteValue.SIXTEENTH));
        assertEquals(3, Beaming.beamCount(NoteValue.THIRTY_SECOND));
        assertEquals(4, Beaming.beamCount(NoteValue.SIXTY_FOURTH));
    }

    @Test
    void fourQuarterNotesFormNoGroups() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                note(QUARTER), note(QUARTER), note(QUARTER), note(QUARTER)));
        assertTrue(Beaming.groupsOf(measure).isEmpty());
    }

    @Test
    void eightEighthNotesFormFourGroupsOfTwoOnePerBeat() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                note(EIGHTH), note(EIGHTH), note(EIGHTH), note(EIGHTH),
                note(EIGHTH), note(EIGHTH), note(EIGHTH), note(EIGHTH)));
        assertEquals(
                List.of(new BeamGroup(0, 1), new BeamGroup(2, 3), new BeamGroup(4, 5), new BeamGroup(6, 7)),
                Beaming.groupsOf(measure));
    }

    @Test
    void fourSixteenthsFillingTheFirstBeatFormOneGroup() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                note(SIXTEENTH), note(SIXTEENTH), note(SIXTEENTH), note(SIXTEENTH), note(HALF_DOTTED)));
        assertEquals(List.of(new BeamGroup(0, 3)), Beaming.groupsOf(measure));
    }

    @Test
    void aRestInTheMiddleCutsTheGroupInTwo() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                note(EIGHTH), rest(EIGHTH), note(EIGHTH), note(EIGHTH), rest(new Duration(NoteValue.HALF, false))));
        assertEquals(
                List.of(new BeamGroup(0, 0), new BeamGroup(2, 3)),
                Beaming.groupsOf(measure));
    }

    @Test
    void anEmptyMeasureWithOnlyARestFormsNoGroups() {
        Measure measure = Measure.empty(TimeSignature.fourFour(), QUARTER);
        assertTrue(Beaming.groupsOf(measure).isEmpty());
    }

    /**
     * El manual, linea 923: "es posible cambiar a mano las barras... usando el menu Nota".
     * Forzar el corte antes de un beat corta el grupo aunque el agrupamiento automatico -por
     * compartir el mismo beat principal- lo hubiera mantenido junto.
     */
    @Test
    void forcingABeamBreakSplitsAGroupThatWouldOtherwiseShareOneBeam() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                note(EIGHTH), note(EIGHTH, BeamBreak.FORCED), note(EIGHTH), note(EIGHTH),
                note(EIGHTH), note(EIGHTH), note(EIGHTH), note(EIGHTH)));
        assertEquals(
                List.of(new BeamGroup(0, 0), new BeamGroup(1, 1), new BeamGroup(2, 3),
                        new BeamGroup(4, 5), new BeamGroup(6, 7)),
                Beaming.groupsOf(measure));
    }

    /** La contraparte: impedir el corte une dos grupos que el agrupamiento automatico separaba. */
    @Test
    void preventingABeamBreakJoinsTwoGroupsAcrossABeatBoundary() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                note(EIGHTH), note(EIGHTH), note(EIGHTH, BeamBreak.PREVENTED), note(EIGHTH),
                note(EIGHTH), note(EIGHTH), note(EIGHTH), note(EIGHTH)));
        assertEquals(
                List.of(new BeamGroup(0, 3), new BeamGroup(4, 5), new BeamGroup(6, 7)),
                Beaming.groupsOf(measure));
    }

    /**
     * Forzar el corte en el primer beat con barra de un grupo no tiene nada que cortar todavia
     * -mismo caso limite que forzar un salto de linea en el primer compas de la partitura- asi
     * que el agrupamiento queda igual que el automatico.
     */
    @Test
    void forcingABeamBreakOnTheFirstBeamableBeatOfTheMeasureChangesNothing() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                note(EIGHTH, BeamBreak.FORCED), note(EIGHTH), note(EIGHTH), note(EIGHTH),
                note(EIGHTH), note(EIGHTH), note(EIGHTH), note(EIGHTH)));
        assertEquals(
                List.of(new BeamGroup(0, 1), new BeamGroup(2, 3), new BeamGroup(4, 5), new BeamGroup(6, 7)),
                Beaming.groupsOf(measure));
    }

    /**
     * Un silencio no se puede unir a una barra sin importar lo que pida el usuario: impedir el
     * corte solo tiene sentido sobre un beat que de por si es beameable.
     */
    @Test
    void preventingABeamBreakOnARestStillCutsTheGroup() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                note(EIGHTH), rest(EIGHTH, BeamBreak.PREVENTED), note(EIGHTH), note(EIGHTH),
                rest(new Duration(NoteValue.HALF, false))));
        assertEquals(
                List.of(new BeamGroup(0, 0), new BeamGroup(2, 3)),
                Beaming.groupsOf(measure));
    }
}
