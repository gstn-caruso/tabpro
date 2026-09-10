package com.gstncaruso.tabpro.ui.status;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import org.junit.jupiter.api.Test;

class StatusBarTest {

    @Test
    void ningunControlDeLaBarraDeEstadoQuedaSinNombreNiTooltipAccesible() {
        StatusBar bar = new StatusBar(new Editor(Score.blank()));

        AccessibilityAssertions.assertNoViolations(bar);
    }

    @Test
    void showsThePageThePositionAndTheActiveTrack() {
        Editor editor = new Editor(Score.blank());
        StatusBar bar = new StatusBar(editor);

        assertEquals("Pág. 1/1", bar.pageText());
        assertEquals("001 : 001", bar.positionText());
        assertEquals("Guitarra", bar.trackNameText());
    }

    @Test
    void warnsWhenTheMeasureIsNotComplete() {
        Editor editor = new Editor(Score.blank());
        StatusBar bar = new StatusBar(editor);

        assertEquals("Compás corto", bar.completenessText());
    }

    @Test
    void showsTheTitleAndTheAuthorOnTheRight() {
        ScoreInfo info = ScoreInfo.empty().withTitle("Sultans of Swing").withMusicAuthor("Mark Knopfler");
        Editor editor = new Editor(new Score(info, 120, Score.blank().tracks(), Score.blank().lyrics()));
        StatusBar bar = new StatusBar(editor);

        assertEquals("Sultans of Swing — Musica: Mark Knopfler", bar.creditsText());
    }

    @Test
    void followsTheEditorWhenTheCursorMoves() {
        Editor editor = new Editor(Score.blank());
        editor.addTrack(Track.standardBass("Bajo"));
        StatusBar bar = new StatusBar(editor);

        editor.selectTrack(1);

        assertEquals("001 : 001", bar.positionText());
        assertEquals("Bajo", bar.trackNameText());
    }
}
