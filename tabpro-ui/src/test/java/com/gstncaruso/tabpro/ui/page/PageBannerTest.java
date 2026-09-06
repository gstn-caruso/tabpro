package com.gstncaruso.tabpro.ui.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.ScoreInfo;
import java.util.List;
import org.junit.jupiter.api.Test;

class PageBannerTest {

    private static final ScoreInfo SULTANS = ScoreInfo.empty()
            .withTitle("Sultans of Swing")
            .withArtist("Dire Straits")
            .withCopyright("(c) 1978 Straitjacket");

    private static final PageFields FOURTH_OF_SEVEN = new PageFields(SULTANS, 4, 7);

    @Test
    void aBannerWithNothingTurnedOnPrintsNothing() {
        PageBanner nothing = allTurnedOff(PageBanner.header());

        assertEquals(List.of(), nothing.fillIn(FOURTH_OF_SEVEN));
    }

    @Test
    void aTurnedOnElementPrintsItsTextWithTheFieldsFilledIn() {
        PageBanner banner = allTurnedOff(PageBanner.header()).with(PageElement.TITLE, true, "[%title]");

        assertEquals(
                List.of(new BannerText(PageElement.TITLE, "Sultans of Swing")),
                banner.fillIn(FOURTH_OF_SEVEN));
    }

    @Test
    void theElementsComeOutInTheOrderOfTheBanner() {
        List<BannerText> printed = PageBanner.header().fillIn(FOURTH_OF_SEVEN);

        assertEquals(
                List.of(PageElement.TITLE, PageElement.ARTIST),
                printed.stream().map(BannerText::element).toList());
    }

    @Test
    void aTurnedOffElementNeverPrintsEvenIfItHasText() {
        PageBanner banner = PageBanner.header().with(PageElement.TITLE, false, "[%title]");

        assertFalse(banner.shows(PageElement.TITLE));
        assertTrue(banner.fillIn(FOURTH_OF_SEVEN).stream().noneMatch(line -> line.element() == PageElement.TITLE));
    }

    @Test
    void anElementWhoseFieldsAreEmptyDoesNotLeaveABlankLine() {
        PageBanner banner = allTurnedOff(PageBanner.header()).with(PageElement.SUBTITLE, true, "[%subtitle]");

        assertEquals(List.of(), banner.fillIn(FOURTH_OF_SEVEN));
    }

    @Test
    void theFooterKnowsWhichSheetItIsPrintingOn() {
        List<BannerText> printed = PageBanner.footer().fillIn(FOURTH_OF_SEVEN);

        assertEquals(
                List.of(
                        new BannerText(PageElement.COPYRIGHT, "(c) 1978 Straitjacket"),
                        new BannerText(PageElement.PAGE_NUMBER, "Pagina 4 de 7")),
                printed);
    }

    @Test
    void changingAnElementLeavesTheRestOfTheBannerAlone() {
        PageBanner banner = PageBanner.header().with(PageElement.TITLE, true, "Cancionero");

        assertEquals("Cancionero", banner.textOf(PageElement.TITLE));
        assertEquals(PageBanner.header().lines().size(), banner.lines().size());
        assertEquals(
                PageBanner.header().textOf(PageElement.ARTIST), banner.textOf(PageElement.ARTIST));
    }

    @Test
    void everyElementOfTheHeaderAndTheFooterCanBeSetUp() {
        List<PageElement> all = java.util.stream.Stream.concat(
                        PageBanner.header().lines().stream(), PageBanner.footer().lines().stream())
                .map(BannerLine::element)
                .toList();

        assertEquals(List.of(PageElement.values()), all);
    }

    private static PageBanner allTurnedOff(PageBanner banner) {
        PageBanner turnedOff = banner;
        for (BannerLine line : banner.lines()) {
            turnedOff = turnedOff.with(line.element(), false, line.text());
        }
        return turnedOff;
    }
}
