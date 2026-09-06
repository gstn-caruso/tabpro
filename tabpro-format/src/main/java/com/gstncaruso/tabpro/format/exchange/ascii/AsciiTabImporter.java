package com.gstncaruso.tabpro.format.exchange.ascii;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.TuningLibrary;
import com.gstncaruso.tabpro.format.exchange.DurationTicks;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Extrae una tablatura ASCII de un texto cualquiera: las cuerdas son lineas de guiones, el
 * cambio de compas es una barra en todas las cuerdas a la vez, y el texto puede traer
 * comentarios alrededor, que se ignoran. Varios bloques seguidos con la misma cantidad de
 * cuerdas son sistemas de la misma pista; un cambio en la cantidad de cuerdas empieza una pista
 * nueva.
 */
public final class AsciiTabImporter {

    public Score importScore(Path path, AsciiTabImportOptions options) {
        try {
            return importScore(Files.readString(path), options);
        } catch (IOException e) {
            throw new ScoreFileException("no se pudo leer " + path, e);
        }
    }

    public Score importScore(String text, AsciiTabImportOptions options) {
        List<List<String>> blocks = AsciiTabBlocks.blocksIn(text);
        if (blocks.isEmpty()) {
            throw new ScoreFileException("el texto no tiene ninguna tablatura reconocible");
        }
        try {
            List<List<List<String>>> tracks = groupIntoTracks(blocks);
            List<Track> result = new ArrayList<>();
            for (int index = 0; index < tracks.size(); index++) {
                result.add(trackFrom(tracks.get(index), index, options));
            }
            return new Score("", 120, result);
        } catch (IllegalArgumentException e) {
            throw new ScoreFileException("la tablatura no se pudo interpretar: " + e.getMessage(), e);
        }
    }

    /**
     * El import de la ventana de ASCII: cae sobre la pista activa, como pide el manual, en vez de
     * reemplazar toda la partitura. Si el texto trae bloques de otra cantidad de cuerdas a la
     * mitad, solo se usa el primer grupo (el que coincide con el resto del texto) -- el import
     * cae sobre una sola pista, y el resto se ignora.
     */
    public Track importInto(Track target, String text, AsciiTabImportOptions options) {
        List<List<String>> blocks = AsciiTabBlocks.blocksIn(text);
        if (blocks.isEmpty()) {
            throw new ScoreFileException("el texto no tiene ninguna tablatura reconocible");
        }
        try {
            List<List<String>> firstGroup = groupIntoTracks(blocks).getFirst();
            return target.withMeasures(measuresFrom(firstGroup, options));
        } catch (IllegalArgumentException e) {
            throw new ScoreFileException("la tablatura no se pudo interpretar: " + e.getMessage(), e);
        }
    }

    /** Bloques seguidos con la misma cantidad de cuerdas son sistemas de una misma pista. */
    private static List<List<List<String>>> groupIntoTracks(List<List<String>> blocks) {
        List<List<List<String>>> tracks = new ArrayList<>();
        for (List<String> block : blocks) {
            if (!tracks.isEmpty() && tracks.getLast().getFirst().size() == block.size()) {
                tracks.getLast().add(block);
            } else {
                List<List<String>> track = new ArrayList<>();
                track.add(block);
                tracks.add(track);
            }
        }
        return tracks;
    }

    private static Track trackFrom(List<List<String>> blocks, int index, AsciiTabImportOptions options) {
        int stringCount = blocks.getFirst().size();
        List<Measure> measures = measuresFrom(blocks, options);
        Tuning tuning = tuningForStringCount(stringCount);
        int program = tuning.equals(Tuning.standardBass()) ? Track.BASS_PROGRAM : Track.GUITAR_PROGRAM;
        return new Track("Pista " + (index + 1), tuning, Channel.playing(program), measures);
    }

    /** Los compases de un grupo de bloques homogeneo (misma cantidad de cuerdas en todos). */
    private static List<Measure> measuresFrom(List<List<String>> blocks, AsciiTabImportOptions options) {
        List<String> lines = concatenate(blocks);
        List<int[]> cells = cellsOf(barColumnsOf(lines), lines.getFirst().length());
        List<Measure> measures = cells.stream()
                .map(cell -> measureFrom(cell[0], cell[1], lines, options))
                .toList();
        return measures.isEmpty()
                ? List.of(Measure.empty(options.defaultTimeSignature(), Duration.quarter()))
                : measures;
    }

    /** Pega, cuerda por cuerda, los bloques de una misma pista uno atras del otro. */
    private static List<String> concatenate(List<List<String>> blocks) {
        int stringCount = blocks.getFirst().size();
        StringBuilder[] lines = new StringBuilder[stringCount];
        for (int string = 0; string < stringCount; string++) {
            lines[string] = new StringBuilder();
        }
        for (List<String> block : blocks) {
            int width = block.stream().mapToInt(String::length).max().orElse(0);
            for (int string = 0; string < stringCount; string++) {
                String line = block.get(string);
                lines[string].append(line).append("-".repeat(Math.max(0, width - line.length())));
            }
        }
        return java.util.Arrays.stream(lines).map(StringBuilder::toString).toList();
    }

    /** Las columnas en las que absolutamente todas las cuerdas muestran una barra a la vez. */
    private static List<Integer> barColumnsOf(List<String> lines) {
        int length = lines.getFirst().length();
        List<Integer> bars = new ArrayList<>();
        for (int column = 0; column < length; column++) {
            int currentColumn = column;
            if (lines.stream().allMatch(line -> line.charAt(currentColumn) == '|')) {
                bars.add(column);
            }
        }
        return bars;
    }

    /** Un compas por cada tramo entre dos barras (o entre el borde del texto y la barra mas cercana). */
    private static List<int[]> cellsOf(List<Integer> barColumns, int length) {
        List<Integer> boundaries = new ArrayList<>();
        boundaries.add(-1);
        boundaries.addAll(barColumns);
        boundaries.add(length);
        List<int[]> cells = new ArrayList<>();
        for (int i = 0; i < boundaries.size() - 1; i++) {
            int from = boundaries.get(i) + 1;
            int to = boundaries.get(i + 1);
            if (to > from) {
                cells.add(new int[] {from, to});
            }
        }
        return cells;
    }

    private static Measure measureFrom(int from, int to, List<String> lines, AsciiTabImportOptions options) {
        List<Attack> attacks = attacksIn(from, to, lines);
        TimeSignature timeSignature = options.defaultTimeSignature();
        List<Beat> beats = switch (options.rhythm()) {
            case RhythmStrategy.Fixed fixed -> beatsWithFixedRhythm(attacks, fixed.duration(), timeSignature);
            case RhythmStrategy.FromSpacing fromSpacing ->
                    beatsFromSpacing(attacks, fromSpacing.intervalsPerQuarterNote(), timeSignature);
        };
        return new Measure(timeSignature, beats);
    }

    /** Los golpes de un tramo, agrupados por columna: la misma columna en varias cuerdas es un acorde. */
    private static List<Attack> attacksIn(int from, int to, List<String> lines) {
        Map<Integer, Map<Integer, Integer>> byStart = new TreeMap<>();
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            int string = index + 1;
            int column = from;
            while (column < to) {
                char character = line.charAt(column);
                if (Character.isDigit(character)) {
                    int start = column;
                    while (column < to && Character.isDigit(line.charAt(column))) {
                        column++;
                    }
                    int fret = Integer.parseInt(line.substring(start, column));
                    byStart.computeIfAbsent(start - from, key -> new java.util.LinkedHashMap<>()).put(string, fret);
                } else {
                    column++;
                }
            }
        }
        return byStart.entrySet().stream().map(entry -> new Attack(entry.getKey(), entry.getValue())).toList();
    }

    private static List<Beat> beatsWithFixedRhythm(List<Attack> attacks, Duration duration, TimeSignature timeSignature) {
        List<Beat> beats = new ArrayList<>();
        for (Attack attack : attacks) {
            beats.add(new Beat(duration, notesOf(attack)));
        }
        long remaining = timeSignature.ticksPerMeasure() - (long) attacks.size() * duration.ticks();
        if (remaining > 0) {
            beats.addAll(restsFor(remaining));
        }
        return beats;
    }

    /**
     * El ritmo {@code <variable>} del manual: cada columna vale una fraccion fija de negra (la
     * grilla que fija intervalsPerQuarterNote, la "segunda lista"), sin importar cuantas columnas
     * tenga el tramo -- cuanto mas lejos la siguiente nota, mas larga la anterior.
     */
    private static List<Beat> beatsFromSpacing(List<Attack> attacks, int intervalsPerQuarterNote, TimeSignature timeSignature) {
        long ticksPerMeasure = timeSignature.ticksPerMeasure();
        List<Beat> beats = new ArrayList<>();
        long position = 0;
        for (int i = 0; i < attacks.size(); i++) {
            Attack attack = attacks.get(i);
            long startTick = tickOf(attack.localStart(), intervalsPerQuarterNote);
            if (startTick > position) {
                beats.addAll(restsFor(startTick - position));
            }
            long nextTick = i + 1 < attacks.size() ? tickOf(attacks.get(i + 1).localStart(), intervalsPerQuarterNote) : ticksPerMeasure;
            Duration duration = DurationTicks.nearestTo(nextTick - startTick);
            beats.add(new Beat(duration, notesOf(attack)));
            position = startTick + duration.ticks();
        }
        if (position < ticksPerMeasure) {
            beats.addAll(restsFor(ticksPerMeasure - position));
        }
        return beats;
    }

    private static long tickOf(int localColumn, int intervalsPerQuarterNote) {
        return Math.round(localColumn * (Duration.TICKS_PER_QUARTER / (double) intervalsPerQuarterNote));
    }

    private static List<Beat> restsFor(long ticks) {
        return DurationTicks.decompose(ticks).stream().map(Beat::rest).toList();
    }

    private static List<Note> notesOf(Attack attack) {
        return attack.fretsByString().entrySet().stream().map(entry -> new Note(entry.getKey(), entry.getValue())).toList();
    }

    private static Tuning tuningForStringCount(int stringCount) {
        if (stringCount == Tuning.standard().stringCount()) {
            return Tuning.standard();
        }
        if (stringCount == Tuning.standardBass().stringCount()) {
            return Tuning.standardBass();
        }
        List<Tuning> known = TuningLibrary.withStringCount(stringCount);
        return known.isEmpty() ? Tuning.standard().withStringCount(stringCount) : known.getFirst();
    }
}
