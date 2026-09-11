package com.gstncaruso.tabpro.ui.dialogs.info;

import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public final class ScoreInfoPanel extends FormPanel {

    private final JTextField title = new JTextField(DialogStyle.TEXT_FIELD_COLUMNS);
    private final JTextField subtitle = new JTextField(DialogStyle.TEXT_FIELD_COLUMNS);
    private final JTextField artist = new JTextField(DialogStyle.TEXT_FIELD_COLUMNS);
    private final JTextField album = new JTextField(DialogStyle.TEXT_FIELD_COLUMNS);
    private final JTextField lyricsAuthor = new JTextField(DialogStyle.TEXT_FIELD_COLUMNS);
    private final JTextField musicAuthor = new JTextField(DialogStyle.TEXT_FIELD_COLUMNS);
    private final JTextField copyright = new JTextField(DialogStyle.TEXT_FIELD_COLUMNS);
    private final JTextField transcriber = new JTextField(DialogStyle.TEXT_FIELD_COLUMNS);
    private final JTextArea instructions = new JTextArea(3, DialogStyle.TEXT_FIELD_COLUMNS);
    private final JTextArea notice = new JTextArea(3, DialogStyle.TEXT_FIELD_COLUMNS);

    public ScoreInfoPanel(ScoreInfo initial) {
        addRow(Texts.get("score_dialogs.shared.title"), title);
        addRow(Texts.get("score_dialogs.ScoreInfoPanel.subtitle"), subtitle);
        addRow(Texts.get("score_dialogs.shared.artist"), artist);
        addRow(Texts.get("score_dialogs.ScoreInfoPanel.albumName"), album);
        addRow(Texts.get("score_dialogs.ScoreInfoPanel.lyricsAuthor"), lyricsAuthor);
        addRow(Texts.get("score_dialogs.ScoreInfoPanel.musicAuthor"), musicAuthor);
        addRow(Texts.get("score_dialogs.ScoreInfoPanel.copyright"), copyright);
        addRow(Texts.get("score_dialogs.ScoreInfoPanel.transcriber"), transcriber);
        addSection(Texts.get("score_dialogs.ScoreInfoPanel.instructions"));
        addFullWidthRow(scrollable(instructions));
        addSection(Texts.get("score_dialogs.ScoreInfoPanel.notice"));
        addFullWidthRow(scrollable(notice));
        apply(initial);
    }

    private static JScrollPane scrollable(JTextArea area) {
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return new JScrollPane(area);
    }

    public void apply(ScoreInfo info) {
        title.setText(info.title());
        subtitle.setText(info.subtitle());
        artist.setText(info.artist());
        album.setText(info.album());
        lyricsAuthor.setText(info.lyricsAuthor());
        musicAuthor.setText(info.musicAuthor());
        copyright.setText(info.copyright());
        transcriber.setText(info.transcriber());
        instructions.setText(info.instructions());
        notice.setText(info.notice());
    }

    public ScoreInfo toScoreInfo() {
        return new ScoreInfo(
                title.getText(),
                subtitle.getText(),
                artist.getText(),
                album.getText(),
                lyricsAuthor.getText(),
                musicAuthor.getText(),
                copyright.getText(),
                transcriber.getText(),
                instructions.getText(),
                notice.getText());
    }
}
