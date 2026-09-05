package com.gstncaruso.tabpro.ui.instruments;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import com.gstncaruso.tabpro.core.playback.Playhead;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * El diapason y el teclado, arriba de la partitura: muestran las notas del beat en el que
 * estas parado y, mientras suena, las del beat que esta sonando en esa misma pista.
 */
public final class BeatViews extends JPanel {

    private static final int TITLE_HEIGHT = 18;

    private final Editor editor;
    private final FretboardView fretboard = new FretboardView();
    private final KeyboardView keyboard = new KeyboardView();
    private final JPanel fretboardBox;
    private final JPanel keyboardBox;
    private Playhead playhead = Playhead.silent();

    public BeatViews(Editor editor) {
        this.editor = editor;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(ScoreColors.SURFACE);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ScoreColors.BORDER));

        fretboardBox = titled("Diapasón", fretboard, FretboardView.PREFERRED_HEIGHT);
        keyboardBox = titled("Teclado", keyboard, KeyboardView.PREFERRED_HEIGHT);
        add(fretboardBox);
        add(keyboardBox);

        editor.addListener(this::refresh);
        refresh();
    }

    public void showPlayhead(Playhead playhead) {
        this.playhead = playhead;
        refresh();
    }

    public boolean isFretboardVisible() {
        return fretboardBox.isVisible();
    }

    public boolean isKeyboardVisible() {
        return keyboardBox.isVisible();
    }

    public void setFretboardVisible(boolean visible) {
        fretboardBox.setVisible(visible);
        revalidate();
        repaint();
    }

    public void setKeyboardVisible(boolean visible) {
        keyboardBox.setVisible(visible);
        revalidate();
        repaint();
    }

    /** La afinacion de la pista donde esta el cursor, que es la que se dibuja. */
    public static Tuning tuningToShow(Editor editor) {
        return editor.currentTrack().tuning();
    }

    /**
     * El beat que suena en la pista del cursor si la reproduccion esta en marcha, y si no el
     * beat sobre el que esta parado el cursor.
     */
    public static Beat beatToShow(Editor editor, Playhead playhead) {
        return soundingBeat(editor, playhead).orElseGet(editor::currentBeat);
    }

    private static Optional<Beat> soundingBeat(Editor editor, Playhead playhead) {
        Cursor cursor = editor.cursor();
        return playhead.on(cursor.track()).flatMap(position -> beatAt(editor.currentTrack(), position));
    }

    private static Optional<Beat> beatAt(Track track, BeatPosition position) {
        if (position.measure() < 0 || position.measure() >= track.measureCount()) {
            return Optional.empty();
        }
        Measure measure = track.measure(position.measure());
        if (position.beat() < 0 || position.beat() >= measure.beats().size()) {
            return Optional.empty();
        }
        return Optional.of(measure.beat(position.beat()));
    }

    private void refresh() {
        Tuning tuning = tuningToShow(editor);
        Beat beat = beatToShow(editor, playhead);
        fretboard.show(tuning, beat);
        keyboard.show(tuning, beat);
    }

    private JPanel titled(String title, JComponent view, int viewHeight) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(ScoreColors.SURFACE);

        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 10f));
        label.setForeground(ScoreColors.MUTED_INK);
        label.setBorder(BorderFactory.createEmptyBorder(3, 12, 2, 0));
        label.setPreferredSize(new Dimension(0, TITLE_HEIGHT));

        box.add(label, BorderLayout.NORTH);
        box.add(view, BorderLayout.CENTER);
        int total = TITLE_HEIGHT + viewHeight;
        box.setPreferredSize(new Dimension(0, total));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, total));
        box.setMinimumSize(new Dimension(0, total));
        return box;
    }
}
