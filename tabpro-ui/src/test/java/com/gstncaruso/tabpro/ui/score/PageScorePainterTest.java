package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.DiagramPlacement;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
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
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class PageScorePainterTest {

    private static final int VIEWPORT_WIDTH = 900;
    private static final double ONE_PIXEL_OF_REDONDEO = 1.0;

    @Test
    void layoutIsMemoizedForTheSameScoreAndViewport() {
        Score score = scoreWithMeasures(10);
        ScoreViewport viewport = ScoreViewport.of(ViewMode.PAGE, Zoom.whole(), VIEWPORT_WIDTH);

        ScoreLayout first = PageScorePainter.layoutFor(score, viewport);
        ScoreLayout second = PageScorePainter.layoutFor(score, viewport);

        assertSame(first, second, "el mismo score y viewport tienen que reusar el layout ya calculado");
    }

    @Test
    void layoutIsRecalculatedWhenTheScoreChanges() {
        ScoreViewport viewport = ScoreViewport.of(ViewMode.PAGE, Zoom.whole(), VIEWPORT_WIDTH);

        ScoreLayout first = PageScorePainter.layoutFor(Score.blank(), viewport);
        ScoreLayout second = PageScorePainter.layoutFor(Score.blank(), viewport);

        assertNotSame(first, second,
                "dos scores distintos, aunque iguales en contenido, no pueden compartir el cache");
    }

    @Test
    void layoutIsRecalculatedWhenTheViewportChanges() {
        Score score = scoreWithMeasures(10);
        ScoreViewport atWholeZoom = ScoreViewport.of(ViewMode.PAGE, Zoom.whole(), VIEWPORT_WIDTH);
        ScoreViewport atHalfZoom = ScoreViewport.of(ViewMode.PAGE, new Zoom(50), VIEWPORT_WIDTH);

        ScoreLayout first = PageScorePainter.layoutFor(score, atWholeZoom);
        ScoreLayout second = PageScorePainter.layoutFor(score, atHalfZoom);

        assertNotSame(first, second, "cambiar el zoom invalida el layout cacheado");
    }

    @Test
    void chordDiagramsUnderTheTitleAreMemoizedForTheSameScoreAndViewport() {
        Score score = scoreWithMeasures(10);
        ScoreViewport viewport = ScoreViewport.of(ViewMode.PAGE, Zoom.whole(), VIEWPORT_WIDTH);

        var first = PageScorePainter.diagramsUnderTheTitleFor(score, viewport);
        var second = PageScorePainter.diagramsUnderTheTitleFor(score, viewport);

        assertSame(first, second, "el mismo score y viewport tienen que reusar la lista ya calculada");
    }

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
    void pagesOutsideTheClipAreNotPainted() {
        Score score = scoreWithMeasures(200);
        ScoreViewport viewport = pageViewport(PageSetup.defaults());
        int total = PageScorePainter.pageCount(score, viewport);
        assertTrue(total >= 3, "hace falta al menos tres hojas para este test");

        PageMetrics sheet = PageMetrics.of(PageSetup.defaults());
        int stride = sheet.pageHeight() + PageMetrics.PAGE_GAP;
        Rectangle clipOnTheMiddlePage = new Rectangle(0, stride, sheet.pageWidth(), sheet.pageHeight());
        LienzoDePrueba lienzo = new LienzoDePrueba(clipOnTheMiddlePage);

        PageScorePainter.paint(
                lienzo, score, new Cursor(0, 0, 0, 1), Playhead.silent(), Optional.empty(), viewport);

        Set<String> footersPainted = footersPaintedIn(lienzo);

        assertFalse(footersPainted.contains(footerOf(1, total)),
                "la primera hoja, fuera del clip, no tiene que pintarse");
        assertFalse(footersPainted.contains(footerOf(3, total)),
                "la tercera hoja, fuera del clip, no tiene que pintarse");
        assertTrue(footersPainted.contains(footerOf(2, total)),
                "la hoja del medio, adentro del clip, si se tiene que pintar");
    }

    private static String footerOf(int pageNumber, int totalPages) {
        return "Página " + pageNumber + " de " + totalPages;
    }

    @Test
    void renderingOnePageDoesNotPaintMeasuresOfTheOthers() {
        Score score = scoreWithMeasures(200);
        ScoreViewport viewport = pageViewport(PageSetup.defaults());
        Pagination pagination = PageScorePainter.paginationOf(score, viewport);
        assertTrue(pagination.pageCount() >= 3, "hace falta al menos tres hojas para este test");

        int measureOnlyOnTheFirstPage = pagination.firstMeasureOfPage().get(0) + 1;
        int measureOnlyOnTheThirdPage = pagination.firstMeasureOfPage().get(2) + 1;
        LienzoDePrueba lienzo = new LienzoDePrueba();

        PageScorePainter.paintPage(
                lienzo, score, new Cursor(0, 0, 0, 1), Playhead.silent(), Optional.empty(), viewport, 1);

        Set<Integer> painted = measureNumbersPaintedIn(lienzo);
        assertFalse(painted.contains(measureOnlyOnTheFirstPage),
                "la hoja del medio no tiene que tocar un compas que solo esta en la primera");
        assertFalse(painted.contains(measureOnlyOnTheThirdPage),
                "la hoja del medio no tiene que tocar un compas que solo esta en la tercera");
        assertFalse(painted.isEmpty(), "la hoja del medio tiene que pintar sus propios compases");
    }

    private static Set<Integer> measureNumbersPaintedIn(LienzoDePrueba lienzo) {
        return lienzo.textosDibujados().stream()
                .filter(texto -> ScoreFonts.MEASURE_NUMBER_FONT.equals(texto.fuente()))
                .map(texto -> Integer.parseInt(texto.texto()))
                .collect(Collectors.toSet());
    }

    private static Set<String> footersPaintedIn(LienzoDePrueba lienzo) {
        return lienzo.textosDibujados().stream()
                .map(LienzoDePrueba.TextoDibujado::texto)
                .collect(Collectors.toSet());
    }

    @Test
    void turningOffAnElementOfTheHeaderChangesWhatTheSheetShows() {
        Score score = Score.blank().withInfo(ScoreInfo.titled("Cancion de prueba"));
        PageSetup showingTheTitle = PageSetup.defaults();
        PageSetup hidingTheTitle = new PageSetup(
                PaperFormat.A4, Orientation.PORTRAIT, 20, 20, 20, 20, 100,
                PageBanner.header().with(PageElement.TITLE, false, "[%title]"), PageBanner.footer());

        assertFalse(
                renderConLienzo(score, showingTheTitle).coincideEnRegionCon(
                        renderConLienzo(score, hidingTheTitle), headerRegionOf(showingTheTitle)),
                "destildar el titulo tiene que sacarlo de la hoja");
    }

    @Test
    void theHeaderSaysWhatTheSetupSaysAndNotWhatTheScoreInformationSays() {
        PageSetup fixedHeading = new PageSetup(
                PaperFormat.A4, Orientation.PORTRAIT, 20, 20, 20, 20, 100,
                onlyTheTitleSaying("Cancionero de la casa"), PageBanner.footer());

        LienzoDePrueba one = renderConLienzo(Score.blank().withInfo(ScoreInfo.titled("Sultans of Swing")), fixedHeading);
        LienzoDePrueba another = renderConLienzo(Score.blank().withInfo(ScoreInfo.titled("Money for Nothing")), fixedHeading);

        assertTrue(one.coincideEnRegionCon(another, headerRegionOf(fixedHeading)),
                "el encabezado es el texto configurado, no el titulo de la partitura");
    }

    @Test
    void theParameterChangeMarkIsRedOnPaperJustLikeOnScreen() {
        LienzoDePrueba lienzo = renderConLienzo(scoreWithAParameterChange(), PageSetup.defaults());

        assertTrue(lienzo.dibujaColorEnRegion(ScoreColors.PARAMETER_CHANGE, musicRegionOf(PageSetup.defaults())),
                "el cambio de parametro se anuncia en rojo");
    }

    @Test
    void thePlayingLineIsTheSameGreenOnPaperAsOnScreen() {
        LienzoDePrueba lienzo = renderConLienzo(
                scoreWithAParameterChange(), PageSetup.defaults(),
                Playhead.silent().advancedTo(new com.gstncaruso.tabpro.core.playback.BeatPosition(0, 0, 0)));

        assertTrue(lienzo.dibujaColorEnRegion(ScoreColors.PLAYING, musicRegionOf(PageSetup.defaults())),
                "la linea de reproduccion tiene que verse verde en la hoja");
    }

    @Test
    void theEditingCursorIsTheSameRedOnPaperAsOnScreen() {
        LienzoDePrueba lienzo = renderConLienzo(scoreWithAParameterChange(), PageSetup.defaults());

        assertTrue(lienzo.dibujaColorEnRegion(ScoreColors.CURSOR, musicRegionOf(PageSetup.defaults())),
                "el cursor de edicion tiene que verse rojo en la hoja");
    }

    @Test
    void theScoreIsWrittenInDarkInkOnPaper() {
        LienzoDePrueba lienzo = renderConLienzo(scoreWithAParameterChange(), PageSetup.defaults());
        Rectangle music = musicRegionOf(PageSetup.defaults());

        assertTrue(lienzo.dibujaColorEnRegion(ScoreColors.PAGE_INK, music), "la partitura se escribe con la tinta de la hoja");
        assertFalse(lienzo.dibujaColorEnRegion(ScoreColors.INK, music), "y no con la tinta clara de la pantalla");
    }

    @Test
    void theHeaderGetsTheDiagramWhenThePlacementAsksForBothSides() {
        BufferedImage aboveOnly = render(scoreWithChordPlacement(DiagramPlacement.ABOVE_THE_STAFF), PageSetup.defaults());
        BufferedImage both = render(scoreWithChordPlacement(DiagramPlacement.BOTH), PageSetup.defaults());

        assertTrue(sameSheet(musicOf(aboveOnly), musicOf(both)),
                "el diagrama arriba del pentagrama no tiene que cambiar entre ABOVE_THE_STAFF y BOTH");
        assertFalse(sameSheet(headerOf(aboveOnly), headerOf(both)),
                "BOTH tiene que agregar el diagrama tambien en el encabezado");
    }

    @Test
    void theHeaderTellsUnderTheTitleApartFromHidden() {
        BufferedImage hidden = render(scoreWithChordPlacement(DiagramPlacement.HIDDEN), PageSetup.defaults());
        BufferedImage underTheTitle = render(scoreWithChordPlacement(DiagramPlacement.UNDER_THE_TITLE), PageSetup.defaults());

        assertTrue(sameSheet(musicOf(hidden), musicOf(underTheTitle)),
                "ninguno de los dos muestra el diagrama arriba del pentagrama");
        assertFalse(sameSheet(headerOf(hidden), headerOf(underTheTitle)),
                "UNDER_THE_TITLE tiene que mostrar el diagrama en el encabezado, HIDDEN no");
    }

    private static Score scoreWithChordPlacement(DiagramPlacement placement) {
        ChordDiagram am = ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1));
        Beat chordBeat = Beat.rest(Duration.quarter()).withEffects(BeatEffects.none().withChord(am));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                chordBeat, Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter())));
        Track track = Track.standardGuitar("Guitarra")
                .withMeasures(List.of(measure))
                .mappingSettings(settings -> settings.withDisplay(settings.display().withDiagrams(placement)));
        return new Score("", 120, List.of(track));
    }

    private static BufferedImage musicOf(BufferedImage sheet) {
        PageMetrics paper = PageMetrics.of(PageSetup.defaults());
        return sheet.getSubimage(
                paper.contentLeft(), paper.contentTop(), paper.contentWidth(), paper.contentHeight());
    }

    private static BufferedImage headerOf(BufferedImage sheet) {
        PageMetrics paper = PageMetrics.of(PageSetup.defaults());
        return sheet.getSubimage(0, 0, sheet.getWidth(), paper.contentTop());
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

    private static PageBanner onlyTheTitleSaying(String text) {
        PageBanner banner = PageBanner.header();
        for (PageElement element : PageElement.values()) {
            if (banner.lines().stream().anyMatch(line -> line.element() == element)) {
                banner = banner.with(element, element == PageElement.TITLE, text);
            }
        }
        return banner;
    }

    private static LienzoDePrueba renderConLienzo(Score score, PageSetup setup) {
        return renderConLienzo(score, setup, Playhead.silent());
    }

    private static LienzoDePrueba renderConLienzo(Score score, PageSetup setup, Playhead playhead) {
        ScoreViewport viewport = pageViewport(setup);
        LienzoDePrueba lienzo = new LienzoDePrueba();
        PageScorePainter.paint(lienzo, score, new Cursor(0, 0, 0, 1), playhead, Optional.empty(), viewport);
        return lienzo;
    }

    private static Rectangle musicRegionOf(PageSetup setup) {
        PageMetrics paper = PageMetrics.of(setup);
        return new Rectangle(paper.contentLeft(), paper.contentTop(), paper.contentWidth(), paper.contentHeight());
    }

    private static Rectangle headerRegionOf(PageSetup setup) {
        PageMetrics paper = PageMetrics.of(setup);
        return new Rectangle(0, 0, paper.pageWidth(), paper.contentTop());
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
        PageScorePainter.paint(
                new LienzoDePrueba(), score, new Cursor(0, 0, 0, 1), Playhead.silent(), Optional.empty(), viewport);
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
