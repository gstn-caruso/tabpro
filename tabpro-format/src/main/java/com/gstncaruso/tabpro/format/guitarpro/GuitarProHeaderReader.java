package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.Lyrics;
import com.gstncaruso.tabpro.core.model.LyricLine;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.core.model.bars.DirectionJump;
import com.gstncaruso.tabpro.core.model.bars.DirectionSymbol;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import com.gstncaruso.tabpro.core.model.bars.TripletFeel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Reads the file header: the score info, the lyrics, the page setup and RSE settings
 * (which have no place in the tabpro model and are discarded), the tempo, and the
 * initial key signature.
 */
final class GuitarProHeaderReader {

    GuitarProHeader read(GuitarProByteReader reader, GuitarProVersion version) {
        ScoreInfo info = readScoreInfo(reader, version);
        Optional<TripletFeel> globalTripletFeel = readGlobalTripletFeel(reader, version);
        Lyrics lyrics = readLyrics(reader, version);
        skipRseMasterSettings(reader, version);
        skipPageSetup(reader, version);
        skipTempoLabel(reader, version);
        int tempo = reader.readInt();
        skipHideTempo(reader, version);
        KeySignature keySignature = readKeySignatureAndOctave(reader, version);
        return new GuitarProHeader(info, lyrics, tempo, keySignature, globalTripletFeel);
    }

    /**
     * The channels play right after the header; the directions, right after them: 19
     * two-byte slots, one per target symbol (Coda, Double Coda, Segno, Segno Segno,
     * Fine, in that order) and one per jump (the fourteen from {@link DirectionJump},
     * in the order the enum declares them), each carrying the measure it points to or
     * -1 if unused. Four reserved bytes close the block.
     *
     * <p>The order in which the file stores the five target symbols is fixed by the
     * Guitar Pro format, not by tabpro: that is why it is declared here, where the file
     * is read, rather than derived from the enum's declaration order.
     */
    static final List<DirectionSymbol> SYMBOL_SLOTS = List.of(
            DirectionSymbol.CODA,
            DirectionSymbol.DOUBLE_CODA,
            DirectionSymbol.SEGNO,
            DirectionSymbol.SEGNO_SEGNO,
            DirectionSymbol.FINE);

    /** The order in which the file stores the fourteen jumps. Fixed by the format. */
    static final List<DirectionJump> JUMP_SLOTS = List.of(
            DirectionJump.DA_CAPO,
            DirectionJump.DA_CAPO_AL_CODA,
            DirectionJump.DA_CAPO_AL_DOUBLE_CODA,
            DirectionJump.DA_CAPO_AL_FINE,
            DirectionJump.DA_SEGNO,
            DirectionJump.DA_SEGNO_AL_CODA,
            DirectionJump.DA_SEGNO_AL_DOUBLE_CODA,
            DirectionJump.DA_SEGNO_AL_FINE,
            DirectionJump.DA_SEGNO_SEGNO,
            DirectionJump.DA_SEGNO_SEGNO_AL_CODA,
            DirectionJump.DA_SEGNO_SEGNO_AL_DOUBLE_CODA,
            DirectionJump.DA_SEGNO_SEGNO_AL_FINE,
            DirectionJump.DA_CODA,
            DirectionJump.DA_DOUBLE_CODA);

    GuitarProDirections readDirections(GuitarProByteReader reader, GuitarProVersion version) {
        if (!version.hasDirections()) {
            return GuitarProDirections.none();
        }
        Map<Integer, DirectionSymbol> symbols = new LinkedHashMap<>();
        for (DirectionSymbol symbol : SYMBOL_SLOTS) {
            readSlot(reader).ifPresent(measureIndex -> symbols.put(measureIndex, symbol));
        }
        Map<Integer, DirectionJump> jumps = new LinkedHashMap<>();
        for (DirectionJump jump : JUMP_SLOTS) {
            readSlot(reader).ifPresent(measureIndex -> jumps.put(measureIndex, jump));
        }
        reader.skip(4);
        return new GuitarProDirections(symbols, jumps);
    }

    /**
     * A target slot: the number of the measure it points to -- the first one is one,
     * not zero -- or empty if unused, which is what a -1 means.
     */
    private Optional<Integer> readSlot(GuitarProByteReader reader) {
        int measureNumber = reader.readShort();
        return measureNumber > 0 ? Optional.of(measureNumber - 1) : Optional.empty();
    }

    private ScoreInfo readScoreInfo(GuitarProByteReader reader, GuitarProVersion version) {
        String title = reader.readLengthPrefixedString();
        String subtitle = reader.readLengthPrefixedString();
        String artist = reader.readLengthPrefixedString();
        String album = reader.readLengthPrefixedString();
        String lyricsAuthor = reader.readLengthPrefixedString();
        String musicAuthor = version.hasMusicAuthorField() ? reader.readLengthPrefixedString() : "";
        String copyright = reader.readLengthPrefixedString();
        String transcriber = reader.readLengthPrefixedString();
        String instructions = reader.readLengthPrefixedString();
        String notice = readNoticeLines(reader);
        return new ScoreInfo(title, subtitle, artist, album, lyricsAuthor, musicAuthor, copyright, transcriber,
                instructions, notice);
    }

    private String readNoticeLines(GuitarProByteReader reader) {
        int lineCount = reader.readInt();
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < lineCount; i++) {
            lines.add(reader.readLengthPrefixedString());
        }
        return String.join("\n", lines);
    }

    private Optional<TripletFeel> readGlobalTripletFeel(GuitarProByteReader reader, GuitarProVersion version) {
        if (!version.hasGlobalTripletFeel()) {
            return Optional.empty();
        }
        boolean swings = reader.readBoolean();
        return Optional.of(swings ? TripletFeel.EIGHTH : TripletFeel.NONE);
    }

    private Lyrics readLyrics(GuitarProByteReader reader, GuitarProVersion version) {
        if (!version.hasLyrics()) {
            return Lyrics.none();
        }
        int trackIndex = Math.max(0, reader.readInt() - 1);
        List<LyricLine> lines = new ArrayList<>();
        for (int i = 0; i < LyricLine.MAX_LINES; i++) {
            int startingMeasure = Math.max(1, reader.readInt());
            String text = reader.readIntPrefixedString();
            lines.add(new LyricLine(startingMeasure, text));
        }
        return new Lyrics(trackIndex, lines);
    }

    private void skipRseMasterSettings(GuitarProByteReader reader, GuitarProVersion version) {
        if (version.hasRseMasterSettings()) {
            reader.skip(19);
        }
    }

    private void skipPageSetup(GuitarProByteReader reader, GuitarProVersion version) {
        if (!version.hasPageSetup()) {
            return;
        }
        reader.skip(28);
        reader.readShort();
        for (int i = 0; i < 10; i++) {
            reader.readLengthPrefixedString();
        }
    }

    private void skipTempoLabel(GuitarProByteReader reader, GuitarProVersion version) {
        if (version.hasTempoLabel()) {
            reader.readLengthPrefixedString();
        }
    }

    private void skipHideTempo(GuitarProByteReader reader, GuitarProVersion version) {
        if (version.hasHideTempo()) {
            reader.readBoolean();
        }
    }

    /**
     * The initial key signature is a signed integer and nothing else: how many
     * accidentals it carries. The major or minor mode does not exist here; it only
     * appears in each measure's key signature changes, which carry two bytes of their own.
     */
    private KeySignature readKeySignatureAndOctave(GuitarProByteReader reader, GuitarProVersion version) {
        int accidentals = (byte) reader.readInt();
        if (version.hasOctave()) {
            reader.readUnsignedByte();
        }
        return new KeySignature(accidentals, Mode.MAJOR);
    }
}
