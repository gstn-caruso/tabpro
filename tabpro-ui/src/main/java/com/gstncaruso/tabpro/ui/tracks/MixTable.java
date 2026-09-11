package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Instruments;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import com.gstncaruso.tabpro.ui.score.TrackVisibility;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public final class MixTable extends JPanel {

    public static final int NUMBER_WIDTH = 24;
    public static final int VISIBLE_WIDTH = 20;
    public static final int NAME_WIDTH = 92;
    public static final int PORT_WIDTH = spinnerWidth(Channel.CHANNELS_PER_PORT);
    public static final int CHANNEL_WIDTH = spinnerWidth(Channel.CHANNELS_PER_PORT);
    public static final int INSTRUMENT_WIDTH = comboWidth(longestInstrumentName());
    public static final int LEVEL_WIDTH = 96;
    public static final int PARAMETER_WIDTH = 42;
    public static final int TOGGLE_WIDTH = 22;
    public static final int COLUMN_GAP = 4;
    public static final int REDUCE_BUTTON_WIDTH = 16;

    private static final List<Integer> COLUMN_WIDTHS = List.of(
            NUMBER_WIDTH, VISIBLE_WIDTH, TOGGLE_WIDTH, TOGGLE_WIDTH, NAME_WIDTH, PORT_WIDTH, CHANNEL_WIDTH,
            CHANNEL_WIDTH,
            INSTRUMENT_WIDTH,
            LEVEL_WIDTH, LEVEL_WIDTH,
            PARAMETER_WIDTH, PARAMETER_WIDTH, PARAMETER_WIDTH, PARAMETER_WIDTH);

    public static final int WIDTH =
            COLUMN_WIDTHS.stream().mapToInt(Integer::intValue).sum() + COLUMN_WIDTHS.size() * COLUMN_GAP + 16;

    private static final Map<MixParameter, String> ABBREVIATION_KEYS = Map.of(
            MixParameter.CHORUS, "views.MixTable.chorusAbbreviation",
            MixParameter.REVERB, "views.MixTable.reverbAbbreviation",
            MixParameter.PHASER, "views.MixTable.phaserAbbreviation",
            MixParameter.TREMOLO, "views.MixTable.tremoloAbbreviation");

    private final Editor editor;
    private final MixTableModel model;
    private final JPanel rowsPanel = new JPanel();
    private final List<MixTableRow> rows = new ArrayList<>();
    private final List<JLabel> columnTitleLabels = new ArrayList<>();
    private JButton reduceButton;
    private JButton restoreButton;

    public MixTable(Editor editor) {
        this(editor, new TrackVisibility());
    }

    public MixTable(Editor editor, TrackVisibility visibleTracks) {
        this.editor = editor;
        this.model = new MixTableModel(visibleTracks);
        setLayout(new BorderLayout());
        setBackground(ScoreColors.SURFACE);

        rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));
        rowsPanel.setBackground(ScoreColors.SURFACE);

        add(header(), BorderLayout.NORTH);
        add(rowsPanel, BorderLayout.CENTER);
        setPreferredSize(new Dimension(WIDTH, 0));

        rebuild();
    }

    public MixTableModel model() {
        return model;
    }

    public void refresh() {
        if (rows.size() != editor.score().trackCount()) {
            rebuild();
            return;
        }
        rows.forEach(MixTableRow::refresh);
    }

    List<MixTableRow> rows() {
        return List.copyOf(rows);
    }

    JButton reduceButton() {
        return reduceButton;
    }

    JButton restoreButton() {
        return restoreButton;
    }

    List<JLabel> columnTitleLabels() {
        return List.copyOf(columnTitleLabels);
    }

    private void rebuild() {
        rowsPanel.removeAll();
        rows.clear();
        for (int trackIndex = 0; trackIndex < editor.score().trackCount(); trackIndex++) {
            MixTableRow row = new MixTableRow(editor, model, trackIndex);
            rows.add(row);
            rowsPanel.add(row);
        }
        rowsPanel.add(Box.createVerticalGlue());
        rowsPanel.revalidate();
        rowsPanel.repaint();
    }

    private JComponent header() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setBackground(ScoreColors.SURFACE);
        header.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        header.setPreferredSize(new Dimension(WIDTH, TrackPanel.HEADER_HEIGHT));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, TrackPanel.HEADER_HEIGHT));

        header.add(reduceRestoreButtons());
        addTitle(header, "", VISIBLE_WIDTH - REDUCE_BUTTON_WIDTH * 2 + NUMBER_WIDTH);
        addTitle(header, Texts.get("views.MixTable.soloColumn"), TOGGLE_WIDTH);
        addTitle(header, Texts.get("views.MixTable.muteColumn"), TOGGLE_WIDTH);
        addTitle(header, Texts.get("views.MixTable.nameColumn"), NAME_WIDTH);
        addTitle(header, Texts.get("views.MixTable.portColumn"), PORT_WIDTH);
        addTitle(header, Texts.get("views.MixTable.channelColumn"), CHANNEL_WIDTH);
        addTitle(header, Texts.get("views.MixTable.channel2Column"), CHANNEL_WIDTH);
        addTitle(header, Texts.get("views.MixTable.instrumentColumn"), INSTRUMENT_WIDTH);
        addTitle(header, MixParameter.VOLUME.label(), LEVEL_WIDTH);
        addTitle(header, MixParameter.PAN.label(), LEVEL_WIDTH);
        for (MixParameter parameter : List.of(
                MixParameter.CHORUS, MixParameter.REVERB, MixParameter.PHASER, MixParameter.TREMOLO)) {
            addTitle(header, Texts.get(ABBREVIATION_KEYS.get(parameter)), PARAMETER_WIDTH);
        }
        return header;
    }

    private JComponent reduceRestoreButtons() {
        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        reduceButton = flatButton("−", Texts.get("views.MixTable.reduceAll"), model::reduceAllParameters);
        restoreButton = flatButton("+", Texts.get("views.MixTable.restoreAll"), model::restoreAllParameters);
        buttons.add(reduceButton);
        buttons.add(restoreButton);
        buttons.add(Box.createHorizontalStrut(COLUMN_GAP));
        return buttons;
    }

    private JButton flatButton(String glyph, String tooltip, Runnable action) {
        JButton button = new JButton(glyph);
        button.setToolTipText(tooltip);
        button.setFocusable(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setMargin(new Insets(0, 2, 0, 2));
        button.setForeground(ScoreColors.MUTED_INK);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 11f));
        Dimension size = new Dimension(REDUCE_BUTTON_WIDTH, TrackPanel.HEADER_HEIGHT);
        button.setPreferredSize(size);
        button.setMaximumSize(size);
        button.addActionListener(e -> {
            action.run();
            refresh();
        });
        return button;
    }

    private void addTitle(JPanel header, String text, int width) {
        JLabel title = title(text, width);
        columnTitleLabels.add(title);
        header.add(title);
        header.add(Box.createHorizontalStrut(COLUMN_GAP));
    }

    private JLabel title(String text, int width) {
        JLabel title = new JLabel(text);
        title.setFont(title.getFont().deriveFont(Font.PLAIN, 10f));
        title.setForeground(ScoreColors.MUTED_INK);
        Dimension size = new Dimension(width, TrackPanel.HEADER_HEIGHT);
        title.setPreferredSize(size);
        title.setMaximumSize(size);
        title.setMinimumSize(size);
        return title;
    }

    private static int spinnerWidth(int maxValue) {
        JSpinner probe = new JSpinner(new SpinnerNumberModel(maxValue, 1, maxValue, 1));
        return probe.getPreferredSize().width;
    }

    private static int comboWidth(String longestText) {
        JComboBox<String> probe = new JComboBox<>(new String[] {longestText});
        return probe.getPreferredSize().width;
    }

    private static String longestInstrumentName() {
        return Instruments.names().stream().max(Comparator.comparingInt(String::length)).orElseThrow();
    }
}
