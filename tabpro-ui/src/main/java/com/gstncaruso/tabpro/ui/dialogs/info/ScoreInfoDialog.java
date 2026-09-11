package com.gstncaruso.tabpro.ui.dialogs.info;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.Component;
import java.util.List;
import javax.swing.JTabbedPane;

public final class ScoreInfoDialog {

    private ScoreInfoDialog() {
    }

    public static void show(Component parent, Editor editor) {
        show(parent, editor, DefaultScoreProperties.userProperties());
    }

    public static void show(Component parent, Editor editor, DefaultScoreProperties defaultProperties) {
        Score score = editor.score();
        ScoreInfoPanel infoPanel = new ScoreInfoPanel(score.info());
        LyricsPanel lyricsPanel = new LyricsPanel(trackNamesOf(score), score.lyrics());
        DefaultScorePropertiesPanel defaultsPanel = new DefaultScorePropertiesPanel(defaultProperties.get());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab(Texts.get("score_dialogs.ScoreInfoDialog.generalTab"), infoPanel);
        tabs.addTab(Texts.get("score_dialogs.ScoreInfoDialog.lyricsTab"), lyricsPanel);
        tabs.addTab(Texts.get("score_dialogs.ScoreInfoDialog.defaultsTab"), defaultsPanel);

        boolean accepted = DialogShell.ask(parent, Texts.get("score_dialogs.ScoreInfoDialog.title"), tabs);
        if (accepted) {
            editor.setInfo(infoPanel.toScoreInfo());
            editor.setLyrics(lyricsPanel.toLyrics());
            defaultProperties.save(defaultsPanel.toDefaults());
        }
    }

    private static List<String> trackNamesOf(Score score) {
        return score.tracks().stream().map(track -> track.name()).toList();
    }
}
