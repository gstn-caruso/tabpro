package com.gstncaruso.tabpro.ui.toolbar;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.EdtEditorListener;
import com.gstncaruso.tabpro.ui.actions.Command;
import com.gstncaruso.tabpro.ui.actions.Commands;
import com.gstncaruso.tabpro.ui.icons.Icons;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

public final class TrackSelector extends JPanel {

    private final Editor editor;
    private final JPanel numbers = new JPanel();
    private final List<JToggleButton> trackButtons = new ArrayList<>();
    private final JButton previousButton;
    private final JButton nextButton;

    public TrackSelector(Editor editor, Commands commands) {
        this.editor = editor;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setOpaque(false);
        numbers.setLayout(new BoxLayout(numbers, BoxLayout.X_AXIS));
        numbers.setOpaque(false);
        previousButton = arrowButton(commands.get("track.previous"));
        nextButton = arrowButton(commands.get("track.next"));
        add(previousButton);
        add(numbers);
        add(nextButton);
        rebuildTrackButtons();
        editor.addListener(EdtEditorListener.onEdt(this::refresh));
    }

    private void refresh() {
        if (trackButtons.size() != editor.score().trackCount()) {
            rebuildTrackButtons();
        } else {
            refreshSelection();
        }
    }

    private void rebuildTrackButtons() {
        numbers.removeAll();
        trackButtons.clear();
        ButtonGroup group = new ButtonGroup();
        for (int trackIndex = 0; trackIndex < editor.score().trackCount(); trackIndex++) {
            int selectedTrackIndex = trackIndex;
            JToggleButton button = new JToggleButton(Icons.letter(String.valueOf(trackIndex + 1)));
            button.addActionListener(event -> editor.selectTrack(selectedTrackIndex));
            group.add(button);
            trackButtons.add(button);
            numbers.add(button);
        }
        refreshSelection();
        numbers.revalidate();
        numbers.repaint();
    }

    private void refreshSelection() {
        int active = editor.cursor().track();
        for (int trackIndex = 0; trackIndex < trackButtons.size(); trackIndex++) {
            JToggleButton button = trackButtons.get(trackIndex);
            String label = "Pista " + (trackIndex + 1) + ": " + editor.score().track(trackIndex).name();
            button.getAccessibleContext().setAccessibleName(label);
            button.setToolTipText(label);
            button.setSelected(trackIndex == active);
        }
        previousButton.setEnabled(active > 0);
        nextButton.setEnabled(active < editor.score().trackCount() - 1);
    }

    private static JButton arrowButton(Command command) {
        JButton button = new JButton(command);
        button.setText(null);
        button.setToolTipText(command.description());
        button.getAccessibleContext().setAccessibleName(command.label());
        return button;
    }

    List<JToggleButton> trackButtons() {
        return List.copyOf(trackButtons);
    }

    JButton previousButton() {
        return previousButton;
    }

    JButton nextButton() {
        return nextButton;
    }
}
