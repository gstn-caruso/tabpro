package com.gstncaruso.tabpro.ui.print;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.page.Orientation;
import com.gstncaruso.tabpro.ui.page.PageBanner;
import com.gstncaruso.tabpro.ui.page.PageMetrics;
import com.gstncaruso.tabpro.ui.page.PageSetup;
import com.gstncaruso.tabpro.ui.page.PaperFormat;
import com.gstncaruso.tabpro.ui.score.Zoom;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScoreSheetsTest {

    private static final PageSetup A4 = PageSetup.defaults();

    @Test
    void aShortScoreFitsInASingleSheet() {
        assertEquals(1, ScoreSheets.pageCount(Score.blank(), A4));
    }

    @Test
    void aLongScoreNeedsSeveralSheets() {
        assertTrue(ScoreSheets.pageCount(scoreWithMeasures(80), A4) > 1);
    }

    @Test
    void everySheetIsExactlyAsBigAsThePaper() {
        Dimension paper = ScoreSheets.pageSize(Zoom.whole(), A4);
        PageMetrics sheet = PageMetrics.of(A4);

        assertEquals(sheet.pageWidth(), paper.width);
        assertEquals(sheet.pageHeight(), paper.height);
        for (BufferedImage page : ScoreSheets.renderPages(scoreWithMeasures(80), Zoom.whole(), A4)) {
            assertEquals(paper.width, page.getWidth());
            assertEquals(paper.height, page.getHeight());
        }
    }

    @Test
    void thereIsOneImagePerSheet() {
        Score score = scoreWithMeasures(80);

        assertEquals(ScoreSheets.pageCount(score, A4), ScoreSheets.renderPages(score, Zoom.whole(), A4).size());
    }

    @Test
    void eachSheetShowsADifferentStretchOfTheScore() {
        List<BufferedImage> sheets = ScoreSheets.renderPages(scoreWithMeasures(80), Zoom.whole(), A4);

        assertNotEquals(pixelsOf(sheets.get(0)), pixelsOf(sheets.get(1)));
    }

    @Test
    void aWiderPaperTakesLessSheets() {
        Score score = scoreWithMeasures(80);

        int onA4 = ScoreSheets.pageCount(score, A4);
        int onA3 = ScoreSheets.pageCount(score, biggerPaper());

        assertTrue(onA3 < onA4, "en A3 entra mas musica por hoja");
    }

    private static PageSetup biggerPaper() {
        return new PageSetup(
                PaperFormat.A3, Orientation.PORTRAIT, 20, 20, 20, 20, 100,
                PageBanner.header(), PageBanner.footer());
    }

    private static String pixelsOf(BufferedImage image) {
        StringBuilder pixels = new StringBuilder();
        for (int y = 0; y < image.getHeight(); y += 7) {
            for (int x = 0; x < image.getWidth(); x += 7) {
                pixels.append(image.getRGB(x, y)).append(' ');
            }
        }
        return pixels.toString();
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
