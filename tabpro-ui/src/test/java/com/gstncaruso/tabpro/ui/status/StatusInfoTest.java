package com.gstncaruso.tabpro.ui.status;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.core.model.Track;
import org.junit.jupiter.api.Test;

class StatusInfoTest {

    @Test
    void aBlankScoreStartsOnMeasureOneAndTrackOne() {
        StatusInfo info = StatusInfo.of(new Editor(Score.blank()));

        assertEquals(1, info.measureNumber());
        assertEquals(1, info.trackNumber());
        assertEquals("Guitarra", info.trackName());
    }

    @Test
    void aFreshMeasureIsTooShort() {
        StatusInfo info = StatusInfo.of(new Editor(Score.blank()));

        assertEquals(MeasureCompleteness.TOO_SHORT, info.completeness());
        assertEquals("1/4", info.measureDurationText());
    }

    @Test
    void movingTheCursorMovesThePosition() {
        Editor editor = new Editor(Score.blank());
        editor.addTrack(Track.standardBass("Bajo"));
        editor.insertMeasure();
        editor.selectTrack(1);
        editor.moveToNextMeasure();

        StatusInfo info = StatusInfo.of(editor);

        assertEquals(2, info.measureNumber());
        assertEquals(2, info.trackNumber());
        assertEquals("Bajo", info.trackName());
    }

    @Test
    void aBlankScoreHasNoTitleNorAuthorToShow() {
        StatusInfo info = StatusInfo.of(new Editor(Score.blank()));

        assertEquals("Sin título", info.title());
        assertEquals("", info.author());
    }

    @Test
    void showsTheTitleAndTheCreditedAuthors() {
        ScoreInfo scoreInfo = ScoreInfo.empty().withTitle("Sultans of Swing").withMusicAuthor("Mark Knopfler");
        Editor editor = new Editor(new Score(scoreInfo, 120, Score.blank().tracks(), Score.blank().lyrics()));

        StatusInfo info = StatusInfo.of(editor);

        assertEquals("Sultans of Swing", info.title());
        assertEquals("Musica: Mark Knopfler", info.author());
    }

    @Test
    void fallsBackToTheArtistWhenNoAuthorWasCredited() {
        ScoreInfo scoreInfo = ScoreInfo.empty().withTitle("Sultans of Swing").withArtist("Dire Straits");
        Editor editor = new Editor(new Score(scoreInfo, 120, Score.blank().tracks(), Score.blank().lyrics()));

        StatusInfo info = StatusInfo.of(editor);

        assertEquals("Dire Straits", info.author());
    }

    @Test
    void tabproDoesNotPaginateYetSoThereIsAlwaysOnePage() {
        StatusInfo info = StatusInfo.of(new Editor(Score.blank()));

        assertEquals(1, info.pageNumber());
    }
}
