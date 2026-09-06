package com.gstncaruso.tabpro.ui.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.ScoreInfo;
import org.junit.jupiter.api.Test;

class PageFieldsTest {

    private static final ScoreInfo SULTANS = ScoreInfo.empty()
            .withTitle("Sultans of Swing")
            .withSubtitle("En vivo en Alchemy")
            .withArtist("Dire Straits")
            .withAlbum("Dire Straits")
            .withMusicAuthor("Mark Knopfler")
            .withLyricsAuthor("Mark Knopfler")
            .withCopyright("(c) 1978 Straitjacket")
            .withTranscriber("Gaston");

    @Test
    void aTextWithoutFieldsIsLeftAlone() {
        assertEquals("Cancionero", fieldsOf(SULTANS).fillIn("Cancionero"));
    }

    @Test
    void anEmptyTextStaysEmpty() {
        assertEquals("", fieldsOf(SULTANS).fillIn(""));
    }

    @Test
    void aFieldBecomesItsValue() {
        assertEquals("Sultans of Swing", fieldsOf(SULTANS).fillIn("[%title]"));
    }

    @Test
    void everyFieldOfTheScoreInformationHasItsValue() {
        PageFields fields = fieldsOf(SULTANS);

        assertEquals("En vivo en Alchemy", fields.fillIn("[%subtitle]"));
        assertEquals("Dire Straits", fields.fillIn("[%artist]"));
        assertEquals("Dire Straits", fields.fillIn("[%album]"));
        assertEquals("Mark Knopfler", fields.fillIn("[%words]"));
        assertEquals("Mark Knopfler", fields.fillIn("[%music]"));
        assertEquals("(c) 1978 Straitjacket", fields.fillIn("[%copyright]"));
        assertEquals("Gaston", fields.fillIn("[%transcriber]"));
    }

    @Test
    void severalFieldsInTheSameTextAreAllReplaced() {
        assertEquals("Dire Straits - Dire Straits", fieldsOf(SULTANS).fillIn("[%artist] - [%album]"));
    }

    @Test
    void theSameFieldTwiceIsReplacedTwice() {
        assertEquals("Sultans of Swing / Sultans of Swing", fieldsOf(SULTANS).fillIn("[%title] / [%title]"));
    }

    @Test
    void thePageAndTheTotalComeFromTheSheetBeingPrinted() {
        PageFields fields = new PageFields(SULTANS, 4, 7);

        assertEquals("Pagina 4 de 7", fields.fillIn("Pagina [%page] de [%pages]"));
    }

    @Test
    void aFieldWithNothingBehindItLeavesTheTextEmpty() {
        assertEquals("", new PageFields(ScoreInfo.empty(), 1, 1).fillIn("[%title]"));
    }

    @Test
    void aFieldWrittenInCapitalsAlsoCounts() {
        assertEquals("Sultans of Swing", fieldsOf(SULTANS).fillIn("[%TITLE]"));
    }

    @Test
    void somethingThatIsNotAFieldIsLeftAsTheUserWroteIt() {
        assertEquals("[%chirimbolo]", fieldsOf(SULTANS).fillIn("[%chirimbolo]"));
        assertEquals("[%title", fieldsOf(SULTANS).fillIn("[%title"));
        assertEquals("%title%", fieldsOf(SULTANS).fillIn("%title%"));
    }

    @Test
    void aTextWhoseFieldsAreAllEmptyHasNothingToSay() {
        PageFields empty = new PageFields(ScoreInfo.empty(), 1, 1);

        assertFalse(empty.hasAnythingToSay("Letra: [%words]"));
        assertFalse(empty.hasAnythingToSay("[%artist] - [%album]"));
    }

    @Test
    void aTextWithOneFieldThatHasAValueIsWorthPrinting() {
        assertTrue(fieldsOf(SULTANS).hasAnythingToSay("Letra y musica: [%music]"));
    }

    @Test
    void aTextWithoutFieldsAlwaysHasSomethingToSay() {
        assertTrue(new PageFields(ScoreInfo.empty(), 1, 1).hasAnythingToSay("Cancionero"));
    }

    private static PageFields fieldsOf(ScoreInfo info) {
        return new PageFields(info, 1, 1);
    }
}
