package com.gstncaruso.tabpro.core.harmony;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Un acorde: su fundamental, su tipo y el bajo con que suena (que puede ser una inversion
 * de una de sus propias notas, o un bajo ajeno al acorde).
 */
public record Chord(PitchClass root, ChordType type, PitchClass bass) {

    public Chord {
        Objects.requireNonNull(root, "un acorde necesita una fundamental");
        Objects.requireNonNull(type, "un acorde necesita un tipo");
        Objects.requireNonNull(bass, "un acorde necesita un bajo, aunque sea la fundamental");
    }

    /** Un acorde sin inversion: el bajo es la propia fundamental. */
    public static Chord of(PitchClass root, ChordType type) {
        return new Chord(root, type, root);
    }

    /** Un acorde invertido o con bajo indicado: el bajo puede ser cualquier nota, ajena o no al acorde. */
    public static Chord inverted(PitchClass root, ChordType type, PitchClass bass) {
        return new Chord(root, type, bass);
    }

    public boolean isInverted() {
        return !bass.equals(root);
    }

    /** Las notas que forman el acorde, deletreadas desde la fundamental, sin el bajo indicado. */
    public List<PitchClass> pitchClasses() {
        return type.tones().stream().map(tone -> tone.interval().from(root)).toList();
    }

    /** Los semitonos que hacen falta si o si: los tonos imprescindibles del acorde, mas el bajo. */
    public Set<Integer> essentialSemitones() {
        Set<Integer> semitones = new LinkedHashSet<>();
        type.tones().stream().filter(ChordTone::essential).forEach(tone -> semitones.add(tone.interval().from(root).semitone()));
        semitones.add(bass.semitone());
        return semitones;
    }

    /** Todos los semitonos que puede sonar el acorde: los tonos de la formula (esenciales u opcionales) mas el bajo. */
    public Set<Integer> formulaSemitones() {
        Set<Integer> semitones = new LinkedHashSet<>();
        type.tones().forEach(tone -> semitones.add(tone.interval().from(root).semitone()));
        semitones.add(bass.semitone());
        return semitones;
    }

    /** Como se escribe: la fundamental, el sufijo del tipo y, si esta invertido, "/bajo". */
    public String name() {
        String base = root.name() + type.suffix();
        return isInverted() ? base + "/" + bass.name() : base;
    }

    @Override
    public String toString() {
        return name();
    }
}
