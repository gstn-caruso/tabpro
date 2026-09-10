package com.gstncaruso.tabpro.ui.status;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.EdtEditorListener;
import com.gstncaruso.tabpro.ui.score.Pagination;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

public final class StatusBar extends JPanel {

    private final Editor editor;
    private final Supplier<Pagination> pagination;
    private final JLabel page = new JLabel();
    private final JLabel position = new JLabel();
    private final JLabel completeness = new JLabel();
    private final JLabel trackName = new JLabel();
    private final JLabel duration = new JLabel();
    private final JLabel credits = new JLabel();

    public StatusBar(Editor editor) {
        this(editor, Pagination::single);
    }

    public StatusBar(Editor editor, Supplier<Pagination> pagination) {
        this.editor = editor;
        this.pagination = pagination;
        setLayout(new BorderLayout());
        setBackground(ScoreColors.SURFACE);
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ScoreColors.BORDER));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
        left.add(sunkenPanel(page, "Página"));
        left.add(sunkenPanel(position, "Posición"));
        left.add(sunkenPanel(completeness, "Estado del compás"));
        left.add(sunkenPanel(trackName, "Pista"));
        left.add(sunkenPanel(duration, "Duración del compás"));

        styled(credits).setHorizontalAlignment(SwingConstants.CENTER);

        add(left, BorderLayout.WEST);
        add(sunkenPanel(credits, "Título y autor"), BorderLayout.CENTER);

        refresh();
        editor.addListener(EdtEditorListener.onEdt(this::refresh));
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

    String durationText() {
        return duration.getText();
    }

    String creditsText() {
        return credits.getText();
    }

    public void refresh() {
        StatusInfo info = StatusInfo.of(editor, pagination.get());
        page.setText("Pág. " + info.pageNumber() + "/" + info.pageCount());
        position.setText(String.format("%03d : %03d", info.measureNumber(), info.measureCount()));
        completeness.setText("Compás " + info.completeness().label());
        completeness.setForeground(
                info.completeness() == MeasureCompleteness.COMPLETE ? ScoreColors.LABEL : ScoreColors.WARNING);
        trackName.setText(info.trackName());
        duration.setText(info.measureBeatsRatioText());
        credits.setText(creditsOf(info));
        credits.setToolTipText(BeatDescription.describe(editor.cursor(), editor.currentBeat()));
    }

    private static String creditsOf(StatusInfo info) {
        return info.author().isBlank() ? info.title() : info.title() + " — " + info.author();
    }

    private JPanel sunkenPanel(JLabel label, String accessibleName) {
        styled(label);
        label.getAccessibleContext().setAccessibleName(accessibleName);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createBevelBorder(BevelBorder.LOWERED, ScoreColors.BEVEL_SHADE, ScoreColors.BORDER),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JLabel styled(JLabel label) {
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 11f));
        label.setForeground(ScoreColors.LABEL);
        return label;
    }
}
