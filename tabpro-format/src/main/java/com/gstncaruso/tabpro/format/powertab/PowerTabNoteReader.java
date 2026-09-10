package com.gstncaruso.tabpro.format.powertab;

import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendType;
import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import com.gstncaruso.tabpro.core.model.effects.NoteEffects;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.model.effects.SlideType;
import com.gstncaruso.tabpro.core.model.effects.Trill;

/**
 * Reads a PowerTab note: the fretted string (0-based in the file, 1-based in the
 * model), the fret, and the complex symbols that add a bend, a harmonic, a slide, or a
 * trill. The trill does not carry its own speed in this format: the model's default is used.
 */
final class PowerTabNoteReader {

    private static final int MAX_COMPLEX_SYMBOLS = 3;

    private static final int SYMBOL_SLIDE = 'd';
    private static final int SYMBOL_BEND = 'e';
    private static final int SYMBOL_TAPPED_HARMONIC = 'f';
    private static final int SYMBOL_TRILL = 'g';
    private static final int SYMBOL_ARTIFICIAL_HARMONIC = 'h';

    private static final int FLAG_TIED = 0x01;
    private static final int FLAG_MUTED = 0x02;
    private static final int FLAG_HAMMER_ON = 0x08;
    private static final int FLAG_PULL_OFF = 0x10;
    private static final int FLAG_NATURAL_HARMONIC = 0x40;
    private static final int FLAG_GHOST_NOTE = 0x80;

    private static final int SLIDE_INTO_FROM_BELOW = 1;
    private static final int SLIDE_INTO_FROM_ABOVE = 2;

    private static final int SLIDE_OUT_SHIFT = 1;
    private static final int SLIDE_OUT_LEGATO = 2;
    private static final int SLIDE_OUT_DOWNWARDS = 3;
    private static final int SLIDE_OUT_UPWARDS = 4;

    Note read(PowerTabByteReader reader) {
        int stringData = reader.readUnsignedByte();
        int simpleData = reader.readUnsignedShort();
        int[] symbols = reader.readSmallFixedArrayOfInts(MAX_COMPLEX_SYMBOLS);

        int string0Based = (stringData >>> 5) & 0x07;
        int fret = stringData & 0x1f;
        boolean tied = (simpleData & FLAG_TIED) != 0;

        NoteEffects effects = NoteEffects.none();
        if ((simpleData & FLAG_GHOST_NOTE) != 0) {
            effects = effects.with(Ornament.GHOST);
        }
        if ((simpleData & FLAG_MUTED) != 0) {
            effects = effects.with(Ornament.DEAD);
        }
        if ((simpleData & (FLAG_HAMMER_ON | FLAG_PULL_OFF)) != 0) {
            effects = effects.with(Ornament.HAMMER_ON_PULL_OFF);
        }
        if ((simpleData & FLAG_NATURAL_HARMONIC) != 0) {
            effects = effects.withHarmonic(HarmonicType.NATURAL);
        }
        effects = withComplexSymbols(effects, symbols);

        return new Note(string0Based + 1, fret, tied, effects);
    }

    private NoteEffects withComplexSymbols(NoteEffects effects, int[] symbols) {
        for (int symbol : symbols) {
            if (symbol == 0) {
                continue;
            }
            int type = (symbol >>> 24) & 0xFF;
            effects = switch (type) {
                case SYMBOL_SLIDE -> withSlide(effects, symbol);
                case SYMBOL_BEND -> effects.withBend(bendOf(symbol));
                case SYMBOL_TAPPED_HARMONIC -> effects.withHarmonic(HarmonicType.TAPPED);
                case SYMBOL_TRILL -> effects.withTrill(Trill.to((symbol >>> 16) & 0xFF));
                case SYMBOL_ARTIFICIAL_HARMONIC -> effects.withHarmonic(HarmonicType.ARTIFICIAL);
                default -> effects;
            };
        }
        return effects;
    }

    /** A PowerTab slide can carry both a "sliding in from" and a "sliding out to" at once. */
    private NoteEffects withSlide(NoteEffects effects, int symbol) {
        int slideOutType = (symbol >>> 8) & 0xFF;
        int slideIntoType = (symbol >>> 16) & 0xFF;
        SlideType slideOut = switch (slideOutType) {
            case SLIDE_OUT_SHIFT -> SlideType.SHIFT;
            case SLIDE_OUT_LEGATO -> SlideType.LEGATO;
            case SLIDE_OUT_DOWNWARDS -> SlideType.OUT_DOWNWARDS;
            case SLIDE_OUT_UPWARDS -> SlideType.OUT_UPWARDS;
            default -> null;
        };
        if (slideOut != null) {
            return effects.withSlide(slideOut);
        }
        SlideType slideInto = switch (slideIntoType) {
            case SLIDE_INTO_FROM_BELOW -> SlideType.IN_FROM_BELOW;
            case SLIDE_INTO_FROM_ABOVE -> SlideType.IN_FROM_ABOVE;
            default -> null;
        };
        return slideInto == null ? effects : effects.withSlide(slideInto);
    }

    /**
     * A PowerTab bend distinguishes 8 variants (holding the peak point or not). The
     * "held" variants are approximated to the base shape, and the duration and the
     * drawing points have nowhere to go in the model.
     */
    private static Bend bendOf(int symbol) {
        int type = (symbol >>> 20) & 0xF;
        int bentPitch = (symbol >>> 4) & 0xF;
        BendType bendType = switch (type) {
            case 0, 2 -> BendType.BEND; // normalBend, bendAndHold (approximated)
            case 1, 6, 7 -> BendType.BEND_RELEASE; // bendAndRelease, gradualRelease, immediateRelease (approximated)
            case 3, 5 -> BendType.PREBEND; // preBend, preBendAndHold (approximated)
            case 4 -> BendType.PREBEND_RELEASE;
            default -> BendType.BEND;
        };
        return Bend.of(bendType, bentPitch);
    }
}
