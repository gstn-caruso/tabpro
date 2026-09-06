package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.Lyrics;
import com.gstncaruso.tabpro.core.model.LyricLine;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import com.gstncaruso.tabpro.core.model.bars.TripletFeel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lee la cabecera del archivo: los datos de la partitura, la letra, la
 * configuracion de pagina y RSE (que no tienen destino en el modelo de
 * tabpro y se descartan), el tempo y la armadura inicial.
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

    /** Los canales suenan justo despues de la cabecera; las direcciones, justo despues de ellos. */
    void skipDirections(GuitarProByteReader reader, GuitarProVersion version) {
        if (!version.hasDirections()) {
            return;
        }
        reader.skip(19 * 2 + 4);
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

    private KeySignature readKeySignatureAndOctave(GuitarProByteReader reader, GuitarProVersion version) {
        int raw = reader.readInt();
        int accidentals = (byte) raw;
        int modeByte = (raw >> 8) & 0xFF;
        if (version.hasOctave()) {
            reader.readUnsignedByte();
        }
        return new KeySignature(accidentals, modeByte == 0 ? Mode.MAJOR : Mode.MINOR);
    }
}
