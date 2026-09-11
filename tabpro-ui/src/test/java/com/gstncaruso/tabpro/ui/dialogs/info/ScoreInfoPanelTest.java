package com.gstncaruso.tabpro.ui.dialogs.info;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class ScoreInfoPanelTest {

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(new ScoreInfoPanel(ScoreInfo.empty()));
    }

    @Test
    void theLyricsAndMusicAuthorLabelsAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Lyrics Author", english.text("score_dialogs.ScoreInfoPanel.lyricsAuthor"));
        assertEquals("Music Author", english.text("score_dialogs.ScoreInfoPanel.musicAuthor"));
    }

    @Test
    void startsWithTheGivenInfo() {
        ScoreInfo info = ScoreInfo.empty().withTitle("Fade to Black").withArtist("Metallica");

        ScoreInfoPanel panel = new ScoreInfoPanel(info);

        assertEquals(info, panel.toScoreInfo());
    }

    @Test
    void reflectsWhateverYouLoadAfterwards() {
        ScoreInfoPanel panel = new ScoreInfoPanel(ScoreInfo.empty());

        panel.apply(ScoreInfo.empty().withTitle("New").withCopyright("2026"));

        ScoreInfo result = panel.toScoreInfo();
        assertEquals("New", result.title());
        assertEquals("2026", result.copyright());
    }

    @Test
    void allTenFieldsRoundTrip() {
        ScoreInfo info = new ScoreInfo(
                "title", "subtitle", "artist", "album", "lyrics author",
                "music author", "copyright", "transcriber", "instructions", "notice");

        ScoreInfoPanel panel = new ScoreInfoPanel(info);

        assertEquals(info, panel.toScoreInfo());
    }
}
