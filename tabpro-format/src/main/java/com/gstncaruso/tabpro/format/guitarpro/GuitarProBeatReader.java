package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.DefaultNames;
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

final class GuitarProBeatReader {

    private static final int HAS_DOT = 0x01;
    private static final int HAS_CHORD = 0x02;
    private static final int HAS_TEXT = 0x04;
    private static final int HAS_EFFECTS = 0x08;
    private static final int HAS_MIX_TABLE_CHANGE = 0x10;
    private static final int HAS_TUPLET = 0x20;
    private static final int HAS_STATUS = 0x40;

    private static final int HAS_TREMOLO_BAR_OR_SLAP = 0x20;

    /** Wide vibrato belongs to the whole beat and uses the same bit in all three versions. */
    private static final int WIDE_VIBRATO = 0x02;

    /** Pickstroke direction, in the second effects byte that exists since GP4. */
    private static final int HAS_PICKSTROKE = 0x02;

    /** The byte that in GP3 selects between the tremolo bar (0) and the slap effect. */
    private static final int NO_SLAP = 0;

    private static final int HAS_STROKE = 0x40;
    private static final int FADE_IN = 0x10;

    /** When the beat breaks the secondary beam, it says how many note values. */
    private static final int BREAK_SECONDARY_BEAM = 0x0800;

    /** String 1 in the file is the highest-pitched one and occupies the top bit of the mask. */
    private static final int HIGHEST_STRING_BIT = 0x40;

    /** The six bits of the parameter-change mask that speak for every other track. */
    private static final int EVERY_TRACK_KNOBS = 0x3F;

    /** The wah of the parameter change: -1 leaves it untouched, -2 turns it off, 0 to 100 is closed to open. */
    private static final int WAH_UNCHANGED = -1;
    private static final int WAH_OFF = -2;
    private static final int WAH_HALFWAY = 50;

    private final GuitarProNoteReader notes = new GuitarProNoteReader();
    private final GuitarProChordReader chords;
    private final GuitarProBendReader bends = new GuitarProBendReader();

    GuitarProBeatReader(DefaultNames names) {
        this.chords = new GuitarProChordReader(names);
    }

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
     * The status byte says whether the beat is empty, normal, or a rest. All three cases
     * are written the same way: the string mask always follows, even when it is zero. So
     * the status does not change how what follows is read, and a beat is a rest for
     * tabpro when it has no notes.
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

    /** Guitar Pro numbers note values from -2 (whole) to 5 (sixty-fourth). */
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
        if ((first & WIDE_VIBRATO) != 0) {
            read = read.withWideVibrato(true);
        }
        if ((first & HAS_TREMOLO_BAR_OR_SLAP) != 0) {
            read = readSlapOrTremoloBar(reader, version, read);
        }
        if ((second & 0x04) != 0) {
            read = read.withTremoloBar(bends.read(reader));
        }
        if ((first & HAS_STROKE) != 0) {
            read = readStroke(reader, version, read);
        }
        if ((second & HAS_PICKSTROKE) != 0) {
            PickstrokeDirection pickstroke = pickstrokeOf(reader.readSignedByte());
            if (pickstroke != null) {
                read = read.withPickstroke(pickstroke);
            }
        }
        return read;
    }

    /** Pickstroke direction is a number: 1 up, 2 down, 0 none. */
    private static PickstrokeDirection pickstrokeOf(int code) {
        return switch (code) {
            case 1 -> PickstrokeDirection.UP;
            case 2 -> PickstrokeDirection.DOWN;
            default -> null;
        };
    }

    /**
     * In GP3, vibrato and harmonics apply to the whole beat and carry no bytes
     * of their own; from GP4 on they live on each note. They are spread by hand.
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
     * In GP3 the tremolo bar and the slap effect share the same bit: a byte says which of
     * the two it is -- 0 is the tremolo bar -- followed by four bytes, either the bar depth
     * or an integer the slap effect does not use. From GP4 on that byte is only the slap
     * effect, and the tremolo bar gets its own bit and point curve.
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

    /**
     * The stroke carries both speeds, up and down, and sounds in whichever direction is
     * not zero. GP5 reverses the order in which it writes them.
     */
    private static BeatEffects readStroke(
            GuitarProByteReader reader, GuitarProVersion version, BeatEffects effects) {
        int first = reader.readUnsignedByte();
        int second = reader.readUnsignedByte();
        int up = version.strokeUpFirst() ? first : second;
        int down = version.strokeUpFirst() ? second : first;
        if (up > 0) {
            return effects.withStroke(new Stroke(StrokeDirection.UP, strokeSpeed(up), false));
        }
        if (down > 0) {
            return effects.withStroke(new Stroke(StrokeDirection.DOWN, strokeSpeed(down), false));
        }
        return effects;
    }

    /**
     * The speed is a note-value number starting at the 128th note: 1 is 1/128, 2 is 1/64,
     * and so on up to 6, which is the quarter note. Tabpro does not reach 1/128 and
     * approximates it with the sixty-fourth, its fastest value.
     */
    private static NoteValue strokeSpeed(int encoded) {
        return switch (encoded) {
            case 1, 2 -> NoteValue.SIXTY_FOURTH;
            case 3 -> NoteValue.THIRTY_SECOND;
            case 4 -> NoteValue.SIXTEENTH;
            case 5 -> NoteValue.EIGHTH;
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
     * The parameter change the mixing table inserts starting at this beat, with the pedal
     * wah if it carries one -- this only exists since GP5. Whatever the change does not
     * touch is written as -1 and left out.
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
        change = changingKnob(change, SoundParameter.VOLUME, volume);
        change = changingKnob(change, SoundParameter.PAN, pan);
        change = changingKnob(change, SoundParameter.CHORUS, chorus);
        change = changingKnob(change, SoundParameter.REVERB, reverb);
        change = changingKnob(change, SoundParameter.PHASER, phaser);
        change = changingKnob(change, SoundParameter.TREMOLO, tremolo);
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

    /**
     * The mixing-table knobs change in their sixteen steps, the same ones as the channel
     * table; the instrument and the tempo, on the other hand, go through as is.
     */
    private static ParameterChange changingKnob(ParameterChange change, SoundParameter parameter, int step) {
        return changing(change, parameter, step < 0 ? step : new GuitarProMixerLevel(step).midi());
    }

    /** -2 off, -1 unchanged, 0 to 100 closed to open: tabpro only distinguishes the three states. */
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
     * The tempo carries its transition like any other parameter, but since GP5.10 it is
     * followed by a flag saying whether the change shows in the score. Without consuming
     * it, everything that comes after in the file is shifted by one byte.
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

    /** Each value that changes carries its own byte for how many beats it takes to arrive. */
    private static int readTransitionDurations(GuitarProByteReader reader, int... changedValues) {
        int longest = 0;
        for (int value : changedValues) {
            if (value >= 0) {
                longest = Math.max(longest, reader.readSignedByte());
            }
        }
        return longest;
    }

    /**
     * Since GP4 a mask says which parameters apply to every track and not just this one.
     * Of that mask, "every track" is only the six knob bits: the top two say whether the
     * change uses the RSE and whether the wah shows in the score, and in a .gp5 file they
     * are set almost always.
     */
    private static boolean readEveryTrackMask(GuitarProByteReader reader, GuitarProVersion version) {
        return version.hasSecondFlagsByte() && (reader.readUnsignedByte() & EVERY_TRACK_KNOBS) != 0;
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
