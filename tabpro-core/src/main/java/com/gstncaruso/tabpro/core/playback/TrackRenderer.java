package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Voice;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Convierte una pista en su linea de tiempo: recorre el orden real de los
 * compases y hace sonar las dos voces, resolviendo las ligaduras, los
 * ligados y los armonicos nota por nota.
 */
final class TrackRenderer {

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
        long tick = 0;
        for (int step = 0; step < order.size(); step++) {
            int measureIndex = order.measureAt(step);
            if (measureIndex >= track.measureCount()) {
                continue;
            }
            Measure measure = track.measure(measureIndex);
            renderVoice(measure.voice(VoicePart.LEAD), tick, measure, measureIndex, lead, true);
            renderVoice(measure.voice(VoicePart.BASS), tick, measure, measureIndex, bass, false);
            tick += measure.durationTicks();
        }
        var channel = track.channel();
        int volume = audible ? channel.volume() : 0;
        return new TrackTimeline(channel.program(), volume, channel.pan(), track.isPercussion(), notes, beats);
    }

    private void renderVoice(
            Voice voice, long measureStartTick, Measure measure, int measureIndex, VoiceCursor cursor,
            boolean collectBeatMarkers) {
        long tick = measureStartTick;
        List<Beat> voiceBeats = voice.beats();
        for (int b = 0; b < voiceBeats.size(); b++) {
            Beat beat = voiceBeats.get(b);
            long beatTicks = beat.duration().ticks();
            if (collectBeatMarkers) {
                beats.add(new ScheduledBeat(tick, measureIndex, b));
            }
            renderBeat(beat, tick, beatTicks, measure.attributes(), cursor);
            tick += beatTicks;
        }
    }

    private void renderBeat(Beat beat, long tick, long beatTicks, MeasureAttributes attributes, VoiceCursor cursor) {
        for (Note note : beat.notes()) {
            renderNote(note, tick, beatTicks, cursor);
        }
    }

    private void renderNote(Note note, long tick, long beatTicks, VoiceCursor cursor) {
        Pitch pitch = pitchOf(note);
        long soundTicks = Math.round(beatTicks * note.soundLength());
        boolean continuesPrevious = note.tied() || note.has(Ornament.HAMMER_ON_PULL_OFF);

        if (continuesPrevious && cursor.hasOpenNote(note.string())) {
            extendOpenNote(cursor, note.string(), soundTicks, pitch, note.has(Ornament.HAMMER_ON_PULL_OFF));
            return;
        }

        ScheduledNote scheduled = new ScheduledNote(tick, soundTicks, pitch, note.velocity(), bendOf(note, soundTicks), false);
        int index = notes.size();
        notes.add(scheduled);
        cursor.open(note.string(), index);
    }

    private void extendOpenNote(VoiceCursor cursor, int string, long extraTicks, Pitch newPitch, boolean isHammer) {
        int index = cursor.openIndexOf(string);
        ScheduledNote open = notes.get(index);
        long newDuration = open.durationTicks() + extraTicks;
        PitchTrajectory bend = open.bend();
        if (isHammer) {
            double jumpSemitones = newPitch.midiNumber() - open.pitch().midiNumber();
            bend = bend.withJumpAt(open.durationTicks(), jumpSemitones);
        }
        notes.set(index, open.withDurationTicks(newDuration).withBend(bend));
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

    /** El estado de una voz mientras se la recorre: que nota sigue abierta en cada cuerda. */
    private static final class VoiceCursor {
        private final Map<Integer, Integer> openIndexByString = new HashMap<>();

        boolean hasOpenNote(int string) {
            return openIndexByString.containsKey(string);
        }

        int openIndexOf(int string) {
            return openIndexByString.get(string);
        }

        void open(int string, int index) {
            openIndexByString.put(string, index);
        }
    }
}
