package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.LyricLine;
import com.gstncaruso.tabpro.core.model.Lyrics;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.TripletFeel;

/**
 * Escribe la cabecera de un archivo Guitar Pro 4: los datos de la partitura, la letra,
 * el tempo y la armadura inicial. GP4 no tiene page setup ni RSE, y el autor de la
 * musica no tiene campo propio: se pierde en la exportacion (lo avisa
 * {@link GuitarProExporter#warningsFor}).
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
        // GP4 no tiene un campo propio para el autor de la musica: se pierde.
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
     * La armadura inicial es el entero de alteraciones y nada mas. El modo mayor o menor
     * solo existe en los cambios de armadura de cada compas, que traen dos bytes propios:
     * el de la cabecera no lo tiene, y meterlo ahi inventa un valor que nadie reconoce.
     */
    private void writeKeySignatureAndOctave(GuitarProByteWriter writer, KeySignature keySignature) {
        writer.writeInt(keySignature.accidentals());
        writer.writeUnsignedByte(0); // octava: GP4 la trae pero el lector la descarta.
    }
}
