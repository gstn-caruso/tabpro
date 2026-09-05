package com.gstncaruso.tabpro.ui.dialogs.info;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import java.awt.Component;
import java.util.List;
import javax.swing.JTabbedPane;

/** La ventana de Informacion de la partitura [F5], con su solapa de letra. */
public final class ScoreInfoDialog {

    private ScoreInfoDialog() {
    }

    public static void show(Component parent, Editor editor) {
        Score score = editor.score();
        ScoreInfoPanel infoPanel = new ScoreInfoPanel(score.info());
        LyricsPanel lyricsPanel = new LyricsPanel(trackNamesOf(score), score.lyrics());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("General", infoPanel);
        tabs.addTab("Letra", lyricsPanel);

        boolean accepted = DialogShell.ask(parent, "Informacion de la partitura", tabs);
        if (accepted) {
            editor.setInfo(infoPanel.toScoreInfo());
            editor.setLyrics(lyricsPanel.toLyrics());
        }
    }

    private static List<String> trackNamesOf(Score score) {
        return score.tracks().stream().map(track -> track.name()).toList();
    }
}
