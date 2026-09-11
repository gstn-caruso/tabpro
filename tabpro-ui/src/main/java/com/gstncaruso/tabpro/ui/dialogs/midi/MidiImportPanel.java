package com.gstncaruso.tabpro.ui.dialogs.midi;

import com.gstncaruso.tabpro.core.files.MidiTrackInfo;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import com.gstncaruso.tabpro.core.playback.PlaybackListener;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.core.playback.Timeline;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import com.gstncaruso.tabpro.ui.dialogs.style.Labels;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.icons.Icons;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public final class MidiImportPanel extends JPanel {

    private static final NoteValue[] QUANTIZE_CHOICES = {
        NoteValue.EIGHTH, NoteValue.SIXTEENTH, NoteValue.THIRTY_SECOND, NoteValue.SIXTY_FOURTH
    };

    private static final PlaybackListener SILENT_LISTENER = new PlaybackListener() {
        @Override
        public void beatStarted(BeatPosition position) {
        }

        @Override
        public void playbackFinished() {
        }
    };

    private final JList<MidiTrackInfo> trackList = new JList<>();
    private final JCheckBox transpose = new JCheckBox(Texts.get("score_dialogs.MidiImportPanel.transpose"));
    private final JCheckBox twoChannelsPerTrack =
            new JCheckBox(Texts.get("score_dialogs.MidiImportPanel.twoChannelsPerTrack"), true);
    private final QuantizeGroup chordPositionQuantizeGroup = new QuantizeGroup();
    private final QuantizeGroup noteDurationQuantizeGroup = new QuantizeGroup();
    private final JButton selectAll =
            iconButton(Icons.selectAllTracks(), Texts.get("score_dialogs.MidiImportPanel.selectAllTracks"));
    private final JButton listen =
            iconButton(Icons.play(), Texts.get("score_dialogs.MidiImportPanel.listenToSelectedTrack"));
    private final JButton stopListening =
            iconButton(Icons.stop(), Texts.get("score_dialogs.MidiImportPanel.stopPlayback"));
    private final Player player;
    private final Function<List<Integer>, Timeline> trackTimeline;

    public MidiImportPanel(List<MidiTrackInfo> tracks, Player player, Function<List<Integer>, Timeline> trackTimeline) {
        super(new BorderLayout(0, DialogStyle.GAP_S));
        this.player = player;
        this.trackTimeline = trackTimeline;
        DialogStyle.padded(this);
        trackList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        trackList.setCellRenderer(trackLabels());
        trackList.getAccessibleContext().setAccessibleName(Texts.get("score_dialogs.MidiImportPanel.midiFileTracks"));
        trackList.setToolTipText(Texts.get("score_dialogs.MidiImportPanel.midiFileTracks"));
        showTracks(tracks);
        selectAll.addActionListener(event -> selectAllTracks());
        listen.addActionListener(event -> listen());
        stopListening.addActionListener(event -> stopListening());

        JPanel checkboxes = new JPanel(new FlowLayout(FlowLayout.LEFT, DialogStyle.GAP_S, DialogStyle.GAP_S));
        checkboxes.add(transpose);
        checkboxes.add(twoChannelsPerTrack);

        FormPanel quantization = new FormPanel()
                .addSection(Texts.get("score_dialogs.MidiImportPanel.chordPositionQuantization"))
                .addFullWidthRow(chordPositionQuantizeGroup.asRow())
                .addSection(Texts.get("score_dialogs.MidiImportPanel.noteDurationQuantization"))
                .addFullWidthRow(noteDurationQuantizeGroup.asRow());

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(checkboxes, BorderLayout.NORTH);
        bottom.add(quantization, BorderLayout.SOUTH);

        JPanel trackListTools = new JPanel(new FlowLayout(FlowLayout.LEFT, DialogStyle.GAP_XS, 0));
        trackListTools.add(selectAll);
        trackListTools.add(listen);
        trackListTools.add(stopListening);

        add(trackListTools, BorderLayout.NORTH);
        add(new JScrollPane(trackList), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    public void selectAllTracks() {
        int lastIndex = trackList.getModel().getSize() - 1;
        if (lastIndex >= 0) {
            trackList.setSelectionInterval(0, lastIndex);
        }
    }

    public void listen() {
        List<Integer> selected = selectedTrackIndices();
        if (selected.isEmpty()) {
            return;
        }
        player.play(trackTimeline.apply(selected), SILENT_LISTENER);
    }

    public void stopListening() {
        player.stop();
    }

    private static JButton iconButton(Icon icon, String accessibleNameAndTooltip) {
        JButton button = new JButton(icon);
        button.setFocusPainted(false);
        button.getAccessibleContext().setAccessibleName(accessibleNameAndTooltip);
        button.setToolTipText(accessibleNameAndTooltip);
        return button;
    }

    public void showTracks(List<MidiTrackInfo> tracks) {
        DefaultListModel<MidiTrackInfo> model = new DefaultListModel<>();
        tracks.forEach(model::addElement);
        trackList.setModel(model);
    }

    public JList<MidiTrackInfo> trackList() {
        return trackList;
    }

    public List<Integer> selectedTrackIndices() {
        return trackList.getSelectedValuesList().stream().map(MidiTrackInfo::index).toList();
    }

    public boolean transposeDownOneOctave() {
        return transpose.isSelected();
    }

    public boolean useTwoChannelsPerTrack() {
        return twoChannelsPerTrack.isSelected();
    }

    public NoteValue chordPositionQuantize() {
        return chordPositionQuantizeGroup.chosen();
    }

    public void chooseChordPositionQuantize(NoteValue value) {
        chordPositionQuantizeGroup.choose(value);
    }

    public NoteValue noteDurationQuantize() {
        return noteDurationQuantizeGroup.chosen();
    }

    public void chooseNoteDurationQuantize(NoteValue value) {
        noteDurationQuantizeGroup.choose(value);
    }

    private static DefaultListCellRenderer trackLabels() {
        return new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean selected, boolean focused) {
                super.getListCellRendererComponent(list, value, index, selected, focused);
                if (value instanceof MidiTrackInfo summary) {
                    setText(summary.percussion()
                            ? Texts.get("score_dialogs.MidiImportPanel.percussionTrack", summary.name())
                            : summary.name());
                }
                return this;
            }
        };
    }

    private static final class QuantizeGroup {

        private final Map<NoteValue, JRadioButton> radios = new LinkedHashMap<>();

        QuantizeGroup() {
            ButtonGroup group = new ButtonGroup();
            for (NoteValue value : QUANTIZE_CHOICES) {
                JRadioButton radio = new JRadioButton(Labels.of(value));
                group.add(radio);
                radios.put(value, radio);
            }
            choose(NoteValue.THIRTY_SECOND);
        }

        JPanel asRow() {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, DialogStyle.GAP_S, 0));
            radios.values().forEach(row::add);
            return row;
        }

        NoteValue chosen() {
            return radios.entrySet().stream()
                    .filter(entry -> entry.getValue().isSelected())
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElseThrow();
        }

        void choose(NoteValue value) {
            radios.get(value).setSelected(true);
        }
    }
}
