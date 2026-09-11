package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.DefaultNames;
import com.gstncaruso.tabpro.core.model.DiagramPlacement;
import com.gstncaruso.tabpro.core.model.ScoreColor;
import com.gstncaruso.tabpro.core.model.TrackDisplay;
import java.util.ArrayList;
import java.util.List;

final class GuitarProTrackReader {

    private static final int FLAG_PERCUSSION = 0x01;
    private static final int FLAG_TWELVE_STRING = 0x02;
    private static final int FLAG_BANJO = 0x04;

    private static final int TUNING_SLOTS = 7;
    private static final int NAME_FIELD_SIZE = 40;

    private static final int STAFF_SHOWS_TABLATURE = 0x01;
    private static final int STAFF_SHOWS_STANDARD_NOTATION = 0x02;

    private final DefaultNames names;

    GuitarProTrackReader(DefaultNames names) {
        this.names = names;
    }

    GuitarProTrackHeader read(GuitarProByteReader reader, GuitarProVersion version, int trackNumber) {
        skipByteBeforeTheFlags(reader, version, trackNumber);
        int flags = reader.readUnsignedByte();
        String name = reader.readFixedString(NAME_FIELD_SIZE);
        int stringCount = reader.readInt();
        List<Integer> tuning = readTuning(reader, stringCount);
        reader.readInt(); // MIDI output port: it has no place in our channel model.
        int channelIndex = reader.readInt();
        int effectChannelIndex = reader.readInt();
        int fretCount = reader.readInt();
        int capo = reader.readInt();
        ScoreColor color = reader.readColor();
        TrackDisplay display = readTrackExtras(reader, version);

        return new GuitarProTrackHeader(
                name.isBlank() ? names.unnamedTrack() : name, tuning, channelIndex, effectChannelIndex, Math.max(1, fretCount),
                Math.max(0, capo), color, (flags & FLAG_PERCUSSION) != 0, (flags & FLAG_TWELVE_STRING) != 0,
                (flags & FLAG_BANJO) != 0, display);
    }

    /**
     * In v5 there is a loose byte before the flags: in 5.10 only ahead of the first
     * track, and in 5.00 ahead of every track.
     */
    private static void skipByteBeforeTheFlags(
            GuitarProByteReader reader, GuitarProVersion version, int trackNumber) {
        if (version.hasTrackExtras() && (trackNumber == 1 || version == GuitarProVersion.GP5_00)) {
            reader.skip(1);
        }
    }

    private List<Integer> readTuning(GuitarProByteReader reader, int stringCount) {
        List<Integer> allSlots = new ArrayList<>(TUNING_SLOTS);
        for (int i = 0; i < TUNING_SLOTS; i++) {
            allSlots.add(reader.readInt());
        }
        int usable = Math.clamp(stringCount, 1, TUNING_SLOTS);
        return List.copyOf(allSlots.subList(0, usable));
    }

    /** The fields only GP5 carries: how the track is drawn and its RSE instrument. */
    private TrackDisplay readTrackExtras(GuitarProByteReader reader, GuitarProVersion version) {
        if (!version.hasTrackExtras()) {
            return TrackDisplay.standard();
        }
        int staffFlags = reader.readUnsignedByte();
        reader.skip(4); // midiAutoFlags, rseAutoAccentuation, bank, humanPlaying
        reader.skip(12); // clefMode, unknownA, unknownB
        reader.skip(10); // padding with no known use
        reader.skip(2); // unknownC, unknownD
        // The RSE instrument: three integers and the effect number, which in 5.00
        // occupies two bytes plus one of padding and in 5.10 becomes a single integer.
        reader.skip(12);
        reader.skip(version.hasTrackEffectExtras() ? 4 : 3);
        if (version.hasTrackEffectExtras()) {
            reader.skip(4); // three-band equalizer
            reader.readLengthPrefixedString(); // RSE effect name
            reader.readLengthPrefixedString(); // RSE effect category
        }
        boolean tablature = (staffFlags & STAFF_SHOWS_TABLATURE) != 0;
        boolean standardNotation = (staffFlags & STAFF_SHOWS_STANDARD_NOTATION) != 0;
        if (!tablature && !standardNotation) {
            tablature = true;
            standardNotation = true;
        }
        return new TrackDisplay(
                standardNotation, tablature, false, false, DiagramPlacement.ABOVE_THE_STAFF, false, false);
    }
}
