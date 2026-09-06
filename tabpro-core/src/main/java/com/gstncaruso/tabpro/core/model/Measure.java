package com.gstncaruso.tabpro.core.model;

import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/** Un compas: su medida, sus atributos y sus dos voces. */
public record Measure(TimeSignature timeSignature, MeasureAttributes attributes, List<Voice> voices) {

    public Measure {
        if (voices.size() != VoicePart.values().length) {
            throw new IllegalArgumentException("un compas tiene exactamente dos voces");
        }
        if (voices.getFirst().isUnused()) {
            throw new IllegalArgumentException("la voz principal necesita al menos un beat");
        }
        voices = List.copyOf(voices);
    }

    public Measure(TimeSignature timeSignature, List<Beat> leadBeats) {
        this(timeSignature, MeasureAttributes.plain(), List.of(new Voice(leadBeats), Voice.unused()));
    }

    public static Measure empty(TimeSignature timeSignature, Duration restDuration) {
        return new Measure(timeSignature, List.of(Beat.rest(restDuration)));
    }

    public Voice voice(VoicePart part) {
        return voices.get(part.ordinal());
    }

    public Voice lead() {
        return voice(VoicePart.LEAD);
    }

    public boolean usesTwoVoices() {
        return !voice(VoicePart.BASS).isUnused();
    }

    /** Los beats de la voz principal, que es con la que se trabaja por defecto. */
    public List<Beat> beats() {
        return lead().beats();
    }

    public Beat beat(int index) {
        return lead().beat(index);
    }

    public long durationTicks() {
        return voices.stream().mapToLong(Voice::durationTicks).max().orElse(0);
    }

    public boolean isComplete() {
        return durationTicks() == timeSignature.ticksPerMeasure();
    }

    public boolean isTooShort() {
        return durationTicks() < timeSignature.ticksPerMeasure();
    }

    public boolean isTooLong() {
        return durationTicks() > timeSignature.ticksPerMeasure();
    }

    public boolean hasNotes() {
        return voices.stream().anyMatch(Voice::hasNotes);
    }

    public Measure withVoice(VoicePart part, Voice voice) {
        List<Voice> updated = new ArrayList<>(voices);
        updated.set(part.ordinal(), voice);
        return new Measure(timeSignature, attributes, updated);
    }

    public Measure mappingVoice(VoicePart part, UnaryOperator<Voice> change) {
        return withVoice(part, change.apply(voice(part)));
    }

    public Measure withBeat(int index, Beat beat) {
        return withBeat(VoicePart.LEAD, index, beat);
    }

    public Measure withBeat(VoicePart part, int index, Beat beat) {
        return mappingVoice(part, voice -> voice.withBeat(index, beat));
    }

    public Measure withBeatInsertedAt(int index, Beat beat) {
        return withBeatInsertedAt(VoicePart.LEAD, index, beat);
    }

    public Measure withBeatInsertedAt(VoicePart part, int index, Beat beat) {
        return mappingVoice(part, voice -> voice.withBeatInsertedAt(index, beat));
    }

    public Measure withoutBeatAt(int index) {
        return withoutBeatAt(VoicePart.LEAD, index);
    }

    public Measure withoutBeatAt(VoicePart part, int index) {
        return mappingVoice(part, voice -> voice.withoutBeatAt(index));
    }

    public Measure withTimeSignature(TimeSignature timeSignature) {
        return new Measure(timeSignature, attributes, voices);
    }

    public Measure withAttributes(MeasureAttributes attributes) {
        return new Measure(timeSignature, attributes, voices);
    }

    public Measure mappingAttributes(UnaryOperator<MeasureAttributes> change) {
        return withAttributes(change.apply(attributes));
    }

    /** El mismo compas sin notas, respetando su medida y sus atributos. */
    public Measure emptied() {
        return new Measure(timeSignature, attributes,
                List.of(Voice.restingFor(Duration.quarter()), Voice.unused()));
    }
}
