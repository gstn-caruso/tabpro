package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.Finger;
import com.gstncaruso.tabpro.core.model.effects.GraceNote;
import com.gstncaruso.tabpro.core.model.effects.GraceTransition;
import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import com.gstncaruso.tabpro.core.model.effects.NoteEffects;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.model.effects.SlideType;
import com.gstncaruso.tabpro.core.model.effects.Trill;

/**
 * Only for GP4: no note's own duration and no structured harmonic, both of which GP5
 * adds. The grace note's "on beat" and "dead" flags do not exist in GP4 either: they
 * are lost when the grace note uses them.
 */
final class GuitarProNoteWriter {

    private static final int FLAG_HEAVY_ACCENT = 0x02;
    private static final int FLAG_GHOST = 0x04;
    private static final int FLAG_EFFECTS = 0x08;
    private static final int FLAG_DYNAMIC = 0x10;
    private static final int FLAG_TYPE_AND_FRET = 0x20;
    private static final int FLAG_ACCENT = 0x40;
    private static final int FLAG_FINGERING = 0x80;

    private static final int NORMAL_TYPE = 1;
    private static final int TIE_TYPE = 2;
    private static final int DEAD_TYPE = 3;

    private final GuitarProBendWriter bendWriter = new GuitarProBendWriter();

    void write(GuitarProByteWriter writer, Note note) {
        NoteEffects effects = note.effects();
        boolean hasFingering = effects.leftHand().isPresent() || effects.rightHand().isPresent();
        boolean hasNoteEffects = hasNoteEffects(effects);

        int flags = FLAG_TYPE_AND_FRET | FLAG_DYNAMIC;
        if (note.has(Ornament.HEAVY_ACCENTED)) {
            flags |= FLAG_HEAVY_ACCENT;
        }
        if (note.has(Ornament.GHOST)) {
            flags |= FLAG_GHOST;
        }
        if (note.has(Ornament.ACCENTED)) {
            flags |= FLAG_ACCENT;
        }
        if (hasFingering) {
            flags |= FLAG_FINGERING;
        }
        if (hasNoteEffects) {
            flags |= FLAG_EFFECTS;
        }
        writer.writeUnsignedByte(flags);
        writer.writeUnsignedByte(typeOf(note));
        writer.writeSignedByte(dynamicCode(effects.dynamic()));
        writer.writeSignedByte(note.fret());
        if (hasFingering) {
            writer.writeSignedByte(fingerCode(effects.leftHand().orElse(null)));
            writer.writeSignedByte(fingerCode(effects.rightHand().orElse(null)));
        }
        if (hasNoteEffects) {
            writeNoteEffects(writer, effects);
        }
    }

    private static int typeOf(Note note) {
        if (note.tied()) {
            return TIE_TYPE;
        }
        if (note.has(Ornament.DEAD)) {
            return DEAD_TYPE;
        }
        return NORMAL_TYPE;
    }

    private static boolean hasNoteEffects(NoteEffects effects) {
        return effects.bend().isPresent()
                || effects.grace().isPresent()
                || effects.has(Ornament.HAMMER_ON_PULL_OFF)
                || effects.has(Ornament.LET_RING)
                || effects.tremoloPicking().isPresent()
                || effects.slide().isPresent()
                || effects.harmonic().isPresent()
                || effects.trill().isPresent()
                || effects.has(Ornament.STACCATO)
                || effects.has(Ornament.PALM_MUTE)
                || effects.has(Ornament.VIBRATO);
    }

    private void writeNoteEffects(GuitarProByteWriter writer, NoteEffects effects) {
        int first = 0;
        if (effects.bend().isPresent()) {
            first |= 0x01;
        }
        if (effects.has(Ornament.HAMMER_ON_PULL_OFF)) {
            first |= 0x02;
        }
        if (effects.has(Ornament.LET_RING)) {
            first |= 0x08;
        }
        if (effects.grace().isPresent()) {
            first |= 0x10;
        }
        int second = 0;
        if (effects.has(Ornament.STACCATO)) {
            second |= 0x01;
        }
        if (effects.has(Ornament.PALM_MUTE)) {
            second |= 0x02;
        }
        if (effects.tremoloPicking().isPresent()) {
            second |= 0x04;
        }
        if (effects.slide().isPresent()) {
            second |= 0x08;
        }
        if (effects.harmonic().isPresent()) {
            second |= 0x10;
        }
        if (effects.trill().isPresent()) {
            second |= 0x20;
        }
        if (effects.has(Ornament.VIBRATO)) {
            second |= 0x40;
        }
        writer.writeUnsignedByte(first);
        writer.writeUnsignedByte(second);
        if (effects.bend().isPresent()) {
            bendWriter.write(writer, effects.bend().get());
        }
        if (effects.grace().isPresent()) {
            writeGraceNote(writer, effects.grace().get());
        }
        if (effects.tremoloPicking().isPresent()) {
            writer.writeSignedByte(tremoloSpeedCode(effects.tremoloPicking().get().speed()));
        }
        if (effects.slide().isPresent()) {
            writer.writeSignedByte(slideCode(effects.slide().get()));
        }
        if (effects.harmonic().isPresent()) {
            writer.writeSignedByte(legacyHarmonicCode(effects.harmonic().get()));
        }
        if (effects.trill().isPresent()) {
            Trill trill = effects.trill().get();
            writer.writeUnsignedByte(trill.fret());
            writer.writeSignedByte(tremoloSpeedCode(trill.speed()));
        }
    }

    /** In GP4 the duration comes before the transition; only GP5 reverses the two fields. */
    private void writeGraceNote(GuitarProByteWriter writer, GraceNote grace) {
        writer.writeUnsignedByte(grace.fret());
        writer.writeSignedByte(dynamicCode(grace.dynamic()));
        writer.writeUnsignedByte(graceDurationCode(grace.duration()));
        writer.writeUnsignedByte(graceTransitionCode(grace.transition()));
        // GP4 carries no flags byte of its own for the grace note: "on beat" and
        // "dead" only exist since GP5, and are lost here.
    }

    private static int dynamicCode(Dynamic dynamic) {
        return dynamic.ordinal() + 1;
    }

    private static int fingerCode(Finger finger) {
        return finger == null ? -1 : finger.ordinal();
    }

    private static int graceTransitionCode(GraceTransition transition) {
        return switch (transition) {
            case NONE -> 0;
            case SLIDE -> 1;
            case BEND -> 2;
            case HAMMER -> 3;
        };
    }

    /** SIXTEENTH has its own code; any other note value (thirty-second by default) uses 1. */
    private static int graceDurationCode(NoteValue duration) {
        return duration == NoteValue.SIXTEENTH ? 3 : 1;
    }

    /** Only eighth, sixteenth, and thirty-second have their own code; the rest fall back to thirty-second. */
    private static int tremoloSpeedCode(NoteValue speed) {
        return switch (speed) {
            case EIGHTH -> 1;
            case SIXTEENTH -> 2;
            default -> 3;
        };
    }

    private static int slideCode(SlideType slide) {
        return switch (slide) {
            case IN_FROM_ABOVE -> -2;
            case IN_FROM_BELOW -> -1;
            case SHIFT -> 1;
            case LEGATO -> 2;
            case OUT_DOWNWARDS -> 3;
            case OUT_UPWARDS -> 4;
        };
    }

    private static int legacyHarmonicCode(HarmonicType harmonic) {
        return switch (harmonic) {
            case NATURAL -> 1;
            case TAPPED -> 3;
            case PINCH -> 4;
            case SEMI -> 5;
            case ARTIFICIAL -> 15;
        };
    }
}
