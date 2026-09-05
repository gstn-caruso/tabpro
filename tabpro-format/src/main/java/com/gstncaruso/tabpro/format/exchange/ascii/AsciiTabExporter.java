package com.gstncaruso.tabpro.format.exchange.ascii;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Pasa una partitura a la tablatura de texto clasica: las cuerdas como lineas de guiones, las
 * barras de compas como {@code |}, con el ancho de columna que haga falta para que los trastes
 * de dos digitos no desalineen a las demas cuerdas.
 */
public final class AsciiTabExporter {

    /** Una columna de guion representa esta fraccion de una negra: la base para el ancho de cada beat. */
    private static final long BASE_UNIT_TICKS = Duration.of(NoteValue.SIXTEENTH).ticks();

    public String export(Score score, AsciiTabExportOptions options) {
        return score.tracks().stream().map(track -> exportTrack(track, options)).collect(Collectors.joining("\n\n"));
    }

    public void export(Score score, Path path, AsciiTabExportOptions options) {
        try {
            Files.writeString(path, export(score, options));
        } catch (IOException e) {
            throw new ScoreFileException("no se pudo escribir " + path, e);
        }
    }

    private static String exportTrack(Track track, AsciiTabExportOptions options) {
        List<List<TabColumn>> systems = systemsOf(track, options.columnsPerLine());
        StringBuilder text = new StringBuilder(track.name());
        for (List<TabColumn> system : systems) {
            text.append('\n').append(renderSystem(system, track.stringCount()));
        }
        return text.toString();
    }

    private static List<List<TabColumn>> systemsOf(Track track, int columnsPerLine) {
        List<List<TabColumn>> systems = new ArrayList<>();
        List<TabColumn> current = new ArrayList<>();
        current.add(TabColumn.bar());
        int currentWidth = 1;
        for (Measure measure : track.measures()) {
            List<TabColumn> measureColumns = columnsOf(measure);
            int measureWidth = measureColumns.stream().mapToInt(TabColumn::width).sum();
            if (currentWidth > 1 && currentWidth + measureWidth > columnsPerLine) {
                systems.add(current);
                current = new ArrayList<>();
                current.add(TabColumn.bar());
                currentWidth = 1;
            }
            current.addAll(measureColumns);
            currentWidth += measureWidth;
        }
        systems.add(current);
        return systems;
    }

    private static List<TabColumn> columnsOf(Measure measure) {
        List<TabColumn> columns = new ArrayList<>();
        for (Beat beat : measure.beats()) {
            columns.add(columnOf(beat));
        }
        columns.add(TabColumn.bar());
        return columns;
    }

    private static TabColumn columnOf(Beat beat) {
        Map<Integer, Integer> frets = beat.notes().stream().collect(Collectors.toMap(Note::string, Note::fret));
        return TabColumn.notes(widthOf(beat), frets);
    }

    private static int widthOf(Beat beat) {
        int durationWidth = (int) Math.max(1, Math.round(beat.duration().ticks() / (double) BASE_UNIT_TICKS));
        int maxFret = beat.notes().stream().mapToInt(Note::fret).max().orElse(0);
        int digitsWidth = Integer.toString(maxFret).length();
        return Math.max(durationWidth, digitsWidth);
    }

    private static String renderSystem(List<TabColumn> columns, int stringCount) {
        return IntStream.rangeClosed(1, stringCount)
                .mapToObj(string -> renderStringLine(columns, string))
                .collect(Collectors.joining("\n"));
    }

    private static String renderStringLine(List<TabColumn> columns, int string) {
        StringBuilder line = new StringBuilder();
        for (TabColumn column : columns) {
            line.append(column.isBar() ? "|" : renderCell(column.fretsByString().get(string), column.width()));
        }
        return line.toString();
    }

    private static String renderCell(Integer fret, int width) {
        if (fret == null) {
            return "-".repeat(width);
        }
        String digits = String.valueOf(fret);
        return digits + "-".repeat(Math.max(0, width - digits.length()));
    }
}
