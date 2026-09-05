package com.gstncaruso.tabpro.core.playback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Voice;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import com.gstncaruso.tabpro.core.model.bars.TripletFeel;
import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import com.gstncaruso.tabpro.core.model.effects.BendType;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.GraceNote;
import com.gstncaruso.tabpro.core.model.effects.GraceTransition;
import com.gstncaruso.tabpro.core.model.effects.NoteEffects;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.model.effects.SlideType;
import com.gstncaruso.tabpro.core.model.effects.Stroke;
import com.gstncaruso.tabpro.core.model.effects.StrokeDirection;
import com.gstncaruso.tabpro.core.model.effects.TremoloPicking;
import com.gstncaruso.tabpro.core.model.effects.Trill;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Los efectos que se arman con varias notas o con el compas entero: bends,
 * slides, ligados, trino, tremolo picking, rasgueo, notas de adorno, fade in
 * y triplet feel, todos vistos a traves del Timeline.
 */
class TrackRendererEffectsTest {

    private static final Duration QUARTER = Duration.quarter();

    @Test
    void unBendCurvaLaAlturaSegunSuFormaEnTodaLaNota() {
        Bend bend = Bend.of(BendType.BEND, 4); // sube un tono
        Note bent = new Note(1, 0).withBend(bend);
        Score score = scoreWithLeadBeats(Beat.of(QUARTER, bent));

        ScheduledNote note = notesOf(score).get(0);

        assertEquals(0.0, note.bend().semitonesAt(0));
        assertEquals(2.0, note.bend().semitonesAt(note.durationTicks()));
    }

    @Test
    void unHammerOnNoAtacaYSaltaDeAlturaEnElLimite() {
        Note first = new Note(1, 0);
        Note hammered = new Note(1, 2).toggling(Ornament.HAMMER_ON_PULL_OFF);
        Score score = scoreWithLeadBeats(Beat.of(QUARTER, first), Beat.of(QUARTER, hammered));

        List<ScheduledNote> notes = notesOf(score);

        assertEquals(1, notes.size());
        ScheduledNote merged = notes.get(0);
        assertEquals(QUARTER.ticks() * 2, merged.durationTicks());
        assertEquals(0.0, merged.bend().semitonesAt(0));
        assertEquals(2.0, merged.bend().semitonesAt(QUARTER.ticks()));
    }

    @Test
    void unSlideLegatoNoAtacaLaSegundaNotaYDeslizaHaciaElla() {
        Note first = new Note(1, 0).withSlide(SlideType.LEGATO);
        Note second = new Note(1, 2);
        Score score = scoreWithLeadBeats(Beat.of(QUARTER, first), Beat.of(QUARTER, second));

        List<ScheduledNote> notes = notesOf(score);

        assertEquals(1, notes.size());
        assertEquals(QUARTER.ticks() * 2, notes.get(0).durationTicks());
        assertEquals(2.0, notes.get(0).bend().semitonesAt(QUARTER.ticks()));
    }

    @Test
    void unSlideShiftSiAtacaLaNotaDeDestino() {
        Note first = new Note(1, 0).withSlide(SlideType.SHIFT);
        Note second = new Note(1, 2);
        Score score = scoreWithLeadBeats(Beat.of(QUARTER, first), Beat.of(QUARTER, second));

        List<ScheduledNote> notes = notesOf(score);

        assertEquals(2, notes.size());
    }

    @Test
    void unSlideEntrandoDesdeAbajoArrancaMasGraveYSubeAlPrincipio() {
        Note note = new Note(1, 5).withSlide(SlideType.IN_FROM_BELOW);
        Score score = scoreWithLeadBeats(Beat.of(QUARTER, note));

        ScheduledNote scheduled = notesOf(score).get(0);

        assertTrue(scheduled.bend().semitonesAt(0) < 0);
        assertEquals(0.0, scheduled.bend().semitonesAt(scheduled.durationTicks()));
    }

    @Test
    void unSlideSaliendoHaciaArribaTerminaSubiendo() {
        Note note = new Note(1, 5).withSlide(SlideType.OUT_UPWARDS);
        Score score = scoreWithLeadBeats(Beat.of(QUARTER, note));

        ScheduledNote scheduled = notesOf(score).get(0);

        assertEquals(0.0, scheduled.bend().semitonesAt(0));
        assertTrue(scheduled.bend().semitonesAt(scheduled.durationTicks()) > 0);
    }

    @Test
    void unTrinoAlternaLasDosAlturas() {
        Trill trill = new Trill(2, NoteValue.SIXTEENTH);
        Note note = new Note(1, 0).withEffects(NoteEffects.none().withTrill(trill));
        Score score = scoreWithLeadBeats(Beat.of(QUARTER, note));

        List<ScheduledNote> notes = notesOf(score);

        assertTrue(notes.size() > 1, "el trino tiene que sonar mas de una vez");
        assertTrue(notes.get(0).pitch().midiNumber() != notes.get(1).pitch().midiNumber());
        long totalTicks = notes.stream().mapToLong(ScheduledNote::durationTicks).sum();
        assertEquals(QUARTER.ticks(), totalTicks);
    }

    @Test
    void unTremoloPickingRepiteLaMismaAltura() {
        TremoloPicking tremolo = TremoloPicking.at(NoteValue.SIXTEENTH);
        Note note = new Note(1, 0).withEffects(NoteEffects.none().withTremoloPicking(tremolo));
        Score score = scoreWithLeadBeats(Beat.of(QUARTER, note));

        List<ScheduledNote> notes = notesOf(score);

        assertTrue(notes.size() > 1);
        assertTrue(notes.stream().allMatch(n -> n.pitch().equals(notes.get(0).pitch())));
    }

    @Test
    void unRasgueoHaciaAbajoArrancaPorLaCuerdaMasGrave() {
        Beat chord = Beat.of(QUARTER, new Note(1, 0), new Note(6, 0))
                .withEffects(BeatEffects.none().withStroke(Stroke.of(StrokeDirection.DOWN)));
        Score score = scoreWithLeadBeats(chord);

        List<ScheduledNote> notes = notesOf(score);

        assertEquals(2, notes.size());
        ScheduledNote lowestFirst = notes.stream().min((a, b) -> Long.compare(a.startTick(), b.startTick())).get();
        assertEquals(new Pitch(40), lowestFirst.pitch()); // cuerda 6, mas grave
        assertTrue(notes.get(0).startTick() != notes.get(1).startTick());
    }

    @Test
    void unaNotaDeAdornoSobreElBeatOcupaElComienzo() {
        Note note = new Note(1, 5).withEffects(NoteEffects.none()
                .withGrace(new GraceNote(3, NoteValue.THIRTY_SECOND,
                        Dynamic.defaultDynamic(),
                        GraceTransition.NONE, true, false)));
        Score score = scoreWithLeadBeats(Beat.of(QUARTER, note));

        List<ScheduledNote> notes = notesOf(score);

        assertEquals(2, notes.size());
        ScheduledNote first = notes.get(0);
        ScheduledNote second = notes.get(1);
        assertEquals(0, first.startTick());
        assertTrue(second.startTick() > 0, "la nota principal arranca despues del adorno");
    }

    @Test
    void unaNotaDeAdornoFueraDelBeatSuenaAntes() {
        Note note = new Note(1, 5).withEffects(NoteEffects.none()
                .withGrace(new GraceNote(3, NoteValue.THIRTY_SECOND,
                        Dynamic.defaultDynamic(),
                        GraceTransition.NONE, false, false)));
        Score score = scoreWithLeadBeats(Beat.of(QUARTER, note));

        List<ScheduledNote> notes = notesOf(score);

        assertEquals(2, notes.size());
        ScheduledNote grace = notes.get(0);
        ScheduledNote main = notes.get(1);
        assertTrue(grace.startTick() < 0, "el adorno le pide prestado tiempo al compas anterior");
        assertEquals(0, main.startTick());
    }

    @Test
    void unFadeInQuedaMarcadoEnLaNota() {
        Beat beat = Beat.of(QUARTER, new Note(1, 0))
                .withEffects(BeatEffects.none().withFadeIn(true));
        Score score = scoreWithLeadBeats(beat);

        ScheduledNote note = notesOf(score).get(0);

        assertTrue(note.fadeIn());
    }

    @Test
    void unaPalancaCurvaTodasLasNotasDelBeat() {
        Bend tremoloBar = Bend.of(BendType.BEND_RELEASE, 4);
        Beat beat = Beat.of(QUARTER, new Note(1, 0))
                .withEffects(BeatEffects.none().withTremoloBar(tremoloBar));
        Score score = scoreWithLeadBeats(beat);

        ScheduledNote note = notesOf(score).get(0);

        assertFalse(note.bend().isFlat());
    }

    @Test
    void elTripletFeelDelCompasSwinguaLasCorcheas() {
        Duration eighth = new Duration(NoteValue.EIGHTH, false);
        Measure measure = new Measure(TimeSignature.fourFour(),
                MeasureAttributes.plain().withTripletFeel(TripletFeel.EIGHTH),
                List.of(new Voice(List.of(
                        Beat.of(eighth, new Note(1, 0)), Beat.of(eighth, new Note(1, 1)))),
                        Voice.unused()));
        Track track = Track.standardGuitar("Guitarra").withMeasure(0, measure);
        Score score = Score.blank().withTrack(0, track);

        List<ScheduledNote> notes = notesOf(score);

        long pair = eighth.ticks() * 2;
        assertEquals(pair * 2 / 3, notes.get(1).startTick());
    }

    private List<ScheduledNote> notesOf(Score score) {
        return Timeline.of(score).tracks().get(0).notes();
    }

    private Score scoreWithLeadBeats(Beat... beatsInOrder) {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beatsInOrder));
        Track track = Track.standardGuitar("Guitarra").withMeasure(0, measure);
        return Score.blank().withTrack(0, track);
    }
}
