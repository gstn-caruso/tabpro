package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import com.gstncaruso.tabpro.core.model.effects.ParameterChange;
import com.gstncaruso.tabpro.core.model.effects.SoundParameter;
import com.gstncaruso.tabpro.core.playback.Playhead;
import com.gstncaruso.tabpro.ui.page.Orientation;
import com.gstncaruso.tabpro.ui.page.PageBanner;
import com.gstncaruso.tabpro.ui.page.PageElement;
import com.gstncaruso.tabpro.ui.page.PageMetrics;
import com.gstncaruso.tabpro.ui.page.PageSetup;
import com.gstncaruso.tabpro.ui.page.PaperFormat;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PageScorePainterTest {

    private static final int VIEWPORT_WIDTH = 900;
    private static final double ONE_PIXEL_OF_REDONDEO = 1.0;

    @Test
    void pageModeIsAsWideAsTheChosenPaper() {
        Dimension size = PageScorePainter.canvasSize(Score.blank(), pageViewport(paperOf(PaperFormat.LETTER, Orientation.PORTRAIT)));

        assertEquals(850, size.width);
    }

    @Test
    void turningThePaperSidewaysMakesTheSheetWider() {
        Dimension portrait = PageScorePainter.canvasSize(
                Score.blank(), pageViewport(paperOf(PaperFormat.A4, Orientation.PORTRAIT)));
        Dimension landscape = PageScorePainter.canvasSize(
                Score.blank(), pageViewport(paperOf(PaperFormat.A4, Orientation.LANDSCAPE)));

        assertTrue(landscape.width > portrait.width, "una hoja acostada es mas ancha");
    }

    @Test
    void widerMarginsLeaveLessRoomSoTheScoreNeedsMorePages() {
        Score score = scoreWithMeasures(60);

        Dimension narrow = PageScorePainter.canvasSize(score, pageViewport(withMargins(10)));
        Dimension wide = PageScorePainter.canvasSize(score, pageViewport(withMargins(60)));

        assertTrue(wide.height > narrow.height, "con margenes gordos la partitura ocupa mas hojas");
    }

    @Test
    void shrinkingTheScoreFitsItInFewerPages() {
        Score score = scoreWithMeasures(60);

        Dimension full = PageScorePainter.canvasSize(score, pageViewport(sized(100)));
        Dimension half = PageScorePainter.canvasSize(score, pageViewport(sized(50)));

        assertTrue(half.height < full.height, "al 50% entra el doble de musica por hoja");
        assertEquals(full.width, half.width, "el tamano de la partitura no cambia el papel");
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

        assertEquals(whole.width / 2.0, half.width, ONE_PIXEL_OF_REDONDEO);
        assertEquals(whole.height / 2.0, half.height, ONE_PIXEL_OF_REDONDEO);
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
        PageMetrics sheet = PageMetrics.of(PageSetup.defaults());

        Optional<ScoreLayout.Hit> hit = PageScorePainter.hitTest(
                score, ScoreViewport.of(ViewMode.PAGE, Zoom.whole(), VIEWPORT_WIDTH),
                sheet.contentLeft() + 60, sheet.contentTop() + 20);

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

    @Test
    void paintsAnUnusualPaperWithoutThrowing() {
        Score score = scoreWithMeasures(30);
        ScoreViewport viewport = pageViewport(
                new PageSetup(
                        PaperFormat.LEGAL, Orientation.LANDSCAPE, 5, 40, 35, 5, 60,
                        PageBanner.header(), PageBanner.footer()));

        assertDoesNotThrow(() -> paintOn(score, viewport));
    }

    @Test
    void turningOffAnElementOfTheHeaderChangesWhatTheSheetShows() {
        Score score = Score.blank().withInfo(ScoreInfo.titled("Cancion de prueba"));
        PageSetup showingTheTitle = PageSetup.defaults();
        PageSetup hidingTheTitle = new PageSetup(
                PaperFormat.A4, Orientation.PORTRAIT, 20, 20, 20, 20, 100,
                PageBanner.header().with(PageElement.TITLE, false, "[%title]"), PageBanner.footer());

        assertFalse(
                sameSheet(render(score, showingTheTitle), render(score, hidingTheTitle)),
                "destildar el titulo tiene que sacarlo de la hoja");
    }

    @Test
    void theHeaderSaysWhatTheSetupSaysAndNotWhatTheScoreInformationSays() {
        PageSetup fixedHeading = new PageSetup(
                PaperFormat.A4, Orientation.PORTRAIT, 20, 20, 20, 20, 100,
                onlyTheTitleSaying("Cancionero de la casa"), PageBanner.footer());

        BufferedImage one = render(Score.blank().withInfo(ScoreInfo.titled("Sultans of Swing")), fixedHeading);
        BufferedImage another = render(Score.blank().withInfo(ScoreInfo.titled("Money for Nothing")), fixedHeading);

        assertTrue(sameSheet(one, another), "el encabezado es el texto configurado, no el titulo de la partitura");
    }

    /**
     * El rectangulito que anuncia un cambio de parametro es rojo porque el rojo es lo que dice
     * que ahi cambia algo: en la hoja tiene que salir del mismo color que en la pantalla.
     */
    @Test
    void theParameterChangeMarkIsRedOnPaperJustLikeOnScreen() {
        BufferedImage music = musicOf(render(scoreWithAParameterChange(), PageSetup.defaults()));

        assertTrue(paints(music, ScoreColors.PARAMETER_CHANGE), "el cambio de parametro se anuncia en rojo");
    }

    /**
     * La linea de reproduccion es un color propio -no un gris de pantalla- asi que no le toca la
     * inversion del Modo Pagina: tiene que llegar a la hoja del mismo verde con que se ve en
     * pantalla, no invertida ni apagada.
     */
    @Test
    void thePlayingLineIsTheSameGreenOnPaperAsOnScreen() {
        BufferedImage music = musicOf(render(
                scoreWithAParameterChange(), PageSetup.defaults(),
                com.gstncaruso.tabpro.core.playback.Playhead.silent().advancedTo(
                        new com.gstncaruso.tabpro.core.playback.BeatPosition(0, 0, 0))));

        assertTrue(paints(music, ScoreColors.PLAYING), "la linea de reproduccion tiene que verse verde en la hoja");
    }

    /**
     * Lo que en la pantalla oscura es tinta clara, sobre la hoja tiene que ser tinta oscura: si
     * llegara al papel del color con el que se dibuja en pantalla, no se veria nada.
     */
    @Test
    void theScoreIsWrittenInDarkInkOnPaper() {
        BufferedImage music = musicOf(render(scoreWithAParameterChange(), PageSetup.defaults()));

        assertTrue(paints(music, ScoreColors.PAGE_INK), "la partitura se escribe con la tinta de la hoja");
        assertFalse(paints(music, ScoreColors.INK), "y no con la tinta clara de la pantalla");
    }

    /** Solo la musica de la hoja, sin el encabezado ni el pie, que se dibujan aparte. */
    private static BufferedImage musicOf(BufferedImage sheet) {
        PageMetrics paper = PageMetrics.of(PageSetup.defaults());
        return sheet.getSubimage(
                paper.contentLeft(), paper.contentTop(), paper.contentWidth(), paper.contentHeight());
    }

    private static Score scoreWithAParameterChange() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), new Note(1, 0)).withEffects(
                        BeatEffects.none().withParameterChange(
                                ParameterChange.nothing().changing(SoundParameter.PAN, 100))),
                Beat.of(Duration.quarter(), new Note(1, 2)),
                Beat.of(Duration.quarter(), new Note(1, 3)),
                Beat.of(Duration.quarter(), new Note(1, 5))));
        Track guitar = Track.standardGuitar("Guitarra");
        return new Score("", 120, List.of(
                new Track("Guitarra", guitar.tuning(), guitar.channel(), List.of(measure))));
    }

    private static boolean paints(BufferedImage sheet, Color color) {
        for (int y = 0; y < sheet.getHeight(); y++) {
            for (int x = 0; x < sheet.getWidth(); x++) {
                if (sheet.getRGB(x, y) == color.getRGB()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static PageBanner onlyTheTitleSaying(String text) {
        PageBanner banner = PageBanner.header();
        for (PageElement element : PageElement.values()) {
            if (banner.lines().stream().anyMatch(line -> line.element() == element)) {
                banner = banner.with(element, element == PageElement.TITLE, text);
            }
        }
        return banner;
    }

    private static BufferedImage render(Score score, PageSetup setup) {
        return render(score, setup, Playhead.silent());
    }

    private static BufferedImage render(Score score, PageSetup setup, Playhead playhead) {
        ScoreViewport viewport = pageViewport(setup);
        Dimension size = PageScorePainter.canvasSize(score, viewport);
        BufferedImage image = new BufferedImage(
                Math.max(1, size.width), Math.max(1, size.height), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        PageScorePainter.paint(
                g, score, new Cursor(0, 0, 0, 1), playhead, Optional.empty(), viewport);
        g.dispose();
        return image;
    }

    private static boolean sameSheet(BufferedImage one, BufferedImage another) {
        if (one.getWidth() != another.getWidth() || one.getHeight() != another.getHeight()) {
            return false;
        }
        for (int y = 0; y < one.getHeight(); y++) {
            for (int x = 0; x < one.getWidth(); x++) {
                if (one.getRGB(x, y) != another.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static ScoreViewport pageViewport(PageSetup setup) {
        return ScoreViewport.of(ViewMode.PAGE, Zoom.whole(), VIEWPORT_WIDTH).withPageSetup(setup);
    }

    private static PageSetup paperOf(PaperFormat format, Orientation orientation) {
        return new PageSetup(format, orientation, 20, 20, 20, 20, 100, PageBanner.header(), PageBanner.footer());
    }

    private static PageSetup withMargins(int millimetres) {
        return new PageSetup(
                PaperFormat.A4, Orientation.PORTRAIT, millimetres, millimetres, millimetres, millimetres, 100, PageBanner.header(), PageBanner.footer());
    }

    private static PageSetup sized(int scorePercent) {
        return new PageSetup(
                PaperFormat.A4, Orientation.PORTRAIT, 20, 20, 20, 20, scorePercent, PageBanner.header(), PageBanner.footer());
    }

    private static void paint(Score score, ViewMode mode, Zoom zoom) {
        paintOn(score, ScoreViewport.of(mode, zoom, VIEWPORT_WIDTH));
    }

    private static void paintOn(Score score, ScoreViewport viewport) {
        Dimension size = PageScorePainter.canvasSize(score, viewport);
        BufferedImage image = new BufferedImage(
                Math.max(1, size.width), Math.max(1, size.height), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        PageScorePainter.paint(
                g, score, new Cursor(0, 0, 0, 1), Playhead.silent(), Optional.empty(), viewport);
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
