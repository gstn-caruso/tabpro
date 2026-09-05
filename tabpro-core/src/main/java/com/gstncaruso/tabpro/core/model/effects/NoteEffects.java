package com.gstncaruso.tabpro.core.model.effects;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/** Todo lo que se le puede pedir a una nota mas alla de su traste. */
public record NoteEffects(
        Set<Ornament> ornaments,
        Dynamic dynamic,
        Optional<Bend> bend,
        Optional<SlideType> slide,
        Optional<HarmonicType> harmonic,
        Optional<Trill> trill,
        Optional<TremoloPicking> tremoloPicking,
        Optional<GraceNote> grace,
        Optional<Finger> leftHand,
        Optional<Finger> rightHand) {

    private static final NoteEffects NONE = new NoteEffects(
            EnumSet.noneOf(Ornament.class), Dynamic.defaultDynamic(),
            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

    public NoteEffects {
        ornaments = Set.copyOf(ornaments);
    }

    public static NoteEffects none() {
        return NONE;
    }

    public boolean isEmpty() {
        return equals(NONE);
    }

    public boolean has(Ornament ornament) {
        return ornaments.contains(ornament);
    }

    public NoteEffects toggling(Ornament ornament) {
        return has(ornament) ? without(ornament) : with(ornament);
    }

    public NoteEffects with(Ornament ornament) {
        EnumSet<Ornament> updated = copyOfOrnaments();
        updated.add(ornament);
        updated.removeAll(contradictionsOf(ornament));
        return withOrnaments(updated);
    }

    public NoteEffects without(Ornament ornament) {
        EnumSet<Ornament> updated = copyOfOrnaments();
        updated.remove(ornament);
        return withOrnaments(updated);
    }

    /** Los adornos que no pueden convivir con este, porque piden lo contrario. */
    private static Set<Ornament> contradictionsOf(Ornament ornament) {
        return switch (ornament) {
            case GHOST -> EnumSet.of(Ornament.ACCENTED, Ornament.HEAVY_ACCENTED);
            case ACCENTED -> EnumSet.of(Ornament.GHOST, Ornament.HEAVY_ACCENTED);
            case HEAVY_ACCENTED -> EnumSet.of(Ornament.GHOST, Ornament.ACCENTED);
            case LET_RING -> EnumSet.of(Ornament.PALM_MUTE, Ornament.STACCATO);
            case PALM_MUTE, STACCATO -> EnumSet.of(Ornament.LET_RING);
            default -> EnumSet.noneOf(Ornament.class);
        };
    }

    /** Cuan fuerte suena esta nota, contando los acentos y las notas fantasma. */
    public Velocity velocity() {
        Velocity intensity = dynamic.intensity();
        if (has(Ornament.HEAVY_ACCENTED)) {
            return intensity.accented().accented();
        }
        if (has(Ornament.ACCENTED)) {
            return intensity.accented();
        }
        if (has(Ornament.GHOST) || has(Ornament.DEAD)) {
            return intensity.ghosted();
        }
        return intensity;
    }

    /** Que fraccion de su figura suena la nota. */
    public double soundLength() {
        if (has(Ornament.DEAD)) {
            return 0.1;
        }
        if (has(Ornament.STACCATO)) {
            return 0.5;
        }
        if (has(Ornament.PALM_MUTE)) {
            return 0.4;
        }
        if (has(Ornament.LET_RING)) {
            return 2.0;
        }
        return 1.0;
    }

    public NoteEffects withDynamic(Dynamic dynamic) {
        return new NoteEffects(ornaments, dynamic, bend, slide, harmonic, trill, tremoloPicking, grace, leftHand, rightHand);
    }

    public NoteEffects withBend(Bend bend) {
        return new NoteEffects(ornaments, dynamic, Optional.of(bend), slide, harmonic, trill, tremoloPicking, grace, leftHand, rightHand);
    }

    public NoteEffects withoutBend() {
        return new NoteEffects(ornaments, dynamic, Optional.empty(), slide, harmonic, trill, tremoloPicking, grace, leftHand, rightHand);
    }

    public NoteEffects withSlide(SlideType slide) {
        return new NoteEffects(ornaments, dynamic, bend, Optional.of(slide), harmonic, trill, tremoloPicking, grace, leftHand, rightHand);
    }

    public NoteEffects withoutSlide() {
        return new NoteEffects(ornaments, dynamic, bend, Optional.empty(), harmonic, trill, tremoloPicking, grace, leftHand, rightHand);
    }

    public NoteEffects withHarmonic(HarmonicType harmonic) {
        return new NoteEffects(ornaments, dynamic, bend, slide, Optional.of(harmonic), trill, tremoloPicking, grace, leftHand, rightHand);
    }

    public NoteEffects withoutHarmonic() {
        return new NoteEffects(ornaments, dynamic, bend, slide, Optional.empty(), trill, tremoloPicking, grace, leftHand, rightHand);
    }

    public NoteEffects withTrill(Trill trill) {
        return new NoteEffects(ornaments, dynamic, bend, slide, harmonic, Optional.of(trill), tremoloPicking, grace, leftHand, rightHand);
    }

    public NoteEffects withoutTrill() {
        return new NoteEffects(ornaments, dynamic, bend, slide, harmonic, Optional.empty(), tremoloPicking, grace, leftHand, rightHand);
    }

    public NoteEffects withTremoloPicking(TremoloPicking tremoloPicking) {
        return new NoteEffects(ornaments, dynamic, bend, slide, harmonic, trill, Optional.of(tremoloPicking), grace, leftHand, rightHand);
    }

    public NoteEffects withoutTremoloPicking() {
        return new NoteEffects(ornaments, dynamic, bend, slide, harmonic, trill, Optional.empty(), grace, leftHand, rightHand);
    }

    public NoteEffects withGrace(GraceNote grace) {
        return new NoteEffects(ornaments, dynamic, bend, slide, harmonic, trill, tremoloPicking, Optional.of(grace), leftHand, rightHand);
    }

    public NoteEffects withoutGrace() {
        return new NoteEffects(ornaments, dynamic, bend, slide, harmonic, trill, tremoloPicking, Optional.empty(), leftHand, rightHand);
    }

    public NoteEffects withLeftHand(Finger finger) {
        return new NoteEffects(ornaments, dynamic, bend, slide, harmonic, trill, tremoloPicking, grace, Optional.ofNullable(finger), rightHand);
    }

    public NoteEffects withRightHand(Finger finger) {
        return new NoteEffects(ornaments, dynamic, bend, slide, harmonic, trill, tremoloPicking, grace, leftHand, Optional.ofNullable(finger));
    }

    private EnumSet<Ornament> copyOfOrnaments() {
        EnumSet<Ornament> copy = EnumSet.noneOf(Ornament.class);
        copy.addAll(ornaments);
        return copy;
    }

    private NoteEffects withOrnaments(Set<Ornament> updated) {
        return new NoteEffects(updated, dynamic, bend, slide, harmonic, trill, tremoloPicking, grace, leftHand, rightHand);
    }
}
