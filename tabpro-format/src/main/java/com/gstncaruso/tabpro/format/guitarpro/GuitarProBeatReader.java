package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Tuplet;
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.model.effects.ParameterChange;
import com.gstncaruso.tabpro.core.model.effects.PickstrokeDirection;
import com.gstncaruso.tabpro.core.model.effects.SoundParameter;
import com.gstncaruso.tabpro.core.model.effects.Stroke;
import com.gstncaruso.tabpro.core.model.effects.StrokeDirection;
import com.gstncaruso.tabpro.core.model.effects.Wah;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    private static final int STROKE_UP = 0x40;
    private static final int STROKE_DOWN = 0x02;
    private static final int HAS_TREMOLO_BAR_OR_SLAP = 0x20;

    /** El byte que en GP3 elige entre la palanca (0) y el golpe. */
    private static final int NO_SLAP = 0;

    private static final int HAS_STROKE = 0x40;
    private static final int FADE_IN = 0x10;

    /** Cuando el beat rompe el corchete secundario, dice de cuantas figuras. */
    private static final int BREAK_SECONDARY_BEAM = 0x0800;

    /** La cuerda 1 del archivo es la mas aguda y ocupa el bit mas alto de la mascara. */
    private static final int HIGHEST_STRING_BIT = 0x40;

    /** El wah del cambio de parametros: -1 no lo toca, -2 lo apaga, 0 a 100 es cerrado a abierto. */
    private static final int WAH_UNCHANGED = -1;
    private static final int WAH_OFF = -2;
    private static final int WAH_HALFWAY = 50;

    private final GuitarProNoteReader notes = new GuitarProNoteReader();
    private final GuitarProChordReader chords = new GuitarProChordReader();
    private final GuitarProBendReader bends = new GuitarProBendReader();

    Beat read(GuitarProByteReader reader, GuitarProVersion version, int stringCount) {
        int flags = reader.readUnsignedByte();
        skipStatus(reader, flags);
        Duration duration = readDuration(reader, flags);
        BeatEffects effects = BeatEffects.none();
        OldOrnaments oldOrnaments = OldOrnaments.NONE;
        if ((flags & HAS_CHORD) != 0) {
            effects = effects.withChord(chords.read(reader, version, stringCount));
        }
        if ((flags & HAS_TEXT) != 0) {
            effects = effects.withText(reader.readLengthPrefixedString());
        }
        if ((flags & HAS_EFFECTS) != 0) {
            int effectFlags = reader.readUnsignedByte();
            int secondFlags = version.hasSecondFlagsByte() ? reader.readUnsignedByte() : 0;
            if (!version.hasSecondFlagsByte()) {
                oldOrnaments = OldOrnaments.of(effectFlags);
            }
            effects = readEffects(reader, version, effects, effectFlags, secondFlags);
        }
        if ((flags & HAS_MIX_TABLE_CHANGE) != 0) {
            effects = readMixTableChange(reader, version, effects);
        }
        List<Note> played = readNotes(reader, version, stringCount);
        skipGp5BeatExtras(reader, version);
        return new Beat(duration, oldOrnaments.spreadOver(played), effects);
    }

    /**
     * El byte de estado dice si el beat esta vacio, es normal o es un silencio. Los tres
     * casos se escriben igual: siempre viene despues la mascara de cuerdas, aunque este
     * en cero. Por eso el estado no cambia como se lee lo que sigue, y un beat es un
     * silencio para tabpro cuando no tiene ninguna nota.
     */
    private static void skipStatus(GuitarProByteReader reader, int flags) {
        if ((flags & HAS_STATUS) != 0) {
            reader.readUnsignedByte();
        }
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

    private BeatEffects readEffects(
            GuitarProByteReader reader, GuitarProVersion version, BeatEffects effects, int first, int second) {
        BeatEffects read = effects.withFadeIn((first & FADE_IN) != 0);
        if (!version.hasSecondFlagsByte() && (first & 0x02) != 0) {
            read = read.withWideVibrato(true);
        }
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
    /**
     * En GP3 el vibrato y los armonicos valen para todo el beat y no traen bytes
     * propios; de GP4 en adelante viven en cada nota. Se los reparte a mano.
     */
    private record OldOrnaments(boolean vibrato, HarmonicType harmonic) {

        static final OldOrnaments NONE = new OldOrnaments(false, null);

        static OldOrnaments of(int flags) {
            HarmonicType harmonic = null;
            if ((flags & 0x04) != 0) {
                harmonic = HarmonicType.NATURAL;
            }
            if ((flags & 0x08) != 0) {
                harmonic = HarmonicType.ARTIFICIAL;
            }
            return new OldOrnaments((flags & 0x01) != 0, harmonic);
        }

        List<Note> spreadOver(List<Note> played) {
            if (!vibrato && harmonic == null) {
                return played;
            }
            List<Note> withEffects = new ArrayList<>(played.size());
            for (Note note : played) {
                Note updated = vibrato ? note.toggling(Ornament.VIBRATO) : note;
                withEffects.add(harmonic == null ? updated : updated.withHarmonic(harmonic));
            }
            return withEffects;
        }
    }

    /**
     * En GP3 la palanca y el golpe entran por el mismo bit: un byte dice cual de los dos
     * es -- 0 es la palanca -- y detras van cuatro bytes, la profundidad de la palanca o
     * un entero que el golpe no usa. De GP4 en adelante ese byte es solo el golpe, y la
     * palanca pasa a tener bit propio y curva de puntos.
     */
    private BeatEffects readSlapOrTremoloBar(
            GuitarProByteReader reader, GuitarProVersion version, BeatEffects effects) {
        int slap = reader.readUnsignedByte();
        if (version.hasSecondFlagsByte()) {
            return slapping(effects, slap);
        }
        if (slap == NO_SLAP) {
            return effects.withTremoloBar(bends.readOldTremoloBar(reader));
        }
        reader.skip(version.slapEffectPaddingBytes());
        return slapping(effects, slap);
    }

    private static BeatEffects slapping(BeatEffects effects, int slap) {
        return switch (slap) {
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

    /**
     * El cambio de parametros que la mesa de mezcla inserta desde este beat, con el wah del
     * pedal si lo trae -- solo existe desde GP5. Lo que el cambio no toca viene escrito en -1
     * y no se lista.
     */
    private static BeatEffects readMixTableChange(GuitarProByteReader reader, GuitarProVersion version, BeatEffects effects) {
        int program = reader.readSignedByte();
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

        ParameterChange change = ParameterChange.nothing();
        change = changing(change, SoundParameter.PROGRAM, program);
        change = changing(change, SoundParameter.VOLUME, volume);
        change = changing(change, SoundParameter.PAN, pan);
        change = changing(change, SoundParameter.CHORUS, chorus);
        change = changing(change, SoundParameter.REVERB, reverb);
        change = changing(change, SoundParameter.PHASER, phaser);
        change = changing(change, SoundParameter.TREMOLO, tremolo);
        change = changing(change, SoundParameter.TEMPO, tempo);

        int transition = readTransitionDurations(reader, volume, pan, chorus, reverb, phaser, tremolo);
        change = change.over(Math.max(transition, readTempoTransition(reader, version, tempo)));
        change = change.onEveryTrack(readEveryTrackMask(reader, version));
        effects = effects.withParameterChange(change);

        if (version.hasGp5ChordFormat()) {
            Optional<Wah> wah = wahOf(reader.readSignedByte());
            if (wah.isPresent()) {
                effects = effects.withWah(wah.get());
            }
            if (version.hasRseInstrumentEffect()) {
                reader.readLengthPrefixedString();
                reader.readLengthPrefixedString();
            }
        }
        return effects;
    }

    private static ParameterChange changing(ParameterChange change, SoundParameter parameter, int value) {
        return value < 0 ? change : change.changing(parameter, value);
    }

    /** -2 apagado, -1 sin cambios, 0 a 100 de cerrado a abierto: tabpro solo distingue los tres estados. */
    private static Optional<Wah> wahOf(int value) {
        if (value == WAH_UNCHANGED) {
            return Optional.empty();
        }
        if (value <= WAH_OFF) {
            return Optional.of(Wah.OFF);
        }
        return Optional.of(value >= WAH_HALFWAY ? Wah.OPEN : Wah.CLOSED);
    }

    /**
     * El tempo trae su transicion como cualquier otro parametro, pero desde GP5.10 la
     * sigue una bandera que dice si el cambio se muestra en la partitura. Sin consumirla,
     * todo lo que viene despues en el archivo queda corrido en un byte.
     */
    private static int readTempoTransition(GuitarProByteReader reader, GuitarProVersion version, int tempo) {
        if (tempo < 0) {
            return 0;
        }
        int transition = reader.readSignedByte();
        if (version.hasHideTempo()) {
            reader.readBoolean();
        }
        return transition;
    }

    /**
     * Cada valor que cambia trae cuantos beats tarda en llegar. El modelo maneja
     * una sola transicion por cambio, asi que se queda con la mas larga.
     */
    private static int readTransitionDurations(GuitarProByteReader reader, int... changedValues) {
        int longest = 0;
        for (int value : changedValues) {
            if (value >= 0) {
                longest = Math.max(longest, reader.readSignedByte());
            }
        }
        return longest;
    }

    /** Desde GP4 una mascara dice que parametros valen para todas las pistas y no solo para esta. */
    private static boolean readEveryTrackMask(GuitarProByteReader reader, GuitarProVersion version) {
        return version.hasSecondFlagsByte() && reader.readUnsignedByte() != 0;
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
