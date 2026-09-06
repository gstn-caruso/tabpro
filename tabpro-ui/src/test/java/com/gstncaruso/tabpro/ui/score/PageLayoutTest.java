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

class PageLayoutTest {

    private static final int NARROW_WIDTH = 300;

    @Test
    void aParchmentIsAlwaysOnePage() {
        ScoreLayout layout = ScoreLayout.of(scoreWithMeasures(40), NARROW_WIDTH);

        PageLayout parchment = PageLayout.of(layout, false);

        assertEquals(1, parchment.pageCount());
        assertEquals(0, parchment.firstSystemOf(0));
        assertEquals(layout.systemCount() - 1, parchment.lastSystemOf(0));
    }

    @Test
    void aTallScoreSplitsIntoSeveralPages() {
        ScoreLayout layout = ScoreLayout.of(scoreWithMeasures(60), NARROW_WIDTH);

        PageLayout page = PageLayout.of(layout, true);

        assertTrue(page.pageCount() > 1, "una partitura larga tiene que ocupar mas de una hoja");
    }

    @Test
    void everySystemBelongsToExactlyOnePage() {
        ScoreLayout layout = ScoreLayout.of(scoreWithMeasures(60), NARROW_WIDTH);
        PageLayout page = PageLayout.of(layout, true);

        for (int system = 0; system < layout.systemCount(); system++) {
            int owningPage = page.pageOf(system);
            assertTrue(page.firstSystemOf(owningPage) <= system && system <= page.lastSystemOf(owningPage));
        }
    }

    @Test
    void pagesAreConsecutiveAndCoverEverySystem() {
        ScoreLayout layout = ScoreLayout.of(scoreWithMeasures(60), NARROW_WIDTH);
        PageLayout page = PageLayout.of(layout, true);

        assertEquals(0, page.firstSystemOf(0));
        for (int p = 0; p < page.pageCount() - 1; p++) {
            assertEquals(page.lastSystemOf(p) + 1, page.firstSystemOf(p + 1));
        }
        assertEquals(layout.systemCount() - 1, page.lastSystemOf(page.pageCount() - 1));
    }

    @Test
    void aShortScoreFitsInOnePage() {
        ScoreLayout layout = ScoreLayout.of(Score.blank(), 4000);

        PageLayout page = PageLayout.of(layout, true);

        assertEquals(1, page.pageCount());
    }

    private static Score scoreWithMeasures(int count) {
        List<Measure> measures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            measures.add(quarters());
        }
        Track guitar = Track.standardGuitar("Guitarra");
        return new Score("", 120, List.of(new Track("Guitarra", guitar.tuning(), guitar.channel(), measures)));
    }

    private static Measure quarters() {
        List<Beat> beats = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            beats.add(Beat.of(Duration.quarter(), new Note(1, i % 5)));
        }
        return new Measure(TimeSignature.fourFour(), beats);
    }
}
