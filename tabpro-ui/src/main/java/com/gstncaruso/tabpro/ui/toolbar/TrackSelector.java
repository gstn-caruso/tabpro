package com.gstncaruso.tabpro.ui.toolbar;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.actions.Commands;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 * Guitar Pro 5, manual pagina 14, fila 1: el selector de pista al final de la barra, un boton
 * numerado por pista y las flechas para ir a la anterior y la siguiente.
 */
public final class TrackSelector extends JPanel {

    private final Editor editor;
    private final List<JToggleButton> trackButtons = new ArrayList<>();
    private final JButton previousButton;
    private final JButton nextButton;

    public TrackSelector(Editor editor, Commands commands) {
        this.editor = editor;
        previousButton = new JButton(commands.get("track.previous"));
        nextButton = new JButton(commands.get("track.next"));
        for (int trackIndex = 0; trackIndex < editor.score().trackCount(); trackIndex++) {
            int selectedTrackIndex = trackIndex;
            JToggleButton button = new JToggleButton();
            button.setSelected(trackIndex == editor.cursor().track());
            button.addActionListener(event -> editor.selectTrack(selectedTrackIndex));
            trackButtons.add(button);
        }
        previousButton.setEnabled(editor.cursor().track() > 0);
        nextButton.setEnabled(editor.cursor().track() < editor.score().trackCount() - 1);
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
