package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Tuplet;
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import com.gstncaruso.tabpro.core.model.effects.PickstrokeDirection;
import com.gstncaruso.tabpro.core.model.effects.Stroke;
import com.gstncaruso.tabpro.core.model.effects.StrokeDirection;
import java.util.ArrayList;
import java.util.List;

/**
 * Lee un beat del archivo: su figura, su grupo irregular, sus efectos y las
 * notas de las cuerdas que suenan, que vienen marcadas en una mascara de bits.
 */
final class GuitarProBeatReader {

    private static final int HAS_DOT = 0x01;
    private static final int HAS_CHORD = 0x02;
    private static final int HAS_TEXT = 0x04;
    private static final int HAS_EFFECTS = 0x08;
    private static final int HAS_MIX_TABLE_CHANGE = 0x10;
    private static final int HAS_TUPLET = 0x20;
    private static final int HAS_STATUS = 0x40;

    private static final int STATUS_REST = 0x02;

    private static final int STROKE_UP = 0x40;
    private static final int STROKE_DOWN = 0x02;
    private static final int HAS_TREMOLO_BAR_OR_SLAP = 0x20;
    private static final int HAS_STROKE = 0x40;
    private static final int FADE_IN = 0x10;

    /** Cuando el beat rompe el corchete secundario, dice de cuantas figuras. */
    private static final int BREAK_SECONDARY_BEAM = 0x0800;

    /** La cuerda 1 del archivo es la mas aguda y ocupa el bit mas alto de la mascara. */
    private static final int HIGHEST_STRING_BIT = 0x40;

    private final GuitarProNoteReader notes = new GuitarProNoteReader();
    private final GuitarProChordReader chords = new GuitarProChordReader();
    private final GuitarProBendReader bends = new GuitarProBendReader();

    Beat read(GuitarProByteReader reader, GuitarProVersion version, int stringCount) {
        int flags = reader.readUnsignedByte();
        boolean rest = readStatus(reader, flags);
        Duration duration = readDuration(reader, flags);
        BeatEffects effects = BeatEffects.none();
        if ((flags & HAS_CHORD) != 0) {
            effects = effects.withChord(chords.read(reader, version, stringCount));
        }
        if ((flags & HAS_TEXT) != 0) {
            effects = effects.withText(reader.readLengthPrefixedString());
        }
        if ((flags & HAS_EFFECTS) != 0) {
            effects = readEffects(reader, version, effects);
        }
        if ((flags & HAS_MIX_TABLE_CHANGE) != 0) {
            skipMixTableChange(reader, version);
        }
        List<Note> played = rest ? List.of() : readNotes(reader, version, stringCount);
        skipGp5BeatExtras(reader, version);
        return new Beat(duration, played, effects);
    }

    /**
     * El byte de estado dice si el beat es un silencio. Cualquier otro valor,
     * incluido el cero, trae notas: un beat sin notas se escribe con el estado
     * de silencio, no con el de vacio.
     */
    private static boolean readStatus(GuitarProByteReader reader, int flags) {
        return (flags & HAS_STATUS) != 0 && reader.readUnsignedByte() == STATUS_REST;
    }

    private static Duration readDuration(GuitarProByteReader reader, int flags) {
        NoteValue value = noteValueOf(reader.readSignedByte());
        Tuplet tuplet = Tuplet.none();
        if ((flags & HAS_TUPLET) != 0) {
            tuplet = tupletOf(reader.readInt());
        }
        return new Duration(value, (flags & HAS_DOT) != 0, tuplet);
    }

    /** Guitar Pro numera las figuras de -2 (redonda) a 5 (semifusa). */
    private static NoteValue noteValueOf(int encoded) {
        return switch (encoded) {
            case -2 -> NoteValue.WHOLE;
            case -1 -> NoteValue.HALF;
            case 0 -> NoteValue.QUARTER;
            case 1 -> NoteValue.EIGHTH;
            case 2 -> NoteValue.SIXTEENTH;
            case 3 -> NoteValue.THIRTY_SECOND;
            default -> NoteValue.SIXTY_FOURTH;
        };
    }

    private static Tuplet tupletOf(int enters) {
        return Tuplet.AVAILABLE.contains(enters) ? Tuplet.of(enters) : Tuplet.none();
    }

    private BeatEffects readEffects(GuitarProByteReader reader, GuitarProVersion version, BeatEffects effects) {
        int first = reader.readUnsignedByte();
        int second = version.hasSecondFlagsByte() ? reader.readUnsignedByte() : 0;
        BeatEffects read = effects.withFadeIn((first & FADE_IN) != 0);
        if ((first & HAS_TREMOLO_BAR_OR_SLAP) != 0) {
            read = readSlapOrTremoloBar(reader, version, read);
        }
        if ((second & 0x04) != 0) {
            read = read.withTremoloBar(bends.read(reader));
        }
        if ((first & HAS_STROKE) != 0) {
            read = readStroke(reader, read);
        }
        if ((second & 0x02) != 0) {
            read = read.withPickstroke(
                    reader.readSignedByte() > 0 ? PickstrokeDirection.UP : PickstrokeDirection.DOWN);
        }
        return read;
    }

    /** En gp3 ese bit era la palanca; de gp4 en adelante es tapping, slap o pop. */
    private BeatEffects readSlapOrTremoloBar(
            GuitarProByteReader reader, GuitarProVersion version, BeatEffects effects) {
        if (!version.hasSecondFlagsByte()) {
            bends.read(reader);
            return effects;
        }
        return switch (reader.readUnsignedByte()) {
            case 1 -> effects.withTapping(true);
            case 2 -> effects.withSlapping(true);
            case 3 -> effects.withPopping(true);
            default -> effects;
        };
    }

    private static BeatEffects readStroke(GuitarProByteReader reader, BeatEffects effects) {
        int down = reader.readUnsignedByte();
        int up = reader.readUnsignedByte();
        if (down > 0) {
            return effects.withStroke(new Stroke(StrokeDirection.DOWN, strokeSpeed(down), false));
        }
        if (up > 0) {
            return effects.withStroke(new Stroke(StrokeDirection.UP, strokeSpeed(up), false));
        }
        return effects;
    }

    private static NoteValue strokeSpeed(int encoded) {
        return switch (encoded) {
            case 1 -> NoteValue.SIXTY_FOURTH;
            case 2 -> NoteValue.THIRTY_SECOND;
            case 3 -> NoteValue.SIXTEENTH;
            case 4 -> NoteValue.EIGHTH;
            default -> NoteValue.QUARTER;
        };
    }

    private List<Note> readNotes(GuitarProByteReader reader, GuitarProVersion version, int stringCount) {
        int mask = reader.readUnsignedByte();
        List<Note> played = new ArrayList<>();
        for (int string = 1; string <= stringCount; string++) {
            if ((mask & (HIGHEST_STRING_BIT >> (string - 1))) != 0) {
                played.add(notes.read(reader, version, string));
            }
        }
        return played;
    }

    /** El cambio de parametros todavia no tiene lugar en el modelo: se saltea entero. */
    private static void skipMixTableChange(GuitarProByteReader reader, GuitarProVersion version) {
        reader.readSignedByte();
        if (version.hasGp5ChordFormat()) {
            reader.skip(16);
        }
        int volume = reader.readSignedByte();
        int pan = reader.readSignedByte();
        int chorus = reader.readSignedByte();
        int reverb = reader.readSignedByte();
        int phaser = reader.readSignedByte();
        int tremolo = reader.readSignedByte();
        if (version.hasGp5ChordFormat()) {
            reader.readLengthPrefixedString();
        }
        int tempo = reader.readInt();
        skipTransitionDurations(reader, volume, pan, chorus, reverb, phaser, tremolo, tempo);
        if (version.hasSecondFlagsByte()) {
            reader.readUnsignedByte();
        }
        if (version.hasGp5ChordFormat()) {
            reader.readUnsignedByte();
            reader.skip(2);
        }
    }

    private static void skipTransitionDurations(GuitarProByteReader reader, int... changedValues) {
        for (int value : changedValues) {
            if (value >= 0) {
                reader.readSignedByte();
            }
        }
    }

    private static void skipGp5BeatExtras(GuitarProByteReader reader, GuitarProVersion version) {
        if (!version.hasSecondVoice()) {
            return;
        }
        int display = reader.readShort();
        if ((display & BREAK_SECONDARY_BEAM) != 0) {
            reader.readUnsignedByte();
        }
    }
}
