package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import com.gstncaruso.tabpro.ui.score.TrackVisibility;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * La mesa de mezcla: una fila por pista con numero, nombre, visibilidad en la vista multipista,
 * solo, silencio, puerto, canal, instrumento y los seis parametros de sonido. El titulo de cada
 * parametro se puede clickear para alternar entre potenciometro y numero, y los botones de arriba
 * reducen o restauran todos los parametros a la vez.
 */
public final class MixTable extends JPanel {

    public static final int NUMBER_WIDTH = 24;
    public static final int VISIBLE_WIDTH = 20;
    public static final int ICON_WIDTH = 20;
    public static final int NAME_WIDTH = 92;
    public static final int PORT_WIDTH = 32;
    public static final int CHANNEL_WIDTH = 32;
    public static final int INSTRUMENT_WIDTH = 132;
    public static final int PARAMETER_WIDTH = 42;
    public static final int TOGGLE_WIDTH = 22;
    public static final int COLUMN_GAP = 4;
    public static final int REDUCE_BUTTON_WIDTH = 16;

    private static final List<Integer> COLUMN_WIDTHS = List.of(
            NUMBER_WIDTH, VISIBLE_WIDTH, ICON_WIDTH, NAME_WIDTH, PORT_WIDTH, CHANNEL_WIDTH, INSTRUMENT_WIDTH,
            PARAMETER_WIDTH, PARAMETER_WIDTH, PARAMETER_WIDTH, PARAMETER_WIDTH, PARAMETER_WIDTH, PARAMETER_WIDTH,
            TOGGLE_WIDTH, TOGGLE_WIDTH);

    public static final int WIDTH =
            COLUMN_WIDTHS.stream().mapToInt(Integer::intValue).sum() + COLUMN_WIDTHS.size() * COLUMN_GAP + 16;

    private final Editor editor;
    private final MixTableModel model;
    private final JPanel rowsPanel = new JPanel();
    private final List<MixTableRow> rows = new ArrayList<>();
    private final Map<MixParameter, JLabel> parameterHeaders = new EnumMap<>(MixParameter.class);
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

    JLabel headerFor(MixParameter parameter) {
        return parameterHeaders.get(parameter);
    }

    JButton reduceButton() {
        return reduceButton;
    }

    JButton restoreButton() {
        return restoreButton;
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
        addTitle(header, "", NAME_WIDTH + ICON_WIDTH + VISIBLE_WIDTH - 2 * REDUCE_BUTTON_WIDTH - COLUMN_GAP);
        addTitle(header, "Prt", PORT_WIDTH);
        addTitle(header, "Cnl", CHANNEL_WIDTH);
        addTitle(header, "Instrumento", INSTRUMENT_WIDTH);
        for (MixParameter parameter : MixParameter.values()) {
            addClickableTitle(header, parameter);
        }
        addTitle(header, "M", TOGGLE_WIDTH);
        addTitle(header, "S", TOGGLE_WIDTH);
        return header;
    }

    private JComponent reduceRestoreButtons() {
        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        reduceButton = flatButton("−", "Reducir todos los parametros", model::reduceAllParameters);
        restoreButton = flatButton("+", "Restaurar todos los parametros", model::restoreAllParameters);
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

    private void addClickableTitle(JPanel header, MixParameter parameter) {
        JLabel title = title(parameter.label(), PARAMETER_WIDTH);
        title.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        title.setToolTipText("Click para alternar entre potenciometro y numero");
        title.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                model.toggleDisplayMode(parameter);
                refresh();
            }
        });
        parameterHeaders.put(parameter, title);
        header.add(title);
        header.add(Box.createHorizontalStrut(COLUMN_GAP));
    }

    private void addTitle(JPanel header, String text, int width) {
        header.add(title(text, width));
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
}
