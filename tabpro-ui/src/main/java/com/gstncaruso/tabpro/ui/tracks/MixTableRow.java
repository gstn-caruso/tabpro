package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.DrumKits;
import com.gstncaruso.tabpro.core.model.Instruments;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;

/**
 * Una fila de la mesa de mezcla: numero, nombre, visibilidad en la vista multipista, puerto,
 * canal, instrumento, los seis parametros de sonido, silencio y solo.
 */
public final class MixTableRow extends JPanel {

    private final Editor editor;
    private final MixTableModel model;
    private final int trackIndex;

    private final JLabel number = new JLabel();
    private final JCheckBox visible = new JCheckBox();
    private final JComponent icon = instrumentIcon();
    private final JLabel name = new JLabel();
    private final JSpinner port = new JSpinner(new SpinnerNumberModel(1, 1, Channel.PORT_COUNT, 1));
    private final JSpinner channel = new JSpinner(new SpinnerNumberModel(1, 1, Channel.CHANNELS_PER_PORT, 1));
    private final JComboBox<String> instrument = new JComboBox<>(Instruments.names().toArray(new String[0]));
    private final List<ParameterCell> parameterCells = new ArrayList<>();
    private final JToggleButton mute = new JToggleButton("M");
    private final JToggleButton solo = new JToggleButton("S");
    private boolean syncing;
    private boolean instrumentComboShowsDrumKits;

    public MixTableRow(Editor editor, MixTableModel model, int trackIndex) {
        this.editor = editor;
        this.model = model;
        this.trackIndex = trackIndex;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setOpaque(true);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, TrackColors.of(trackIndex)),
                BorderFactory.createEmptyBorder(2, 6, 2, 8)));
        fixHeight();

        number.setFont(number.getFont().deriveFont(Font.PLAIN, 10f));
        number.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addColumn(number, MixTable.NUMBER_WIDTH);

        visible.setOpaque(false);
        visible.setFocusable(false);
        visible.setToolTipText("Visible en la vista multipista");
        visible.addActionListener(e -> model.setVisibleInMultitrackView(trackIndex, visible.isSelected()));
        addColumn(visible, MixTable.VISIBLE_WIDTH);

        addColumn(icon, MixTable.ICON_WIDTH);

        name.setFont(name.getFont().deriveFont(Font.BOLD));
        name.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addColumn(name, MixTable.NAME_WIDTH);

        spinner(port, () -> editor.setPort(trackIndex, (Integer) port.getValue()));
        addColumn(port, MixTable.PORT_WIDTH);

        spinner(channel, () -> editor.setChannelNumber(trackIndex, (Integer) channel.getValue()));
        addColumn(channel, MixTable.CHANNEL_WIDTH);

        instrument.setFocusable(false);
        instrument.addActionListener(e -> pushProgram());
        addColumn(instrument, MixTable.INSTRUMENT_WIDTH);

        for (MixParameter parameter : MixParameter.values()) {
            ParameterCell cell = new ParameterCell(editor, model, parameter, trackIndex);
            parameterCells.add(cell);
            addColumn(cell, MixTable.PARAMETER_WIDTH);
        }

        toggle(mute, () -> editor.toggleMute(trackIndex));
        toggle(solo, () -> editor.toggleSolo(trackIndex));

        addMouseListener(selectOnClick());
        number.addMouseListener(selectOnClick());
        name.addMouseListener(selectOnClick());
        refresh();
    }

    public void refresh() {
        syncing = true;
        Track track = editor.score().track(trackIndex);
        Channel ch = track.channel();
        number.setText(String.valueOf(trackIndex + 1));
        visible.setSelected(model.isVisibleInMultitrackView(trackIndex));
        name.setText(track.name());
        port.setValue(ch.port());
        channel.setValue(ch.number());
        refreshInstrumentCombo(track);
        mute.setSelected(ch.muted());
        solo.setSelected(ch.solo());
        parameterCells.forEach(cell -> {
            cell.refresh();
            cell.setVisible(!model.isReduced());
        });
        boolean selected = editor.cursor().track() == trackIndex;
        setBackground(selected ? ScoreColors.SURFACE_HIGHLIGHT : ScoreColors.SURFACE);
        boolean sounds = soundsRightNow(track);
        name.setForeground(sounds ? ScoreColors.INK : ScoreColors.MUTED_INK);
        number.setForeground(sounds ? ScoreColors.LABEL : ScoreColors.MUTED_INK);
        icon.repaint();
        syncing = false;
    }

    JLabel numberLabel() {
        return number;
    }

    JLabel nameLabel() {
        return name;
    }

    JCheckBox visibleCheckbox() {
        return visible;
    }

    JSpinner portField() {
        return port;
    }

    JSpinner channelField() {
        return channel;
    }

    JComboBox<String> instrumentField() {
        return instrument;
    }

    List<ParameterCell> parameterCells() {
        return List.copyOf(parameterCells);
    }

    private boolean soundsRightNow(Track track) {
        return editor.score().isAudible(trackIndex) || track.channel().solo();
    }

    private void fixHeight() {
        Dimension height = new Dimension(Integer.MAX_VALUE, TrackPanel.ROW_HEIGHT);
        setMaximumSize(height);
        setMinimumSize(new Dimension(0, TrackPanel.ROW_HEIGHT));
        setPreferredSize(new Dimension(MixTable.WIDTH, TrackPanel.ROW_HEIGHT));
    }

    private void spinner(JSpinner field, Runnable push) {
        field.setFocusable(false);
        field.addChangeListener(e -> {
            if (!syncing) {
                push.run();
            }
        });
    }

    private void toggle(JToggleButton button, Runnable push) {
        button.setFocusable(false);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 10f));
        button.setMargin(new java.awt.Insets(0, 0, 0, 0));
        button.addActionListener(e -> {
            if (!syncing) {
                push.run();
            }
        });
        addColumn(button, MixTable.TOGGLE_WIDTH);
    }

    private void pushProgram() {
        if (syncing || instrument.getSelectedIndex() < 0) {
            return;
        }
        int index = instrument.getSelectedIndex();
        boolean percussion = editor.score().track(trackIndex).isPercussion();
        editor.setProgram(trackIndex, percussion ? DrumKits.programAt(index) : index);
    }

    /**
     * En el canal 10 el program change no elige un instrumento: elige un Drum Kit (manual,
     * capitulo Percussion). El combo ofrece kits o instrumentos segun sea la pista.
     */
    private void refreshInstrumentCombo(Track track) {
        boolean percussion = track.isPercussion();
        if (percussion != instrumentComboShowsDrumKits) {
            List<String> options = percussion ? DrumKits.names() : Instruments.names();
            instrument.setModel(new DefaultComboBoxModel<>(options.toArray(new String[0])));
            instrumentComboShowsDrumKits = percussion;
        }
        int program = track.channel().program();
        instrument.setSelectedIndex(percussion ? DrumKits.indexOf(program) : program);
    }

    private void addColumn(JComponent component, int width) {
        Dimension size = new Dimension(width, TrackPanel.ROW_HEIGHT - 6);
        component.setPreferredSize(size);
        component.setMaximumSize(size);
        component.setMinimumSize(size);
        add(component);
        add(Box.createHorizontalStrut(MixTable.COLUMN_GAP));
    }

    /** El dibujito del instrumento de la pista, que se lee de un vistazo mejor que el combo. */
    private JComponent instrumentIcon() {
        return new JComponent() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                int program = editor.score().track(trackIndex).channel().program();
                boolean sounds = editor.score().isAudible(trackIndex);
                double size = Math.min(getWidth(), getHeight()) - 2;
                InstrumentIcon.paint(
                        (java.awt.Graphics2D) g,
                        program,
                        sounds ? ScoreColors.INK : ScoreColors.MUTED_INK,
                        (getWidth() - size) / 2.0,
                        (getHeight() - size) / 2.0,
                        size);
            }
        };
    }

    private MouseAdapter selectOnClick() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getSource() == name) {
                    TrackPanel.renameTrack(MixTableRow.this, editor, trackIndex);
                    return;
                }
                editor.selectTrack(trackIndex);
            }
        };
    }
}
