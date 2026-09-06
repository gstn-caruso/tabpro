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

    /**
     * Los canales suenan justo despues de la cabecera; las direcciones, justo
     * despues de ellos: 19 slots de dos bytes, uno por simbolo de destino
     * (Coda, Doble Coda, Segno, Segno Segno, Fine, en ese orden) y uno por
     * salto (los catorce de {@link DirectionJump}, en el orden en que los
     * declara el enum), cada uno con el compas al que apunta o -1 si no se
     * usa. Cuatro bytes reservados cierran el bloque.
     */
    /**
     * El orden en que el archivo guarda los cinco simbolos de destino. Lo fija el
     * formato de Guitar Pro, no tabpro: por eso se declara aca, donde se lee el
     * archivo, y no se deduce del orden de declaracion del enum.
     */
    static final List<DirectionSymbol> SYMBOL_SLOTS = List.of(
            DirectionSymbol.CODA,
            DirectionSymbol.DOUBLE_CODA,
            DirectionSymbol.SEGNO,
            DirectionSymbol.SEGNO_SEGNO,
            DirectionSymbol.FINE);

    /** El orden en que el archivo guarda los catorce saltos. Lo fija el formato. */
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
     * Un slot de destino: el numero del compas al que apunta -- el primero es el uno, no
     * el cero --, o vacio si no se usa, que es lo que dice un -1.
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
     * La armadura inicial es un entero con signo y nada mas: cuantas alteraciones lleva.
     * El modo mayor o menor no existe aca; solo aparece en los cambios de armadura de
     * cada compas, que traen dos bytes propios.
     */
    private KeySignature readKeySignatureAndOctave(GuitarProByteReader reader, GuitarProVersion version) {
        int accidentals = (byte) reader.readInt();
        if (version.hasOctave()) {
            reader.readUnsignedByte();
        }
        return new KeySignature(accidentals, Mode.MAJOR);
    }
}
