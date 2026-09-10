package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.LyricLine;
import com.gstncaruso.tabpro.core.model.Lyrics;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.TripletFeel;

/**
 * Writes the header of a Guitar Pro 4 file: the score info, the lyrics, the tempo, and
 * the initial key signature. GP4 has no page setup or RSE, and the music author has no
 * field of its own: it is lost on export ({@link GuitarProExporter#warningsFor} reports it).
 */
final class GuitarProHeaderWriter {

    void write(GuitarProByteWriter writer, ScoreInfo info, TripletFeel globalTripletFeel, Lyrics lyrics, int tempo,
            KeySignature keySignature) {
        writeScoreInfo(writer, info);
        writer.writeBoolean(globalTripletFeel.swings());
        writeLyrics(writer, lyrics);
        writer.writeInt(tempo);
        writeKeySignatureAndOctave(writer, keySignature);
    }

    private void writeScoreInfo(GuitarProByteWriter writer, ScoreInfo info) {
        writer.writeLengthPrefixedString(info.title());
        writer.writeLengthPrefixedString(info.subtitle());
        writer.writeLengthPrefixedString(info.artist());
        writer.writeLengthPrefixedString(info.album());
        writer.writeLengthPrefixedString(info.lyricsAuthor());
        // GP4 has no field of its own for the music author: it is lost.
        writer.writeLengthPrefixedString(info.copyright());
        writer.writeLengthPrefixedString(info.transcriber());
        writer.writeLengthPrefixedString(info.instructions());
        writeNoticeLines(writer, info.notice());
    }

    private void writeNoticeLines(GuitarProByteWriter writer, String notice) {
        if (notice.isEmpty()) {
            writer.writeInt(0);
            return;
        }
        String[] lines = notice.split("\n", -1);
        writer.writeInt(lines.length);
        for (String line : lines) {
            writer.writeLengthPrefixedString(line);
        }
    }

    private void writeLyrics(GuitarProByteWriter writer, Lyrics lyrics) {
        writer.writeInt(lyrics.trackIndex() + 1);
        for (int i = 0; i < LyricLine.MAX_LINES; i++) {
            LyricLine line = lyrics.line(i);
            writer.writeInt(line.startingMeasure());
            writer.writeIntPrefixedString(line.text());
        }
    }

    /**
     * The initial key signature is the accidentals integer and nothing else. The major
     * or minor mode only exists in each measure's key signature changes, which carry two
     * bytes of their own: the header has none, and putting one there would invent a
     * value nobody recognizes.
     */
    private void writeKeySignatureAndOctave(GuitarProByteWriter writer, KeySignature keySignature) {
        writer.writeInt(keySignature.accidentals());
        writer.writeUnsignedByte(0); // octave: GP4 carries it but the reader discards it.
    }
}
