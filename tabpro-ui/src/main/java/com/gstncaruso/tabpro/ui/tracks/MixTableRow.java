package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.DrumKits;
import com.gstncaruso.tabpro.core.model.Instruments;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import com.gstncaruso.tabpro.ui.theme.UiSizing;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
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

public final class MixTableRow extends JPanel {

    private final Editor editor;
    private final MixTableModel model;
    private final int trackIndex;

    private final JLabel number = new JLabel();
    private final JCheckBox visible = new JCheckBox();
    private final JLabel name = new JLabel();
    private final JSpinner port = new JSpinner(new SpinnerNumberModel(1, 1, Channel.PORT_COUNT, 1));
    private final JSpinner channel = new JSpinner(new SpinnerNumberModel(1, 1, Channel.CHANNELS_PER_PORT, 1));
    private final JSpinner effectChannel = new JSpinner(new SpinnerNumberModel(1, 1, Channel.CHANNELS_PER_PORT, 1));
    private final JComboBox<String> instrument = new JComboBox<>(Instruments.names().toArray(new String[0]));
    private final LevelSlider volumeSlider =
            new LevelSlider(0, Channel.MAX, 0, ScoreColors.VOLUME_LEVEL, ScoreColors.MUTED_INK);
    private final LevelSlider panSlider =
            new LevelSlider(0, Channel.MAX, Channel.CENTER_PAN, ScoreColors.PAN_LEVEL, ScoreColors.PAN_LEVEL);
    private final List<ParameterCell> parameterCells = new ArrayList<>();
    private final List<Runnable> uiSizing = new ArrayList<>();
    private final JToggleButton mute = new JToggleButton(Texts.get("views.MixTableRow.muteButton"));
    private final JToggleButton solo = new JToggleButton(Texts.get("views.MixTableRow.soloButton"));
    private boolean syncing;
    private boolean instrumentComboShowsDrumKits;

    public MixTableRow(Editor editor, MixTableModel model, int trackIndex) {
        this.editor = editor;
        this.model = model;
        this.trackIndex = trackIndex;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setOpaque(true);
        onUiUpdate(() -> {
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, UiSizing.scaled(4), 0, 0, TrackColors.of(trackIndex)),
                    BorderFactory.createEmptyBorder(UiSizing.scaled(2), UiSizing.scaled(6),
                            UiSizing.scaled(2), UiSizing.scaled(8))));
            fixHeight();
        });

        onUiUpdate(
                () -> number.setFont(number.getFont().deriveFont(Font.PLAIN, (float) UiSizing.scaled(10))));
        number.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addColumn(number, () -> UiSizing.scaled(MixTable.NUMBER_WIDTH));

        visible.setOpaque(false);
        visible.setFocusable(false);
        visible.setToolTipText(Texts.get("views.MixTableRow.visibleInMultitrackView"));
        visible.addActionListener(e -> model.setVisibleInMultitrackView(trackIndex, visible.isSelected()));
        addColumn(visible, () -> UiSizing.scaled(MixTable.VISIBLE_WIDTH));

        toggle(solo, () -> editor.toggleSolo(trackIndex));
        toggle(mute, () -> editor.toggleMute(trackIndex));

        name.setFont(name.getFont().deriveFont(Font.BOLD));
        name.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addColumn(name, () -> UiSizing.scaled(MixTable.NAME_WIDTH));

        spinner(port, () -> editor.setPort(trackIndex, (Integer) port.getValue()));
        addColumn(port, MixTable::portWidth);

        channel.setToolTipText(Texts.get("views.MixTableRow.midiChannelTooltip"));
        spinner(channel, () -> editor.setChannelNumber(trackIndex, (Integer) channel.getValue()));
        addColumn(channel, MixTable::channelWidth);

        effectChannel.setToolTipText(Texts.get("views.MixTableRow.effectsChannelTooltip"));
        spinner(effectChannel, () -> editor.setEffectChannel(trackIndex, (Integer) effectChannel.getValue()));
        addColumn(effectChannel, MixTable::channelWidth);

        instrument.setFocusable(false);
        instrument.addActionListener(e -> pushProgram());
        addColumn(instrument, MixTable::instrumentWidth);

        levelSlider(volumeSlider, MixParameter.VOLUME);
        addColumn(volumeSlider, () -> UiSizing.scaled(MixTable.LEVEL_WIDTH));
        levelSlider(panSlider, MixParameter.PAN);
        addColumn(panSlider, () -> UiSizing.scaled(MixTable.LEVEL_WIDTH));

        for (MixParameter parameter : List.of(
                MixParameter.CHORUS, MixParameter.REVERB, MixParameter.PHASER, MixParameter.TREMOLO)) {
            ParameterCell cell = new ParameterCell(editor, parameter, trackIndex);
            parameterCells.add(cell);
            onUiUpdate(() -> cell.numberField().setFont(
                    cell.numberField().getFont().deriveFont((float) UiSizing.scaled(10))));
            addColumn(cell, () -> UiSizing.scaled(MixTable.PARAMETER_WIDTH));
        }

        addMouseListener(selectOnClick());
        number.addMouseListener(selectOnClick());
        name.addMouseListener(selectOnClick());
        refresh();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (uiSizing != null) {
            uiSizing.forEach(Runnable::run);
        }
    }

    private void onUiUpdate(Runnable sizing) {
        uiSizing.add(sizing);
        sizing.run();
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
        effectChannel.setValue(ch.effectChannel());
        refreshInstrumentCombo(track);
        mute.setSelected(ch.muted());
        solo.setSelected(ch.solo());
        volumeSlider.setValue(MixParameter.VOLUME.valueOf(track));
        panSlider.setValue(MixParameter.PAN.valueOf(track));
        volumeSlider.setVisible(!model.isReduced());
        panSlider.setVisible(!model.isReduced());
        parameterCells.forEach(cell -> {
            cell.refresh();
            cell.setVisible(!model.isReduced());
        });
        boolean selected = editor.cursor().track() == trackIndex;
        setBackground(selected ? ScoreColors.SURFACE_HIGHLIGHT : ScoreColors.SURFACE);
        boolean sounds = soundsRightNow(track);
        name.setForeground(sounds ? ScoreColors.INK : ScoreColors.MUTED_INK);
        number.setForeground(sounds ? ScoreColors.LABEL : ScoreColors.MUTED_INK);
        refreshAccessibleNames(track.name());
        syncing = false;
    }

    private void refreshAccessibleNames(String trackName) {
        visible.getAccessibleContext().setAccessibleName(Texts.get("views.MixTableRow.showTrack", trackName));
        mute.getAccessibleContext().setAccessibleName(Texts.get("views.MixTableRow.muteTrack", trackName));
        solo.getAccessibleContext().setAccessibleName(Texts.get("views.MixTableRow.soloTrack", trackName));
        port.getAccessibleContext().setAccessibleName(Texts.get("views.MixTableRow.port", trackName));
        port.setToolTipText(Texts.get("views.MixTableRow.port", trackName));
        channel.getAccessibleContext().setAccessibleName(Texts.get("views.MixTableRow.channel", trackName));
        effectChannel.getAccessibleContext().setAccessibleName(
                Texts.get("views.MixTableRow.effectChannel", trackName));
        instrument.getAccessibleContext().setAccessibleName(Texts.get("views.MixTableRow.instrument", trackName));
        instrument.setToolTipText(Texts.get("views.MixTableRow.instrument", trackName));
        volumeSlider.getAccessibleContext().setAccessibleName(
                Texts.get("views.MixTableRow.parameterOfTrack", MixParameter.VOLUME.label(), trackName));
        panSlider.getAccessibleContext().setAccessibleName(
                Texts.get("views.MixTableRow.parameterOfTrack", MixParameter.PAN.label(), trackName));
    }

    private void levelSlider(LevelSlider slider, MixParameter parameter) {
        slider.onUserChange(() -> parameter.applyTo(editor, trackIndex, slider.getValue()));
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

    JSpinner effectChannelField() {
        return effectChannel;
    }

    JComboBox<String> instrumentField() {
        return instrument;
    }

    LevelSlider volumeSlider() {
        return volumeSlider;
    }

    LevelSlider panSlider() {
        return panSlider;
    }

    JToggleButton muteToggle() {
        return mute;
    }

    JToggleButton soloToggle() {
        return solo;
    }

    List<ParameterCell> parameterCells() {
        return List.copyOf(parameterCells);
    }

    private boolean soundsRightNow(Track track) {
        return editor.score().isAudible(trackIndex) || track.channel().solo();
    }

    private void fixHeight() {
        Dimension height = new Dimension(Integer.MAX_VALUE, TrackPanel.rowHeight());
        setMaximumSize(height);
        setMinimumSize(new Dimension(0, TrackPanel.rowHeight()));
        setPreferredSize(new Dimension(MixTable.width(), TrackPanel.rowHeight()));
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
        onUiUpdate(
                () -> button.setFont(button.getFont().deriveFont(Font.BOLD, (float) UiSizing.scaled(10))));
        button.setMargin(new java.awt.Insets(0, 0, 0, 0));
        button.addActionListener(e -> {
            if (!syncing) {
                push.run();
            }
        });
        addColumn(button, () -> UiSizing.scaled(MixTable.TOGGLE_WIDTH));
    }

    private void pushProgram() {
        if (syncing || instrument.getSelectedIndex() < 0) {
            return;
        }
        int index = instrument.getSelectedIndex();
        boolean percussion = editor.score().track(trackIndex).isPercussion();
        editor.setProgram(trackIndex, percussion ? DrumKits.programAt(index) : index);
    }

    /** On MIDI channel 10 a program change selects a drum kit, not an instrument. */
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

    private void addColumn(JComponent component, IntSupplier width) {
        onUiUpdate(() -> {
            Dimension size = new Dimension(width.getAsInt(), TrackPanel.rowHeight() - UiSizing.scaled(6));
            component.setPreferredSize(size);
            component.setMaximumSize(size);
            component.setMinimumSize(size);
        });
        add(component);
        Box.Filler gap = (Box.Filler) Box.createHorizontalStrut(0);
        onUiUpdate(() -> gap.changeShape(
                new Dimension(UiSizing.scaled(MixTable.COLUMN_GAP), 0),
                new Dimension(UiSizing.scaled(MixTable.COLUMN_GAP), 0),
                new Dimension(UiSizing.scaled(MixTable.COLUMN_GAP), Short.MAX_VALUE)));
        add(gap);
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
