package com.gstncaruso.tabpro.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Beat;
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

class ScoreDtoTest {

    @Test
    void roundTripsABlankScore() {
        Score score = Score.blank();

        ScoreDto dto = ScoreDto.from(score);

        assertEquals(score, dto.toScore());
    }

    @Test
    void roundTripsNotesAndRests() {
        Beat beatWithNotes = Beat.of(Duration.quarter(), new Note(6, 0), new Note(5, 2));
        Beat rest = Beat.rest(Duration.quarter());
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beatWithNotes, rest));
        Track track = new Track("Guitarra", Tuning.standard(), 25, List.of(measure));
        Score score = new Score("Prueba", 120, List.of(track));

        ScoreDto dto = ScoreDto.from(score);

        assertEquals(score, dto.toScore());
    }

    @Test
    void roundTripsDottedDurations() {
        Duration dottedEighth = new Duration(NoteValue.EIGHTH, true);
        Beat beat = Beat.rest(dottedEighth);
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beat));
        Track track = new Track("Guitarra", Tuning.standard(), 25, List.of(measure));
        Score score = new Score("Prueba", 120, List.of(track));

        ScoreDto dto = ScoreDto.from(score);

        assertEquals(score, dto.toScore());
    }

    @Test
    void roundTripsSeveralMeasures() {
        Measure firstMeasure = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        Measure secondMeasure = new Measure(TimeSignature.fourFour(),
                List.of(Beat.of(Duration.quarter(), new Note(1, 3))));
        Track track = new Track("Guitarra", Tuning.standard(), 25, List.of(firstMeasure, secondMeasure));
        Score score = new Score("Prueba", 120, List.of(track));

        ScoreDto dto = ScoreDto.from(score);

        assertEquals(score, dto.toScore());
    }

    @Test
    void roundTripsSeveralTracks() {
        Track guitar = Track.standardGuitar("Guitarra");
        Track bass = Track.standardGuitar("Bajo");
        Score score = new Score("Prueba", 120, List.of(guitar, bass));

        ScoreDto dto = ScoreDto.from(score);

        assertEquals(score, dto.toScore());
    }

    @Test
    void rejectsAStringBeyondTheTuning() {
        NoteDto note = new NoteDto(7, 0);
        BeatDto beat = new BeatDto(4, false, List.of(note));
        MeasureDto measure = new MeasureDto(new TimeSignatureDto(4, 4), List.of(beat));
        TrackDto track = new TrackDto("Guitarra", 25, List.of(64, 59, 55, 50, 45, 40), List.of(measure));
        ScoreDto dto = new ScoreDto(ScoreDto.CURRENT_FORMAT, "Prueba", 120, List.of(track));

        assertThrows(ScoreFileException.class, dto::toScore);
    }
}
