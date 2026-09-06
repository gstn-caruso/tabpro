package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PaginationTest {

    @Test
    void aScoreOnASingleSheetHasEverythingOnPageOne() {
        Pagination single = Pagination.single();

        assertEquals(1, single.pageCount());
        assertEquals(1, single.pageOf(0));
        assertEquals(1, single.pageOf(132));
    }

    @Test
    void aMeasureFallsOnTheSheetThatStartedBeforeIt() {
        Pagination pages = Pagination.startingAtMeasures(List.of(0, 8, 15));

        assertEquals(3, pages.pageCount());
        assertEquals(1, pages.pageOf(0));
        assertEquals(1, pages.pageOf(7));
        assertEquals(2, pages.pageOf(8));
        assertEquals(2, pages.pageOf(14));
        assertEquals(3, pages.pageOf(15));
    }

    @Test
    void aMeasureBeyondTheScoreFallsOnTheLastSheet() {
        Pagination pages = Pagination.startingAtMeasures(List.of(0, 8, 15));

        assertEquals(3, pages.pageOf(9999));
    }

    @Test
    void aMeasureBeforeTheFirstOneFallsOnTheFirstSheet() {
        Pagination pages = Pagination.startingAtMeasures(List.of(0, 8, 15));

        assertEquals(1, pages.pageOf(-1));
    }

    @Test
    void aLongScoreIsSpreadOverSeveralSheets() {
        Pagination pages = paginationOf(scoreWithMeasures(80));

        assertTrue(pages.pageCount() > 1, "una partitura larga no entra en una sola hoja");
        assertEquals(1, pages.pageOf(0));
        assertTrue(pages.pageOf(79) > 1, "los ultimos compases caen en una hoja posterior");
    }

    @Test
    void everyMeasureOfTheScoreFallsOnSomeSheet() {
        Score score = scoreWithMeasures(80);
        Pagination pages = paginationOf(score);

        for (int measure = 0; measure < score.measureCount(); measure++) {
            int page = pages.pageOf(measure);
            assertTrue(page >= 1 && page <= pages.pageCount(), "el compas " + measure + " cayo en la hoja " + page);
        }
    }

    private static Pagination paginationOf(Score score) {
        return PageScorePainter.paginationOf(score, ScoreViewport.of(ViewMode.PAGE, Zoom.whole(), 900));
    }

    private static Score scoreWithMeasures(int count) {
        List<Measure> measures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            measures.add(new Measure(TimeSignature.fourFour(), List.of(
                    Beat.of(Duration.quarter(), new Note(1, i % 5)),
                    Beat.of(Duration.quarter(), new Note(1, i % 5)),
                    Beat.of(Duration.quarter(), new Note(1, i % 5)),
                    Beat.of(Duration.quarter(), new Note(1, i % 5)))));
        }
        Track guitar = Track.standardGuitar("Guitarra");
        return new Score("", 120, List.of(new Track("Guitarra", guitar.tuning(), guitar.channel(), measures)));
    }
}
