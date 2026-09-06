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
import com.gstncaruso.tabpro.core.model.effects.TremoloPicking;

/** Lee una nota de la tablatura y todos los efectos que le puede pedir la partitura. */
final class GuitarProNoteReader {

    private static final int FLAG_DURATION = 0x01;
    private static final int FLAG_HEAVY_ACCENT = 0x02;
    private static final int FLAG_GHOST = 0x04;
    private static final int FLAG_EFFECTS = 0x08;
    private static final int FLAG_DYNAMIC = 0x10;
    private static final int FLAG_TYPE_AND_FRET = 0x20;
    private static final int FLAG_ACCENT = 0x40;
    private static final int FLAG_FINGERING = 0x80;

    private static final int TIE_TYPE = 2;
    private static final int DEAD_TYPE = 3;

    private final GuitarProBendReader bendReader = new GuitarProBendReader();

    Note read(GuitarProByteReader reader, GuitarProVersion version, int string) {
        int flags = reader.readUnsignedByte();

        // El tipo y el traste los dispara la misma bandera pero no son consecutivos:
        // la dinamica se mete en el medio. Lo unico que cambia entre generaciones es
        // donde va la duracion propia de la nota, y de que tamano es.
        boolean isGp5 = version.hasNoteDurationPercent();
        int type = 1;
        if ((flags & FLAG_TYPE_AND_FRET) != 0) {
            type = reader.readUnsignedByte();
        }
        if (!isGp5) {
            skipDurationOverride(reader, version, flags);
        }

        NoteEffects effects = NoteEffects.none();
        if ((flags & FLAG_DYNAMIC) != 0) {
            effects = effects.withDynamic(dynamicOf(reader.readSignedByte()));
        }
        int fret = 0;
        if ((flags & FLAG_TYPE_AND_FRET) != 0) {
            fret = reader.readSignedByte();
        }
        Finger leftHand = null;
        Finger rightHand = null;
        if ((flags & FLAG_FINGERING) != 0) {
            leftHand = fingerOf(reader.readSignedByte());
            rightHand = fingerOf(reader.readSignedByte());
        }
        if (isGp5) {
            skipDurationOverride(reader, version, flags);
            // En gp5 toda nota cierra con un byte de banderas propio.
            reader.readUnsignedByte();
        }
        if (leftHand != null) {
            effects = effects.withLeftHand(leftHand);
        }
        if (rightHand != null) {
            effects = effects.withRightHand(rightHand);
        }
        if ((flags & FLAG_HEAVY_ACCENT) != 0) {
            effects = effects.with(Ornament.HEAVY_ACCENTED);
        }
        if ((flags & FLAG_GHOST) != 0) {
            effects = effects.with(Ornament.GHOST);
        }
        if ((flags & FLAG_ACCENT) != 0) {
            effects = effects.with(Ornament.ACCENTED);
        }
        if (type == DEAD_TYPE) {
            effects = effects.with(Ornament.DEAD);
        }
        if ((flags & FLAG_EFFECTS) != 0) {
            effects = readNoteEffects(reader, version, effects);
        }

        boolean tied = type == TIE_TYPE;
        int safeFret = Math.clamp(fret, 0, Note.MAX_FRET);
        return new Note(string, safeFret, tied, effects);
    }

    private void skipDurationOverride(GuitarProByteReader reader, GuitarProVersion version, int flags) {
        if ((flags & FLAG_DURATION) == 0) {
            return;
        }
        if (version.hasNoteDurationPercent()) {
            reader.readDoubleBigEndian();
        } else {
            reader.readSignedByte();
            reader.readSignedByte();
        }
    }

    private NoteEffects readNoteEffects(GuitarProByteReader reader, GuitarProVersion version, NoteEffects effects) {
        int flags = reader.readUnsignedByte();
        int flags2 = version.hasSecondFlagsByte() ? reader.readUnsignedByte() : 0;

        if ((flags & 0x01) != 0) {
            effects = effects.withBend(bendReader.read(reader));
        }
        if ((flags & 0x10) != 0) {
            effects = effects.withGrace(readGraceNote(reader, version));
        }
        if ((flags & 0x02) != 0) {
            effects = effects.with(Ornament.HAMMER_ON_PULL_OFF);
        }
        if ((flags & 0x08) != 0) {
            effects = effects.with(Ornament.LET_RING);
        }

        boolean slideRead = false;
        if (version.hasSecondFlagsByte()) {
            if ((flags2 & 0x04) != 0) {
                effects = effects.withTremoloPicking(new TremoloPicking(tremoloSpeedOf(reader.readSignedByte())));
            }
            if ((flags2 & 0x08) != 0) {
                slideRead = true;
                SlideType slide = slideTypeOf(reader.readSignedByte());
                if (slide != null) {
                    effects = effects.withSlide(slide);
                }
            }
            if ((flags2 & 0x10) != 0) {
                effects = withHarmonic(reader, version, effects);
            }
            if ((flags2 & 0x20) != 0) {
                int fret = reader.readUnsignedByte();
                NoteValue speed = tremoloSpeedOf(reader.readSignedByte());
                effects = effects.withTrill(new Trill(fret, speed));
            }
            if ((flags2 & 0x01) != 0) {
                effects = effects.with(Ornament.STACCATO);
            }
            if ((flags2 & 0x02) != 0) {
                effects = effects.with(Ornament.PALM_MUTE);
            }
            if ((flags2 & 0x40) != 0) {
                effects = effects.with(Ornament.VIBRATO);
            }
        }
        if (!slideRead && (flags & 0x04) != 0) {
            effects = effects.withSlide(SlideType.SHIFT);
        }
        return effects;
    }

    /**
     * El adorno trae siempre traste, dinamica, duracion y transicion, pero hasta GP4 la
     * duracion va antes que la transicion y desde GP5 el orden se invierte. Leerlos al
     * reves no corre ningun byte: cambia el adorno por otro sin que nada avise.
     */
    private GraceNote readGraceNote(GuitarProByteReader reader, GuitarProVersion version) {
        int fret = reader.readUnsignedByte();
        Dynamic dynamic = dynamicOf(reader.readSignedByte());
        NoteValue duration;
        GraceTransition transition;
        if (version.hasGraceTransitionBeforeDuration()) {
            transition = graceTransitionOf(reader.readUnsignedByte());
            duration = graceDurationOf(reader.readUnsignedByte());
        } else {
            duration = graceDurationOf(reader.readUnsignedByte());
            transition = graceTransitionOf(reader.readUnsignedByte());
        }
        boolean onBeat = false;
        boolean dead = false;
        if (version.hasGraceFlags()) {
            int graceFlags = reader.readUnsignedByte();
            dead = (graceFlags & 0x01) != 0;
            onBeat = (graceFlags & 0x02) != 0;
        }
        return new GraceNote(fret, duration, dynamic, transition, onBeat, dead);
    }

    private NoteEffects withHarmonic(GuitarProByteReader reader, GuitarProVersion version, NoteEffects effects) {
        if (version.hasStructuredHarmonic()) {
            int type = reader.readUnsignedByte();
            if (type == 2) {
                reader.skip(3);
            } else if (type == 3) {
                reader.skip(1);
            }
            HarmonicType harmonic = structuredHarmonicOf(type);
            return harmonic == null ? effects : effects.withHarmonic(harmonic);
        }
        HarmonicType harmonic = legacyHarmonicOf(reader.readSignedByte());
        return harmonic == null ? effects : effects.withHarmonic(harmonic);
    }

    private static HarmonicType structuredHarmonicOf(int type) {
        return switch (type) {
            case 1 -> HarmonicType.NATURAL;
            case 2 -> HarmonicType.ARTIFICIAL;
            case 3 -> HarmonicType.TAPPED;
            case 4 -> HarmonicType.PINCH;
            case 5 -> HarmonicType.SEMI;
            default -> null;
        };
    }

    private static HarmonicType legacyHarmonicOf(int code) {
        return switch (code) {
            case 1 -> HarmonicType.NATURAL;
            case 3 -> HarmonicType.TAPPED;
            case 4 -> HarmonicType.PINCH;
            case 5 -> HarmonicType.SEMI;
            case 15, 17, 22 -> HarmonicType.ARTIFICIAL;
            default -> null;
        };
    }

    private static SlideType slideTypeOf(int code) {
        return switch (code) {
            case -2 -> SlideType.IN_FROM_ABOVE;
            case -1 -> SlideType.IN_FROM_BELOW;
            case 1 -> SlideType.SHIFT;
            case 2 -> SlideType.LEGATO;
            case 3 -> SlideType.OUT_DOWNWARDS;
            case 4 -> SlideType.OUT_UPWARDS;
            default -> null;
        };
    }

    private static NoteValue tremoloSpeedOf(int code) {
        return switch (code) {
            case 1 -> NoteValue.EIGHTH;
            case 2 -> NoteValue.SIXTEENTH;
            default -> NoteValue.THIRTY_SECOND;
        };
    }

    private static GraceTransition graceTransitionOf(int code) {
        return switch (code) {
            case 1 -> GraceTransition.SLIDE;
            case 2 -> GraceTransition.BEND;
            case 3 -> GraceTransition.HAMMER;
            default -> GraceTransition.NONE;
        };
    }

    /** 1=treintaidosava, 2=veinticuatroava (sin equivalente: cae en treintaidosava), 3=dieciseisava. */
    private static NoteValue graceDurationOf(int code) {
        return code == 3 ? NoteValue.SIXTEENTH : NoteValue.THIRTY_SECOND;
    }

    private static Dynamic dynamicOf(int code) {
        Dynamic[] values = Dynamic.values();
        int index = Math.clamp(code - 1, 0, values.length - 1);
        return values[index];
    }

    private static Finger fingerOf(int code) {
        return code >= 0 && code <= 4 ? Finger.values()[code] : null;
    }
}
