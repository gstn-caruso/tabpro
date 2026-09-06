package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.DiagramPlacement;
import com.gstncaruso.tabpro.core.model.ScoreColor;
import com.gstncaruso.tabpro.core.model.TrackDisplay;
import java.util.ArrayList;
import java.util.List;

/** Lee el encabezado de una pista: su afinacion, sus canales y como se dibuja. */
final class GuitarProTrackReader {

    private static final int FLAG_PERCUSSION = 0x01;
    private static final int FLAG_TWELVE_STRING = 0x02;
    private static final int FLAG_BANJO = 0x04;

    private static final int TUNING_SLOTS = 7;
    private static final int NAME_FIELD_SIZE = 40;

    private static final int STAFF_SHOWS_TABLATURE = 0x01;
    private static final int STAFF_SHOWS_STANDARD_NOTATION = 0x02;

    GuitarProTrackHeader read(GuitarProByteReader reader, GuitarProVersion version, int trackNumber) {
        skipByteBeforeTheFlags(reader, version, trackNumber);
        int flags = reader.readUnsignedByte();
        String name = reader.readFixedString(NAME_FIELD_SIZE);
        int stringCount = reader.readInt();
        List<Integer> tuning = readTuning(reader, stringCount);
        reader.readInt(); // port de salida MIDI: no tiene lugar en nuestro modelo de canal.
        int channelIndex = reader.readInt();
        int effectChannelIndex = reader.readInt();
        int fretCount = reader.readInt();
        int capo = reader.readInt();
        ScoreColor color = reader.readColor();
        TrackDisplay display = readTrackExtras(reader, version);

        return new GuitarProTrackHeader(
                name.isBlank() ? "Pista" : name, tuning, channelIndex, effectChannelIndex, Math.max(1, fretCount),
                Math.max(0, capo), color, (flags & FLAG_PERCUSSION) != 0, (flags & FLAG_TWELVE_STRING) != 0,
                (flags & FLAG_BANJO) != 0, display);
    }

    /**
     * En v5 hay un byte suelto antes de las banderas: en 5.10 solo delante de la
     * primera pista, y en 5.00 delante de todas.
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

    /** Los campos que solo trae GP5: como se dibuja la pista y su instrumento de RSE. */
    private TrackDisplay readTrackExtras(GuitarProByteReader reader, GuitarProVersion version) {
        if (!version.hasTrackExtras()) {
            return TrackDisplay.standard();
        }
        int staffFlags = reader.readUnsignedByte();
        reader.skip(4); // midiAutoFlags, rseAutoAccentuation, bank, humanPlaying
        reader.skip(12); // clefMode, unknownA, unknownB
        reader.skip(10); // relleno sin uso conocido
        reader.skip(2); // unknownC, unknownD
        reader.skip(16); // instrumento de RSE (numero, banco, efecto)
        if (version.hasTrackEffectExtras()) {
            reader.skip(4); // ecualizador de 3 bandas
            reader.readLengthPrefixedString(); // nombre del efecto de RSE
            reader.readLengthPrefixedString(); // categoria del efecto de RSE
        }
        boolean tablature = (staffFlags & STAFF_SHOWS_TABLATURE) != 0;
        boolean standardNotation = (staffFlags & STAFF_SHOWS_STANDARD_NOTATION) != 0;
        if (!tablature && !standardNotation) {
            tablature = true;
            standardNotation = true;
        }
        return new TrackDisplay(standardNotation, tablature, true, false, DiagramPlacement.ABOVE_THE_STAFF);
    }
}
