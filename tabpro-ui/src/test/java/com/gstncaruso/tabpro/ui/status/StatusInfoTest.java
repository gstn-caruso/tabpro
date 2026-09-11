package com.gstncaruso.tabpro.ui.status;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.i18n.TextsDefaultNames;
import com.gstncaruso.tabpro.ui.score.Pagination;
import org.junit.jupiter.api.Test;

class StatusInfoTest {

    @Test
    void aBlankScoreStartsOnMeasureOneAndTrackOne() {
        StatusInfo info = StatusInfo.of(new Editor(Score.blank(new TextsDefaultNames())), Pagination.single());

        assertEquals(1, info.measureNumber());
        assertEquals(1, info.trackNumber());
        assertEquals("Guitarra", info.trackName());
    }

    @Test
    void aFreshMeasureIsTooShort() {
        StatusInfo info = StatusInfo.of(new Editor(Score.blank(new TextsDefaultNames())), Pagination.single());

        assertEquals(MeasureCompleteness.TOO_SHORT, info.completeness());
        assertEquals("1/4", info.measureDurationText());
    }

    @Test
    void aFreshMeasureShowsItsBeatsRatioInGuitarProFormat() {
        StatusInfo info = StatusInfo.of(new Editor(Score.blank(new TextsDefaultNames())), Pagination.single());

        assertEquals("1.000 : 4.000", info.measureBeatsRatioText());
    }

    @Test
    void movingTheCursorMovesThePosition() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        editor.addTrack(Track.standardBass("Bajo"));
        editor.insertMeasure();
        editor.selectTrack(1);
        editor.moveToNextMeasure();

        StatusInfo info = StatusInfo.of(editor, Pagination.single());

        assertEquals(2, info.measureNumber());
        assertEquals(2, info.trackNumber());
        assertEquals("Bajo", info.trackName());
    }

    @Test
    void aBlankScoreHasNoTitleNorAuthorToShow() {
        StatusInfo info = StatusInfo.of(new Editor(Score.blank(new TextsDefaultNames())), Pagination.single());

        assertEquals("Sin título", info.title());
        assertEquals("", info.author());
    }

    @Test
    void showsTheTitleAndTheCreditedAuthors() {
        ScoreInfo scoreInfo = ScoreInfo.empty().withTitle("Sultans of Swing").withMusicAuthor("Mark Knopfler");
        Editor editor = new Editor(new Score(scoreInfo, 120, Score.blank(new TextsDefaultNames()).tracks(), Score.blank(new TextsDefaultNames()).lyrics()));

        StatusInfo info = StatusInfo.of(editor, Pagination.single());

        assertEquals("Sultans of Swing", info.title());
        assertEquals("Música: Mark Knopfler", info.author());
    }

    @Test
    void creditsTheSamePersonForWordsAndMusicOnce() {
        StatusInfo info = statusOf(ScoreInfo.empty().withTitle("Brothers in Arms")
                .withMusicAuthor("Mark Knopfler").withLyricsAuthor("Mark Knopfler"));

        assertEquals("Letra y música: Mark Knopfler", info.author());
    }

    @Test
    void creditsMusicAndWordsOnTwoLinesWhenDifferentPeopleWroteThem() {
        StatusInfo info = statusOf(ScoreInfo.empty().withTitle("Your Song")
                .withMusicAuthor("Elton John").withLyricsAuthor("Bernie Taupin"));

        assertEquals("Música: Elton John\nLetra: Bernie Taupin", info.author());
    }

    @Test
    void creditsOnlyTheWordsWhenNoComposerWasCredited() {
        StatusInfo info = statusOf(ScoreInfo.empty().withTitle("Your Song").withLyricsAuthor("Bernie Taupin"));

        assertEquals("Letra: Bernie Taupin", info.author());
    }

    private static StatusInfo statusOf(ScoreInfo scoreInfo) {
        Editor editor = new Editor(new Score(scoreInfo, 120, Score.blank(new TextsDefaultNames()).tracks(), Score.blank(new TextsDefaultNames()).lyrics()));
        return StatusInfo.of(editor, Pagination.single());
    }

    @Test
    void fallsBackToTheArtistWhenNoAuthorWasCredited() {
        ScoreInfo scoreInfo = ScoreInfo.empty().withTitle("Sultans of Swing").withArtist("Dire Straits");
        Editor editor = new Editor(new Score(scoreInfo, 120, Score.blank(new TextsDefaultNames()).tracks(), Score.blank(new TextsDefaultNames()).lyrics()));

        StatusInfo info = StatusInfo.of(editor, Pagination.single());

        assertEquals("Dire Straits", info.author());
    }

    @Test
    void aScoreOnASingleSheetSaysPageOneOfOne() {
        StatusInfo info = StatusInfo.of(new Editor(Score.blank(new TextsDefaultNames())), Pagination.single());

        assertEquals(1, info.pageNumber());
        assertEquals(1, info.pageCount());
    }

    @Test
    void theStatusSaysWhichSheetTheCursorIsStandingOn() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        for (int i = 0; i < 10; i++) {
            editor.insertMeasure();
        }
        editor.moveTo(8, 0, 1);

        StatusInfo info = StatusInfo.of(editor, Pagination.startingAtMeasures(java.util.List.of(0, 4, 8)));

        assertEquals(3, info.pageNumber());
        assertEquals(3, info.pageCount());
    }

    @Test
    void theStatusCountsEveryMeasureOfTheScore() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        editor.insertMeasure();
        editor.insertMeasure();

        StatusInfo info = StatusInfo.of(editor, Pagination.single());

        assertEquals(3, info.measureCount());
    }
}
