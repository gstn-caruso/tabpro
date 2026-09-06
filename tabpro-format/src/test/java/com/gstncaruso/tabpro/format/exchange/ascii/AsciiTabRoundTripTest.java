package com.gstncaruso.tabpro.format.exchange.ascii;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * La tablatura ASCII no tiene forma de anotar un silencio, asi que una nota seguida de un
 * silencio se ve identica a una nota mas larga: esa ambiguedad la advierte el manual y no hay
 * forma de evitarla leyendo solo el espaciado. Por eso la ida y vuelta que se puede pedir sin
 * perder nada es la de una pista sin silencios, con duraciones multiplo de la corchea.
 */
class AsciiTabRoundTripTest {

    private final AsciiTabExporter exporter = new AsciiTabExporter();
    private final AsciiTabImporter importer = new AsciiTabImporter();

    @Test
    void aTrackWithoutRestsRoundTripsExactlyThroughSpacing() {
        Beat chord = Beat.of(Duration.of(NoteValue.QUARTER), new Note(3, 5), new Note(6, 0));
        Beat first = Beat.of(Duration.of(NoteValue.EIGHTH), new Note(1, 12));
        Beat second = Beat.of(Duration.of(NoteValue.EIGHTH), new Note(2, 3));
        Beat third = Beat.of(Duration.of(NoteValue.QUARTER), new Note(4, 7));
        Beat fourth = Beat.of(Duration.of(NoteValue.QUARTER), new Note(5, 2));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(chord, first, second, third, fourth));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score original = new Score("Prueba", 120, List.of(track));

        String tab = exporter.export(original, AsciiTabExportOptions.standard());
        // el exportador usa una columna por semicorchea (4 por negra): con esa misma cantidad de
        // intervalos por negra la ida y vuelta preserva las duraciones exactas.
        Score imported = importer.importScore(tab, AsciiTabImportOptions.standard().withRhythm(RhythmStrategy.fromSpacing(4)));

        assertEquals(List.of(chord, first, second, third, fourth), imported.track(0).measure(0).beats());
    }
}
