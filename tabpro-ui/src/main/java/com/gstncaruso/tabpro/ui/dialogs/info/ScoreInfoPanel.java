package com.gstncaruso.tabpro.ui.dialogs.info;

import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Los datos generales de la partitura: titulo, autores, derechos e instrucciones.
 * Se puede leer y llenar sin mostrarse, para poder probarlo sin abrir ventanas.
 */
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
        addRow("Titulo", title);
        addRow("Subtitulo", subtitle);
        addRow("Artista", artist);
        addRow("Album", album);
        addRow("Autor de la letra", lyricsAuthor);
        addRow("Autor de la musica", musicAuthor);
        addRow("Copyright", copyright);
        addRow("Transcriptor", transcriber);
        addSection("Instrucciones");
        addFullWidthRow(scrollable(instructions));
        addSection("Notas");
        addFullWidthRow(scrollable(notice));
        apply(initial);
    }

    private static JScrollPane scrollable(JTextArea area) {
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return new JScrollPane(area);
    }

    /** Llena los campos con los datos de una partitura ya cargada. */
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
