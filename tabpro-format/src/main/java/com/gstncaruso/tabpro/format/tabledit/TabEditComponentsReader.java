package com.gstncaruso.tabpro.format.tabledit;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Note;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the list of components at the end of the file: each one is a fixed 12-byte
 * record (a position, a type byte, and seven of payload), so whatever is not a note or
 * a rest can be discarded without losing alignment. A type we do not recognize at all
 * is declared with an exception instead of risking a blind interpretation.
 */
final class TabEditComponentsReader {

    private static final int RECORD_SIZE = 12;
    private static final int FOOTER_SIZE = 4;
    private static final int EXPECTED_FOOTER = -1;

    private static final int TYPE_REST = 0x33;

    /**
     * Types we recognize but do not yet translate into the tabpro model: chord, line
     * break, accent, crescendo, text event, tie (graphic slur with duration and beam),
     * scale diagram, drum change, spacing mark or grace-note metadata (they share the
     * same type), voice/instrument change, symbol, alternate ending (the repeats), beam
     * cut, stem length, and syncopation.
     */
    private static final int[] KNOWN_BUT_UNSUPPORTED_TYPES = {
            0x35, 0x36, 0x37, 0x38, 0x39, 0x3D, 0x75, 0x78, 0x7D, 0x7E, 0xB6, 0xB7, 0xBD, 0xBE, 0xFD, 0xFE,
    };

    private final TabEditNoteReader noteReader = new TabEditNoteReader();
    private final TabEditRestReader restReader = new TabEditRestReader();

    List<TabEditEvent> read(TabEditByteReader input, List<TabEditMeasure> measures, List<Integer> trackStringCounts) {
        List<TabEditEvent> events = new ArrayList<>();

        while (input.remaining() >= RECORD_SIZE) {
            TabEditByteReader record = new TabEditByteReader(input.readBlock(RECORD_SIZE));
            int location = record.readInt();
            int type = record.readUnsignedByte();
            TabEditPosition position = TabEditPosition.fromLocation(location, measures, trackStringCounts);

            if (type == TYPE_REST) {
                TabEditRestFields fields = restReader.read(record);
                events.add(new TabEditRestEvent(position, fields.duration(), fields.voice()));
            } else if (isKnownButUnsupported(type)) {
                // The 12-byte block is already fully consumed: no need to read anything else.
            } else if (isNoteType(type)) {
                TabEditNoteFields fields = noteReader.read(record, type);
                if (fields.isGraceNote()) {
                    // TablEdit stores the grace note as its own event, with its own
                    // position; tabpro only knows how to decorate the main note with a
                    // preceding grace note. Merging them by hand would mean guessing which
                    // one is "the main one", so for now it is declared unsupported and discarded.
                    continue;
                }
                events.add(noteEventOf(position, fields));
            } else {
                throw ScoreFileException.damaged(String.format("unknown TablEdit component: type 0x%02X", type));
            }
        }

        if (input.remaining() != FOOTER_SIZE) {
            throw ScoreFileException.damaged(
                    "unexpected TablEdit footer size: " + input.remaining() + " bytes left");
        }
        if (input.readInt() != EXPECTED_FOOTER) {
            throw ScoreFileException.damaged("unexpected TablEdit footer");
        }

        return events;
    }

    private static TabEditNoteEvent noteEventOf(TabEditPosition position, TabEditNoteFields fields) {
        int fret = Math.clamp(fields.fret(), 0, Note.MAX_FRET);
        Note note = new Note(position.stringZeroBased() + 1, fret, fields.tied(), fields.effects());
        return new TabEditNoteEvent(position, fields.duration(), fields.voice(), note, fields.tapping(),
                fields.slapping(), fields.fadeIn());
    }

    /** The note range applies to any byte whose lowest 5 bits fall in it, regardless of the other bits. */
    private static boolean isNoteType(int type) {
        int lowerBits = type & 0x1F;
        return lowerBits > 0 && lowerBits <= 0x19;
    }

    private static boolean isKnownButUnsupported(int type) {
        for (int known : KNOWN_BUT_UNSUPPORTED_TYPES) {
            if (known == type) {
                return true;
            }
        }
        return false;
    }
}
