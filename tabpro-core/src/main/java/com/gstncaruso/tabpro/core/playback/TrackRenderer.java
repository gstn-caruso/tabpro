package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import com.gstncaruso.tabpro.core.model.effects.GraceNote;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.model.effects.SlideType;
import com.gstncaruso.tabpro.core.model.effects.Stroke;
import com.gstncaruso.tabpro.core.model.effects.TremoloPicking;
import com.gstncaruso.tabpro.core.model.effects.Trill;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntFunction;

/**
 * Convierte una pista en su linea de tiempo: recorre el orden real de los
 * compases y hace sonar las dos voces, resolviendo ligaduras, ligados,
 * slides, armonicos, trino, tremolo picking, rasgueo, notas de adorno y el
 * triplet feel del compas.
 */
final class TrackRenderer {

    /** Cuanto tarda un slide indefinido en llegar a destino: una semicorchea. */
    private static final long SLIDE_RAMP_TICKS = new Duration(NoteValue.SIXTEENTH, false).ticks();

    /** Cuanto se aparta la altura en un slide que entra o sale sin nota de destino. */
    private static final double SLIDE_INOUT_SEMITONES = 3.0;

    private static final double VIBRATO_DEPTH_SEMITONES = 0.4;
    private static final double WIDE_VIBRATO_DEPTH_SEMITONES = 1.0;
    private static final long VIBRATO_PERIOD_TICKS = new Duration(NoteValue.EIGHTH, false).ticks();

    private final Track track;
    private final PlayOrder order;
    private final boolean audible;

    private final List<ScheduledNote> notes = new ArrayList<>();
    private final List<ScheduledBeat> beats = new ArrayList<>();
    private final VoiceCursor lead = new VoiceCursor();
    private final VoiceCursor bass = new VoiceCursor();

    TrackRenderer(Track track, PlayOrder order, boolean audible) {
        this.track = track;
        this.order = order;
        this.audible = audible;
    }

    TrackTimeline render() {
        for (TimedBeat timed : TrackClock.of(track, order)) {
            if (timed.voice() == VoicePart.LEAD) {
                beats.add(new ScheduledBeat(timed.tick(), timed.measureIndex(), timed.beatIndex()));
            }
            renderBeat(timed.beat(), timed.tick(), timed.durationTicks(), cursorOf(timed.voice()));
        }
        Channel channel = track.channel();
        int volume = audible ? channel.volume() : 0;
        return new TrackTimeline(
                channel.program(), volume, channel.pan(), track.isPercussion(), channel.port(), notes, beats, List.of());
    }

    private VoiceCursor cursorOf(VoicePart part) {
        return part == VoicePart.LEAD ? lead : bass;
    }

    private void renderBeat(Beat beat, long tick, long beatTicks, VoiceCursor cursor) {
        List<Note> ordered = orderForStroke(beat);
        long strokeDelay = beat.effects().stroke().map(Stroke::delayTicks).orElse(0L);
        for (int i = 0; i < ordered.size(); i++) {
            Note note = ordered.get(i);
            long noteTick = tick + i * strokeDelay;
            long noteBeatTicks = beatTicks;

            Optional<GraceNote> grace = note.effects().grace();
            if (grace.isPresent()) {
                long graceTicks = new Duration(grace.get().duration(), false).ticks();
                if (grace.get().onBeat()) {
                    scheduleGrace(note, grace.get(), noteTick, graceTicks);
                    noteTick += graceTicks;
                    noteBeatTicks = Math.max(1, noteBeatTicks - graceTicks);
                } else {
                    scheduleGrace(note, grace.get(), noteTick - graceTicks, graceTicks);
                }
            }

            renderNote(note, noteTick, noteBeatTicks, beat, cursor);
        }
    }

    private List<Note> orderForStroke(Beat beat) {
        List<Note> ordered = new ArrayList<>(beat.notes());
        beat.effects().stroke().ifPresent(stroke -> {
            Comparator<Note> byString = Comparator.comparingInt(Note::string);
            ordered.sort(stroke.direction().startsAtTheLowestString() ? byString.reversed() : byString);
        });
        return ordered;
    }

    private void renderNote(Note note, long tick, long beatTicks, Beat beat, VoiceCursor cursor) {
        if (note.effects().trill().isPresent()) {
            cursor.clearPendingLegato(note.string());
            scheduleTrill(note, tick, beatTicks, note.effects().trill().get());
            return;
        }
        if (note.effects().tremoloPicking().isPresent()) {
            cursor.clearPendingLegato(note.string());
            scheduleTremoloPicking(note, tick, beatTicks, note.effects().tremoloPicking().get());
            return;
        }

        boolean pendingLegato = cursor.hasPendingLegato(note.string());
        cursor.clearPendingLegato(note.string());
        boolean isHammer = note.has(Ornament.HAMMER_ON_PULL_OFF);
        boolean continuesPrevious = (note.tied() || isHammer || pendingLegato) && cursor.hasOpenNote(note.string());

        Pitch pitch = pitchOf(note);
        long soundTicks = Math.round(beatTicks * note.soundLength());

        if (continuesPrevious) {
            extendOpenNote(cursor, note.string(), soundTicks, pitch, isHammer, pendingLegato && !isHammer && !note.tied());
        } else {
            PitchTrajectory bend = bendOf(note, soundTicks)
                    .plus(slideShapeOf(note, soundTicks))
                    .plus(beatEffectsBend(beat, beatTicks))
                    .plus(vibratoOf(note, soundTicks));
            ScheduledNote scheduled = new ScheduledNote(tick, soundTicks, pitch, note.velocity(), bend, beat.effects().fadeIn());
            int index = notes.size();
            notes.add(scheduled);
            cursor.open(note.string(), index);
        }

        if (targetsNextNoteWithoutRepicking(note)) {
            cursor.markPendingLegato(note.string());
        }
    }

    private boolean targetsNextNoteWithoutRepicking(Note note) {
        return note.effects().slide()
                .map(slide -> slide.towardsTheNextNote() && !slide.picksTheDestination())
                .orElse(false);
    }

    private void extendOpenNote(
            VoiceCursor cursor, int string, long extraTicks, Pitch newPitch, boolean isHammer, boolean isLegato) {
        int index = cursor.openIndexOf(string);
        ScheduledNote open = notes.get(index);
        long boundary = open.durationTicks();
        long newDuration = boundary + extraTicks;
        PitchTrajectory bend = open.bend();
        if (isHammer) {
            double delta = newPitch.midiNumber() - open.pitch().midiNumber();
            bend = bend.withJumpAt(boundary, delta);
        } else if (isLegato) {
            double delta = newPitch.midiNumber() - open.pitch().midiNumber();
            bend = bend.rampingTo(boundary, delta, SLIDE_RAMP_TICKS);
        }
        notes.set(index, open.withDurationTicks(newDuration).withBend(bend));
    }

    private void scheduleTrill(Note note, long tick, long beatTicks, Trill trill) {
        long soundTicks = Math.round(beatTicks * note.soundLength());
        long unit = new Duration(trill.speed(), false).ticks();
        Pitch base = pitchOf(note);
        Pitch alternate = pitchOf(note.withFret(trill.fret()));
        scheduleAlternating(note, tick, soundTicks, unit, index -> index % 2 == 0 ? base : alternate);
    }

    private void scheduleTremoloPicking(Note note, long tick, long beatTicks, TremoloPicking tremolo) {
        long soundTicks = Math.round(beatTicks * note.soundLength());
        long unit = new Duration(tremolo.speed(), false).ticks();
        Pitch pitch = pitchOf(note);
        scheduleAlternating(note, tick, soundTicks, unit, index -> pitch);
    }

    private void scheduleAlternating(Note note, long tick, long soundTicks, long unit, IntFunction<Pitch> pitchAt) {
        int count = (int) Math.max(1, soundTicks / Math.max(1, unit));
        long remaining = soundTicks;
        long cursorTick = tick;
        for (int i = 0; i < count; i++) {
            long duration = (i == count - 1) ? remaining : unit;
            notes.add(new ScheduledNote(cursorTick, duration, pitchAt.apply(i), note.velocity(), PitchTrajectory.flat(), false));
            cursorTick += duration;
            remaining -= duration;
        }
    }

    private void scheduleGrace(Note note, GraceNote grace, long tick, long graceTicks) {
        Pitch pitch = track.isPercussion() ? new Pitch(grace.fret()) : track.pitchOf(new Note(note.string(), grace.fret()));
        long duration = grace.dead() ? Math.max(1, Math.round(graceTicks * 0.1)) : graceTicks;
        notes.add(new ScheduledNote(tick, duration, pitch, grace.dynamic().intensity(), PitchTrajectory.flat(), false));
    }

    private Pitch pitchOf(Note note) {
        if (track.isPercussion()) {
            return new Pitch(note.fret());
        }
        Pitch fretted = track.pitchOf(note);
        return note.effects().harmonic()
                .map(harmonic -> HarmonicPitch.of(harmonic, track.tuning().pitchOfString(note.string()), fretted, note.fret()))
                .orElse(fretted);
    }

    private PitchTrajectory bendOf(Note note, long durationTicks) {
        return note.effects().bend().map(bend -> PitchTrajectory.of(bend, durationTicks)).orElse(PitchTrajectory.flat());
    }

    /** El slide sin nota de destino define su propia curva; el que apunta a la siguiente nota se resuelve al fundirla. */
    private PitchTrajectory slideShapeOf(Note note, long soundTicks) {
        return note.effects().slide()
                .filter(slide -> !slide.towardsTheNextNote())
                .map(slide -> shapeFor(slide, soundTicks))
                .orElse(PitchTrajectory.flat());
    }

    private PitchTrajectory shapeFor(SlideType slide, long soundTicks) {
        long ramp = Math.min(SLIDE_RAMP_TICKS, soundTicks);
        return switch (slide) {
            case IN_FROM_BELOW -> PitchTrajectory.ramp(0, -SLIDE_INOUT_SEMITONES, ramp, 0);
            case IN_FROM_ABOVE -> PitchTrajectory.ramp(0, SLIDE_INOUT_SEMITONES, ramp, 0);
            case OUT_DOWNWARDS -> PitchTrajectory.ramp(Math.max(0, soundTicks - ramp), 0, soundTicks, -SLIDE_INOUT_SEMITONES);
            case OUT_UPWARDS -> PitchTrajectory.ramp(Math.max(0, soundTicks - ramp), 0, soundTicks, SLIDE_INOUT_SEMITONES);
            default -> PitchTrajectory.flat();
        };
    }

    private PitchTrajectory beatEffectsBend(Beat beat, long beatTicks) {
        BeatEffects effects = beat.effects();
        PitchTrajectory result = PitchTrajectory.flat();
        if (effects.tremoloBar().isPresent()) {
            result = result.plus(PitchTrajectory.of(effects.tremoloBar().get(), beatTicks));
        }
        if (effects.wideVibrato()) {
            result = result.plus(PitchTrajectory.vibrato(beatTicks, WIDE_VIBRATO_DEPTH_SEMITONES, VIBRATO_PERIOD_TICKS));
        }
        return result;
    }

    private PitchTrajectory vibratoOf(Note note, long soundTicks) {
        return note.has(Ornament.VIBRATO)
                ? PitchTrajectory.vibrato(soundTicks, VIBRATO_DEPTH_SEMITONES, VIBRATO_PERIOD_TICKS)
                : PitchTrajectory.flat();
    }

    /** El estado de una voz mientras se la recorre: que nota sigue abierta en cada cuerda. */
    private static final class VoiceCursor {
        private final Map<Integer, Integer> openIndexByString = new HashMap<>();
        private final Set<Integer> pendingLegatoStrings = new HashSet<>();

        boolean hasOpenNote(int string) {
            return openIndexByString.containsKey(string);
        }

        int openIndexOf(int string) {
            return openIndexByString.get(string);
        }

        void open(int string, int index) {
            openIndexByString.put(string, index);
        }

        boolean hasPendingLegato(int string) {
            return pendingLegatoStrings.contains(string);
        }

        void markPendingLegato(int string) {
            pendingLegatoStrings.add(string);
        }

        void clearPendingLegato(int string) {
            pendingLegatoStrings.remove(string);
        }
    }
}
