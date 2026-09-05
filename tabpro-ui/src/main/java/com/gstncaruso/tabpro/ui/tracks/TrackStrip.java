package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Instruments;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;

/** La fila de una pista en el panel: nombre, instrumento, volumen, paneo, silenciar y solo. */
public final class TrackStrip extends JPanel {

    private final Editor editor;
    private final int trackIndex;
    private final JComponent icon = instrumentIcon();
    private final JLabel name = new JLabel();
    private final JComboBox<String> instrument = new JComboBox<>(Instruments.names().toArray(new String[0]));
    private final JSlider volume = new JSlider(0, 127);
    private final JSlider pan = new JSlider(0, 127);
    private final JToggleButton mute = new JToggleButton("M");
    private final JToggleButton solo = new JToggleButton("S");
    private boolean syncing;

    public TrackStrip(Editor editor, int trackIndex) {
        this.editor = editor;
        this.trackIndex = trackIndex;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        fixHeight();

        addColumn(icon, TrackPanel.ICON_WIDTH);
        name.setFont(name.getFont().deriveFont(Font.BOLD));
        name.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addColumn(name, TrackPanel.NAME_WIDTH);

        instrument.setFocusable(false);
        instrument.addActionListener(e -> pushProgram());
        addColumn(instrument, TrackPanel.INSTRUMENT_WIDTH);

        slider(volume, () -> editor.setVolume(trackIndex, volume.getValue()));
        addColumn(volume, TrackPanel.SLIDER_WIDTH);
        slider(pan, () -> editor.setPan(trackIndex, pan.getValue()));
        addColumn(pan, TrackPanel.SLIDER_WIDTH);

        toggle(mute, () -> editor.toggleMute(trackIndex));
        toggle(solo, () -> editor.toggleSolo(trackIndex));

        addMouseListener(selectOnClick());
        name.addMouseListener(selectOnClick());
        refresh();
    }

    public void refresh() {
        syncing = true;
        Track track = editor.score().track(trackIndex);
        Channel channel = track.channel();
        name.setText(track.name());
        instrument.setSelectedIndex(channel.program());
        volume.setValue(channel.volume());
        pan.setValue(channel.pan());
        mute.setSelected(channel.muted());
        solo.setSelected(channel.solo());
        boolean selected = editor.cursor().track() == trackIndex;
        setBackground(selected ? ScoreColors.SURFACE_HIGHLIGHT : ScoreColors.SURFACE);
        name.setForeground(soundsRightNow(track) ? ScoreColors.INK : ScoreColors.MUTED_INK);
        icon.repaint();
        syncing = false;
    }

    private boolean soundsRightNow(Track track) {
        return editor.score().isAudible(trackIndex) || track.channel().solo();
    }

    private void fixHeight() {
        Dimension height = new Dimension(Integer.MAX_VALUE, TrackPanel.ROW_HEIGHT);
        setMaximumSize(height);
        setMinimumSize(new Dimension(0, TrackPanel.ROW_HEIGHT));
        setPreferredSize(new Dimension(TrackPanel.MIXER_WIDTH, TrackPanel.ROW_HEIGHT));
    }

    private void slider(JSlider slider, Runnable push) {
        slider.setFocusable(false);
        slider.setOpaque(false);
        slider.addChangeListener(e -> {
            if (!syncing && !slider.getValueIsAdjusting()) {
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
        addColumn(button, TrackPanel.TOGGLE_WIDTH);
    }

    private void pushProgram() {
        if (!syncing && instrument.getSelectedIndex() >= 0) {
            editor.setProgram(trackIndex, instrument.getSelectedIndex());
        }
    }

    private void addColumn(JComponent component, int width) {
        Dimension size = new Dimension(width, TrackPanel.ROW_HEIGHT - 6);
        component.setPreferredSize(size);
        component.setMaximumSize(size);
        component.setMinimumSize(size);
        add(component);
        add(Box.createHorizontalStrut(TrackPanel.COLUMN_GAP));
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
                    TrackPanel.renameTrack(TrackStrip.this, editor, trackIndex);
                    return;
                }
                editor.selectTrack(trackIndex);
            }
        };
    }
}
