package com.gstncaruso.tabpro.format.tabledit;

import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import com.gstncaruso.tabpro.core.model.effects.NoteEffects;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.model.effects.SlideType;
import com.gstncaruso.tabpro.core.model.effects.TremoloPicking;
import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendType;
import com.gstncaruso.tabpro.core.model.NoteValue;

/**
 * Reads the seven payload bytes of a note (the type byte was already read by the
 * orchestrator, since it also needs it to decide what kind of component it is).
 * TablEdit stores neither the pitch of a bend nor the speed of a tremolo: where the
 * format carries no data, the fixed value assumed is documented, the same as TuxGuitar
 * itself does.
 */
final class TabEditNoteReader {

    /** A whole tone (2 semitones), the most common bend, in tabpro's quarter-tone units. */
    private static final int DEFAULT_BEND_QUARTER_TONES = 4;

    TabEditNoteFields read(TabEditByteReader input, int type) {
        int fret = (type & 0x1F) - 1;
        boolean isGraceNote = (type & 0x40) != 0;

        int byte1 = input.readUnsignedByte();
        int durationCode = byte1 & 0x1F;
        int dynamicsCode = (byte1 >> 5) & 0x07;

        int byte2 = input.readUnsignedByte();
        int effect1 = byte2 & 0x0F;
        int attributes = (byte2 >> 4) & 0x03;

        int byte3 = input.readUnsignedByte();
        int graceNoteFret = byte3 & 0x1F;
        int graceNoteEffect = (byte3 >> 5) & 0x07;

        int byte4 = input.readUnsignedByte();
        int effect2 = byte4 & 0x0F;
        int effect3 = (byte4 >> 4) & 0x0F;

        input.skip(2); // font/highlight and fingering/strumming: only affect the drawing.

        int byte7 = input.readUnsignedByte();
        boolean tied = ((byte7 >> 5) & 0x01) != 0;

        Dynamic dynamic = dynamicOf(dynamicsCode);
        // TablEdit also uses the PPP dynamic as a tie mark, in addition to the tie bit.
        boolean reallyTied = tied || dynamic == Dynamic.PIANO_PIANISSIMO;

        VoicePart voice = attributes == 3 ? VoicePart.BASS : VoicePart.LEAD;

        NoteEffects effects = NoteEffects.none().withDynamic(dynamic);
        effects = applyEffect1(effects, effect1);
        effects = applyEffect2(effects, effect2);
        effects = applyEffect3(effects, effect3);

        boolean tapping = effect1 == 9;
        boolean slapping = effect2 == 2;
        boolean fadeIn = effect2 == 8;

        return new TabEditNoteFields(
                fret, isGraceNote, graceNoteFret, TabEditDurationMapper.toDuration(durationCode), reallyTied, dynamic,
                voice, effects, tapping, slapping, fadeIn);
    }

    private static NoteEffects applyEffect1(NoteEffects effects, int code) {
        return switch (code) {
            case 1, 2 -> effects.with(Ornament.HAMMER_ON_PULL_OFF); // TablEdit does not distinguish hammer-on from pull-off.
            case 3 -> effects.withSlide(SlideType.LEGATO); // the format does not store the slide type.
            case 6 -> effects.withHarmonic(HarmonicType.NATURAL);
            case 7 -> effects.withHarmonic(HarmonicType.ARTIFICIAL);
            case 8 -> effects.with(Ornament.PALM_MUTE);
            case 10 -> effects.with(Ornament.VIBRATO);
            case 11 -> effects.withTremoloPicking(new TremoloPicking(NoteValue.SIXTEENTH)); // no speed data.
            case 12 -> effects.withBend(Bend.of(BendType.BEND, DEFAULT_BEND_QUARTER_TONES));
            case 13 -> effects.withBend(Bend.of(BendType.BEND_RELEASE, DEFAULT_BEND_QUARTER_TONES));
            case 15 -> effects.with(Ornament.DEAD);
            // 0 no effect; 4 choke, 5 brush, 9 tap (beat mark), 14 roll: have no note equivalent.
            default -> effects;
        };
    }

    private static NoteEffects applyEffect2(NoteEffects effects, int code) {
        return switch (code) {
            case 1 -> effects.with(Ornament.LET_RING);
            case 4 -> effects.with(Ornament.GHOST);
            case 7 -> effects.with(Ornament.STACCATO);
            // 2 slap and 8 fade in are beat marks; 3 strum, 5/6 tremolo bar, 9 fade out, 15 hide:
            // have no equivalent in the tabpro model.
            default -> effects;
        };
    }

    private static NoteEffects applyEffect3(NoteEffects effects, int code) {
        return switch (code) {
            case 1, 2 -> effects.with(Ornament.HAMMER_ON_PULL_OFF);
            case 6 -> effects.withHarmonic(HarmonicType.NATURAL);
            case 7 -> effects.withHarmonic(HarmonicType.ARTIFICIAL);
            case 8 -> effects.with(Ornament.LET_RING);
            case 9 -> effects.with(Ornament.GHOST);
            case 10 -> effects.with(Ornament.DEAD);
            // 3 roll, 4 choke, 5 brush, 11 variation: have no equivalent.
            default -> effects;
        };
    }

    private static Dynamic dynamicOf(int code) {
        // TablEdit numbers FFF..PPP (0..7); tabpro numbers the other way, from ppp to fff.
        Dynamic[] values = Dynamic.values();
        return values[values.length - 1 - Math.clamp(code, 0, values.length - 1)];
    }
}
