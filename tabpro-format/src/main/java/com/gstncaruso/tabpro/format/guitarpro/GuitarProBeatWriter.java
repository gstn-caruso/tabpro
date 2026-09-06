package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import com.gstncaruso.tabpro.core.model.effects.ParameterChange;
import com.gstncaruso.tabpro.core.model.effects.SoundParameter;
import com.gstncaruso.tabpro.core.model.effects.Stroke;
import com.gstncaruso.tabpro.core.model.effects.StrokeDirection;

/**
 * Escribe un beat: su figura, su grupo irregular, sus efectos y las notas que suenan. El
 * espejo de {@link GuitarProBeatReader}, solo para GP4 (sin la segunda voz ni el vibrato
 * ancho, que solo existen en otras generaciones del formato).
 */
final class GuitarProBeatWriter {

    private static final int HAS_DOT = 0x01;
    private static final int HAS_CHORD = 0x02;
    private static final int HAS_TEXT = 0x04;
    private static final int HAS_EFFECTS = 0x08;
    private static final int HAS_MIX_TABLE_CHANGE = 0x10;
    private static final int HAS_TUPLET = 0x20;
    private static final int HAS_STATUS = 0x40;

    private static final int STATUS_REST = 0x02;
    private static final int STATUS_NORMAL = 0x00;

    private static final int FADE_IN = 0x10;
    private static final int HAS_TREMOLO_BAR_OR_SLAP = 0x20;
    private static final int HAS_STROKE = 0x40;

    /** La cuerda 1 del archivo es la mas aguda y ocupa el bit mas alto de la mascara. */
    private static final int HIGHEST_STRING_BIT = 0x40;

    private final GuitarProNoteWriter noteWriter = new GuitarProNoteWriter();
    private final GuitarProChordWriter chordWriter = new GuitarProChordWriter();
    private final GuitarProBendWriter bendWriter = new GuitarProBendWriter();

    void write(GuitarProByteWriter writer, Beat beat) {
        BeatEffects effects = beat.effects();
        boolean hasBeatEffects = hasBeatEffects(effects);

        int flags = HAS_STATUS;
        if (beat.duration().dotted()) {
            flags |= HAS_DOT;
        }
        if (!beat.duration().tuplet().isPlain()) {
            flags |= HAS_TUPLET;
        }
        if (effects.chord().isPresent()) {
            flags |= HAS_CHORD;
        }
        if (effects.text().isPresent()) {
            flags |= HAS_TEXT;
        }
        if (hasBeatEffects) {
            flags |= HAS_EFFECTS;
        }
        if (!effects.parameterChange().isEmpty()) {
            flags |= HAS_MIX_TABLE_CHANGE;
        }

        writer.writeUnsignedByte(flags);
        writer.writeUnsignedByte(beat.isRest() ? STATUS_REST : STATUS_NORMAL);
        writeDuration(writer, beat.duration());
        if (effects.chord().isPresent()) {
            chordWriter.write(writer, effects.chord().get());
        }
        if (effects.text().isPresent()) {
            writer.writeLengthPrefixedString(effects.text().get());
        }
        if (hasBeatEffects) {
            writeBeatEffects(writer, effects);
        }
        if (!effects.parameterChange().isEmpty()) {
            writeParameterChange(writer, effects.parameterChange());
        }
        if (!beat.isRest()) {
            writeNotes(writer, beat);
        }
    }

    private void writeDuration(GuitarProByteWriter writer, Duration duration) {
        writer.writeSignedByte(noteValueCode(duration.value()));
        if (!duration.tuplet().isPlain()) {
            writer.writeInt(duration.tuplet().enters());
        }
    }

    private static int noteValueCode(NoteValue value) {
        return switch (value) {
            case WHOLE -> -2;
            case HALF -> -1;
            case QUARTER -> 0;
            case EIGHTH -> 1;
            case SIXTEENTH -> 2;
            case THIRTY_SECOND -> 3;
            case SIXTY_FOURTH -> 4;
        };
    }

    private static boolean hasBeatEffects(BeatEffects effects) {
        return effects.fadeIn() || effects.tapping() || effects.slapping() || effects.popping()
                || effects.tremoloBar().isPresent() || effects.stroke().isPresent()
                || effects.pickstroke().isPresent();
    }

    private void writeBeatEffects(GuitarProByteWriter writer, BeatEffects effects) {
        int first = 0;
        if (effects.fadeIn()) {
            first |= FADE_IN;
        }
        if (effects.tapping() || effects.slapping() || effects.popping()) {
            first |= HAS_TREMOLO_BAR_OR_SLAP;
        }
        if (effects.stroke().isPresent()) {
            first |= HAS_STROKE;
        }
        int second = 0;
        if (effects.tremoloBar().isPresent()) {
            second |= 0x04;
        }
        if (effects.pickstroke().isPresent()) {
            second |= 0x02;
        }
        writer.writeUnsignedByte(first);
        writer.writeUnsignedByte(second);
        if (effects.tapping() || effects.slapping() || effects.popping()) {
            writer.writeUnsignedByte(effects.tapping() ? 1 : effects.slapping() ? 2 : 3);
        }
        if (effects.tremoloBar().isPresent()) {
            bendWriter.write(writer, effects.tremoloBar().get());
        }
        if (effects.stroke().isPresent()) {
            writeStroke(writer, effects.stroke().get());
        }
        if (effects.pickstroke().isPresent()) {
            writer.writeSignedByte(
                    effects.pickstroke().get() == com.gstncaruso.tabpro.core.model.effects.PickstrokeDirection.UP
                            ? 1 : 0);
        }
    }

    private void writeStroke(GuitarProByteWriter writer, Stroke stroke) {
        int code = strokeSpeedCode(stroke.speed());
        writer.writeUnsignedByte(stroke.direction() == StrokeDirection.DOWN ? code : 0);
        writer.writeUnsignedByte(stroke.direction() == StrokeDirection.UP ? code : 0);
    }

    /** Solo cuatro figuras tienen codigo propio; el resto (negra, blanca, redonda) cae en negra. */
    private static int strokeSpeedCode(NoteValue speed) {
        return switch (speed) {
            case SIXTY_FOURTH -> 1;
            case THIRTY_SECOND -> 2;
            case SIXTEENTH -> 3;
            case EIGHTH -> 4;
            default -> 5;
        };
    }

    private void writeParameterChange(GuitarProByteWriter writer, ParameterChange change) {
        writer.writeSignedByte(valueOrUnset(change, SoundParameter.PROGRAM));
        writer.writeSignedByte(valueOrUnset(change, SoundParameter.VOLUME));
        writer.writeSignedByte(valueOrUnset(change, SoundParameter.PAN));
        writer.writeSignedByte(valueOrUnset(change, SoundParameter.CHORUS));
        writer.writeSignedByte(valueOrUnset(change, SoundParameter.REVERB));
        writer.writeSignedByte(valueOrUnset(change, SoundParameter.PHASER));
        writer.writeSignedByte(valueOrUnset(change, SoundParameter.TREMOLO));
        writer.writeInt(valueOrUnset(change, SoundParameter.TEMPO));
        for (SoundParameter parameter : new SoundParameter[] {
                SoundParameter.VOLUME, SoundParameter.PAN, SoundParameter.CHORUS, SoundParameter.REVERB,
                SoundParameter.PHASER, SoundParameter.TREMOLO, SoundParameter.TEMPO}) {
            if (change.changes(parameter)) {
                writer.writeSignedByte(Math.clamp(change.transitionBeats(), 0, 127));
            }
        }
        writer.writeUnsignedByte(change.everyTrack() ? 1 : 0);
    }

    private static int valueOrUnset(ParameterChange change, SoundParameter parameter) {
        return change.changes(parameter) ? change.valueOf(parameter).orElseThrow() : -1;
    }

    private void writeNotes(GuitarProByteWriter writer, Beat beat) {
        int mask = 0;
        for (Note note : beat.notes()) {
            mask |= HIGHEST_STRING_BIT >> (note.string() - 1);
        }
        writer.writeUnsignedByte(mask);
        for (Note note : beat.notes()) {
            noteWriter.write(writer, note);
        }
    }
}
