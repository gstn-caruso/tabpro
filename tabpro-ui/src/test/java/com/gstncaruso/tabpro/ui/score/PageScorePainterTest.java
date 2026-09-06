package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.playback.Playhead;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PageScorePainterTest {

    private static final int VIEWPORT_WIDTH = 900;

    @Test
    void pageModeIsAsWideAsAPage() {
        Dimension size = PageScorePainter.canvasSize(Score.blank(), ScoreViewport.of(ViewMode.PAGE, Zoom.whole(), VIEWPORT_WIDTH));

        assertEquals(PageLayout.PAGE_WIDTH, size.width);
    }

    @Test
    void screenVerticalModeFillsTheViewport() {
        Dimension size = PageScorePainter.canvasSize(
                Score.blank(), ScoreViewport.of(ViewMode.SCREEN_VERTICAL, Zoom.whole(), VIEWPORT_WIDTH));

        assertEquals(VIEWPORT_WIDTH, size.width);
    }

    @Test
    void zoomScalesTheCanvas() {
        Dimension whole = PageScorePainter.canvasSize(Score.blank(), ScoreViewport.of(ViewMode.PAGE, Zoom.whole(), VIEWPORT_WIDTH));
        Dimension half = PageScorePainter.canvasSize(Score.blank(), ScoreViewport.of(ViewMode.PAGE, new Zoom(50), VIEWPORT_WIDTH));

        assertEquals(whole.width / 2, half.width);
        assertEquals(whole.height / 2, half.height);
    }

    @Test
    void aTallScoreNeedsMoreThanOnePageInPageMode() {
        Score score = scoreWithMeasures(60);

        Dimension onePage = PageScorePainter.canvasSize(Score.blank(), ScoreViewport.of(ViewMode.PAGE, Zoom.whole(), VIEWPORT_WIDTH));
        Dimension manyPages = PageScorePainter.canvasSize(score, ScoreViewport.of(ViewMode.PAGE, Zoom.whole(), VIEWPORT_WIDTH));

        assertTrue(manyPages.height > onePage.height * 2, "una partitura larga ocupa varias hojas");
    }

    @Test
    void parchmentNeverGrowsInDiscretePageSteps() {
        Score score = scoreWithMeasures(60);

        Dimension parchment = PageScorePainter.canvasSize(score, ScoreViewport.of(ViewMode.PARCHMENT, Zoom.whole(), VIEWPORT_WIDTH));
        Dimension paged = PageScorePainter.canvasSize(score, ScoreViewport.of(ViewMode.PAGE, Zoom.whole(), VIEWPORT_WIDTH));

        assertTrue(parchment.height < paged.height, "el pergamino no reserva aire de mas de una hoja completa");
    }

    @Test
    void paintsEveryViewModeWithoutThrowing() {
        Score score = scoreWithLyricsAndInfo();
        for (ViewMode mode : ViewMode.values()) {
            assertDoesNotThrow(() -> paint(score, mode, Zoom.whole()), "modo " + mode + " no deberia fallar");
        }
    }

    @Test
    void hitTestInPageModeFindsTheSameBeatAsAPlainClick() {
        Score score = Score.blank();
        Dimension size = PageScorePainter.canvasSize(score, ScoreViewport.of(ViewMode.PAGE, Zoom.whole(), VIEWPORT_WIDTH));
        paint(score, ViewMode.PAGE, Zoom.whole());
        int headerAndMargin = PageLayout.PAGE_MARGIN + PageLayout.HEADER_HEIGHT;

        Optional<ScoreLayout.Hit> hit = PageScorePainter.hitTest(
                score, ScoreViewport.of(ViewMode.PAGE, Zoom.whole(), VIEWPORT_WIDTH), 60, headerAndMargin + 20);

        assertTrue(hit.isPresent(), "un clic dentro del area de la hoja tiene que encontrar algo");
        assertEquals(0, hit.get().measure());
        assertTrue(size.height > 0);
    }

    @Test
    void hitTestOutsideAnyPageFindsNothing() {
        Score score = Score.blank();
        paint(score, ViewMode.PAGE, Zoom.whole());

        Optional<ScoreLayout.Hit> hit = PageScorePainter.hitTest(
                score, ScoreViewport.of(ViewMode.PAGE, Zoom.whole(), VIEWPORT_WIDTH), 60, 5);

        assertTrue(hit.isEmpty(), "el encabezado no tiene compases");
    }

    private static void paint(Score score, ViewMode mode, Zoom zoom) {
        Dimension size = PageScorePainter.canvasSize(score, ScoreViewport.of(mode, zoom, VIEWPORT_WIDTH));
        BufferedImage image = new BufferedImage(
                Math.max(1, size.width), Math.max(1, size.height), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        PageScorePainter.paint(
                g, score, new Cursor(0, 0, 0, 1), Playhead.silent(), Optional.empty(),
                ScoreViewport.of(mode, zoom, VIEWPORT_WIDTH));
        g.dispose();
    }

    private static Score scoreWithLyricsAndInfo() {
        Score score = Score.blank().withInfo(
                ScoreInfo.titled("Cancion de prueba").withArtist("Alguien").withCopyright("(c) 2026"));
        return score.withLyrics(
                com.gstncaruso.tabpro.core.model.Lyrics.none().onTrack(0)
                        .withLine(0, com.gstncaruso.tabpro.core.model.LyricLine.empty()
                                .startingAt(1).saying("La-la-la")));
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
