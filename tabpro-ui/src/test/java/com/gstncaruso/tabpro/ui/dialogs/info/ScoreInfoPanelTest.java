package com.gstncaruso.tabpro.ui.dialogs.info;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.ScoreInfo;
import org.junit.jupiter.api.Test;

class ScoreInfoPanelTest {

    @Test
    void startsWithTheGivenInfo() {
        ScoreInfo info = ScoreInfo.empty().withTitle("Fade to Black").withArtist("Metallica");

        ScoreInfoPanel panel = new ScoreInfoPanel(info);

        assertEquals(info, panel.toScoreInfo());
    }

    @Test
    void reflectsWhateverYouLoadAfterwards() {
        ScoreInfoPanel panel = new ScoreInfoPanel(ScoreInfo.empty());

        panel.apply(ScoreInfo.empty().withTitle("Nueva").withCopyright("2026"));

        ScoreInfo result = panel.toScoreInfo();
        assertEquals("Nueva", result.title());
        assertEquals("2026", result.copyright());
    }

    @Test
    void allTenFieldsRoundTrip() {
        ScoreInfo info = new ScoreInfo(
                "titulo", "subtitulo", "artista", "album", "letrista",
                "musico", "copyright", "transcriptor", "instrucciones", "notas");

        ScoreInfoPanel panel = new ScoreInfoPanel(info);

        assertEquals(info, panel.toScoreInfo());
    }
}
