package com.gstncaruso.tabpro.format.exchange.ascii;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AsciiTabExporterTest {

    private final AsciiTabExporter exporter = new AsciiTabExporter();

    @Test
    void drawsEachStringAsADashLineWithBarsAtTheEdges() {
        Measure measure = Measure.empty(TimeSignature.fourFour(), Duration.of(NoteValue.WHOLE));
        Track track = new Track("Guitarra", Tuning.standard(), com.gstncaruso.tabpro.core.model.Channel.playing(25), List.of(measure));
        Score score = new Score("Prueba", 120, List.of(track));

        String tab = exporter.export(score, AsciiTabExportOptions.standard());

        List<String> lines = linesOf(tab);
        assertEquals(6, stringLinesOf(lines).size());
        for (String line : stringLinesOf(lines)) {
            assertTrue(line.startsWith("|"), "cada linea de cuerda arranca con una barra: " + line);
            assertTrue(line.endsWith("|"), "cada linea de cuerda termina con una barra: " + line);
            assertTrue(line.chars().allMatch(c -> c == '-' || c == '|'), "solo guiones y barras: " + line);
        }
    }

    @Test
    void placesEachFretUnderItsOwnString() {
        Beat beat = Beat.of(Duration.of(NoteValue.QUARTER), new Note(3, 5), new Note(6, 0));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beat, Beat.rest(new Duration(NoteValue.HALF, true))));
        Track track = new Track("Guitarra", Tuning.standard(), com.gstncaruso.tabpro.core.model.Channel.playing(25), List.of(measure));
        Score score = new Score("Prueba", 120, List.of(track));

        String tab = exporter.export(score, AsciiTabExportOptions.standard());

        List<String> stringLines = stringLinesOf(linesOf(tab));
        assertTrue(stringLines.get(2).contains("5"), "la cuerda 3 tiene el traste 5: " + stringLines.get(2));
        assertTrue(stringLines.get(5).contains("0"), "la cuerda 6 tiene el traste 0: " + stringLines.get(5));
        assertTrue(stringLines.get(0).chars().noneMatch(Character::isDigit), "la cuerda 1 no suena: " + stringLines.get(0));
    }

    @Test
    void keepsTwoDigitFretsAligned() {
        Beat beat = Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 12), new Note(6, 0));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beat, Beat.rest(new Duration(NoteValue.HALF, true))));
        Track track = new Track("Guitarra", Tuning.standard(), com.gstncaruso.tabpro.core.model.Channel.playing(25), List.of(measure));
        Score score = new Score("Prueba", 120, List.of(track));

        String tab = exporter.export(score, AsciiTabExportOptions.standard());

        List<String> stringLines = stringLinesOf(linesOf(tab));
        // las dos lineas tienen que medir lo mismo aunque una lleve un numero de dos digitos
        assertEquals(stringLines.get(0).length(), stringLines.get(5).length());
    }

    @Test
    void wrapsIntoANewSystemWhenAMeasureWouldNotFit() {
        List<Measure> measures = Arrays.asList(
                new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.of(NoteValue.WHOLE), new Note(6, 0)))),
                new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.of(NoteValue.WHOLE), new Note(6, 1)))),
                new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.of(NoteValue.WHOLE), new Note(6, 2)))));
        Track track = new Track("Guitarra", Tuning.standard(), com.gstncaruso.tabpro.core.model.Channel.playing(25), measures);
        Score score = new Score("Prueba", 120, List.of(track));
        AsciiTabExportOptions narrow = new AsciiTabExportOptions(20);

        String tab = exporter.export(score, narrow);

        List<String> stringLines = stringLinesOf(linesOf(tab));
        assertTrue(stringLines.size() > 6, "con columnas angostas hacen falta varios sistemas: " + tab);
        for (String line : stringLines) {
            assertTrue(line.length() <= narrow.columnsPerLine(), "ninguna linea supera el ancho pedido: " + line);
        }
    }

    @Test
    void includesTheTrackNameAsAHeading() {
        Track track = Track.standardBass("Bajo");
        Score score = new Score("Prueba", 120, List.of(track));

        String tab = exporter.export(score, AsciiTabExportOptions.standard());

        assertTrue(tab.contains("Bajo"));
    }

    private static List<String> linesOf(String text) {
        return Arrays.stream(text.split("\n", -1)).toList();
    }

    private static List<String> stringLinesOf(List<String> lines) {
        return lines.stream().filter(line -> !line.isBlank() && line.chars().allMatch(c -> c == '-' || c == '|' || Character.isDigit(c))).toList();
    }
}
