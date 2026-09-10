package com.gstncaruso.tabpro.core.harmony;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record Chord(PitchClass root, ChordType type, PitchClass bass) {

    public Chord {
        Objects.requireNonNull(root, "a chord needs a root");
        Objects.requireNonNull(type, "a chord needs a type");
        Objects.requireNonNull(bass, "a chord needs a bass note, even if it is the root");
    }

    public static Chord of(PitchClass root, ChordType type) {
        return new Chord(root, type, root);
    }

    public static Chord inverted(PitchClass root, ChordType type, PitchClass bass) {
        return new Chord(root, type, bass);
    }

    public boolean isInverted() {
        return !bass.equals(root);
    }

    public List<PitchClass> pitchClasses() {
        return type.tones().stream().map(tone -> tone.interval().from(root)).toList();
    }

    public Set<Integer> essentialSemitones() {
        return essentialSemitones(Set.of());
    }

    public Set<Integer> essentialSemitones(Set<Interval> omittedTones) {
        Set<Integer> semitones = new LinkedHashSet<>();
        type.tones().stream()
                .filter(ChordTone::essential)
                .filter(tone -> !omittedTones.contains(tone.interval()))
                .forEach(tone -> semitones.add(tone.interval().from(root).semitone()));
        semitones.add(bass.semitone());
        return semitones;
    }

    public Set<Integer> formulaSemitones() {
        Set<Integer> semitones = new LinkedHashSet<>();
        type.tones().forEach(tone -> semitones.add(tone.interval().from(root).semitone()));
        semitones.add(bass.semitone());
        return semitones;
    }

    public String name() {
        return name(true);
    }

    public String name(boolean showBassWhenInverted) {
        String base = root.name() + type.suffix();
        return isInverted() && showBassWhenInverted ? base + "/" + bass.name() : base;
    }

    @Override
    public String toString() {
        return name();
    }
}
