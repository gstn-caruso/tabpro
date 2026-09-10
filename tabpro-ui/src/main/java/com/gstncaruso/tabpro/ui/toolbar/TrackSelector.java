package com.gstncaruso.tabpro.ui.toolbar;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.actions.Commands;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 * Guitar Pro 5, manual pagina 14, fila 1: el selector de pista al final de la barra, un boton
 * numerado por pista y las flechas para ir a la anterior y la siguiente.
 */
public final class TrackSelector extends JPanel {

    private final Editor editor;
    private final List<JToggleButton> trackButtons = new ArrayList<>();

    public TrackSelector(Editor editor, Commands commands) {
        this.editor = editor;
        for (int trackIndex = 0; trackIndex < editor.score().trackCount(); trackIndex++) {
            JToggleButton button = new JToggleButton();
            button.setSelected(trackIndex == editor.cursor().track());
            trackButtons.add(button);
        }
    }

    List<JToggleButton> trackButtons() {
        return List.copyOf(trackButtons);
    }
}
