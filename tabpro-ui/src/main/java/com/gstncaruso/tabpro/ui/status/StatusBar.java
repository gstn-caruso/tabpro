package com.gstncaruso.tabpro.ui.status;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * La barra de abajo de la ventana: pagina, posicion del cursor, si el compas actual esta
 * completo, la pista activa y, a la derecha, el titulo y el autor de la partitura.
 */
public final class StatusBar extends JPanel {

    private final Editor editor;
    private final JLabel page = new JLabel();
    private final JLabel position = new JLabel();
    private final JLabel completeness = new JLabel();
    private final JLabel trackName = new JLabel();
    private final JLabel credits = new JLabel();

    public StatusBar(Editor editor) {
        this.editor = editor;
        setLayout(new BorderLayout());
        setBackground(ScoreColors.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ScoreColors.BORDER),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
        left.add(styled(page));
        left.add(separator());
        left.add(styled(position));
        left.add(separator());
        left.add(styled(completeness));
        left.add(separator());
        left.add(styled(trackName));

        styled(credits).setHorizontalAlignment(SwingConstants.RIGHT);

        add(left, BorderLayout.WEST);
        add(credits, BorderLayout.EAST);

        refresh();
        editor.addListener(this::refresh);
    }

    String pageText() {
        return page.getText();
    }

    String positionText() {
        return position.getText();
    }

    String completenessText() {
        return completeness.getText();
    }

    String trackNameText() {
        return trackName.getText();
    }

    String creditsText() {
        return credits.getText();
    }

    private void refresh() {
        StatusInfo info = StatusInfo.of(editor);
        page.setText("Pág. " + info.pageNumber());
        position.setText("Compás " + info.measureNumber() + " · Pista " + info.trackNumber());
        completeness.setText(info.measureDurationText() + " (" + info.completeness().label() + ")");
        completeness.setForeground(
                info.completeness() == MeasureCompleteness.COMPLETE ? ScoreColors.LABEL : ScoreColors.WARNING);
        trackName.setText(info.trackName());
        credits.setText(creditsOf(info));
        credits.setToolTipText(BeatDescription.describe(editor.cursor(), editor.currentBeat()));
    }

    private static String creditsOf(StatusInfo info) {
        return info.author().isBlank() ? info.title() : info.title() + " — " + info.author();
    }

    private JLabel separator() {
        JLabel separator = new JLabel(" · ");
        return styled(separator);
    }

    private JLabel styled(JLabel label) {
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 11f));
        label.setForeground(ScoreColors.LABEL);
        return label;
    }
}
