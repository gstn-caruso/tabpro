package com.gstncaruso.tabpro.ui.instruments;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import com.gstncaruso.tabpro.core.playback.Playhead;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * El diapason y el teclado, arriba de la partitura: muestran las notas del beat en el que
 * estas parado y, mientras suena, las del beat que esta sonando en esa misma pista. Un clic
 * sobre un traste o una tecla escribe esa nota en el beat del cursor; uno sobre una que ya
 * esta la borra; el clic derecho la escribe y avanza al beat siguiente.
 */
public final class BeatViews extends JPanel {

    private static final int TITLE_HEIGHT = 24;

    private final Editor editor;
    private final FretboardView fretboard = new FretboardView();
    private final KeyboardView keyboard = new KeyboardView();
    private final JPanel fretboardBox;
    private final JPanel keyboardBox;
    private Playhead playhead = Playhead.silent();

    public BeatViews(Editor editor, Player player) {
        this.editor = editor;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(ScoreColors.SURFACE);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ScoreColors.BORDER));

        fretboardBox = titled("Diapasón", fretboard, FretboardView.PREFERRED_HEIGHT, fretboardToolbar());
        keyboardBox = titled("Teclado", keyboard, KeyboardView.PREFERRED_HEIGHT, keyboardToolbar());
        add(fretboardBox);
        add(keyboardBox);

        installWriting(new InstrumentEditing(editor, player));
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

    /** El diapason en si, para quien necesite leer o cambiar algo mas puntual. */
    public FretboardView fretboard() {
        return fretboard;
    }

    /** El teclado en si, para quien necesite leer o cambiar algo mas puntual. */
    public KeyboardView keyboard() {
        return keyboard;
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
        return locationToShow(editor, playhead).beat();
    }

    /** Donde esta, pista y todo, el beat que hay que mostrar: ver {@link #beatToShow}. */
    public static BeatLocation locationToShow(Editor editor, Playhead playhead) {
        Cursor cursor = editor.cursor();
        Track track = editor.currentTrack();
        return soundingPosition(editor, playhead)
                .map(position -> new BeatLocation(track, position.measure(), VoicePart.LEAD, position.beat()))
                .orElseGet(() -> new BeatLocation(track, cursor.measure(), cursor.voice(), cursor.beat()));
    }

    /**
     * Si lo que se ve es el beat del cursor. Mientras suena se ve el beat que suena, que no es el
     * que se editaria: ahi el clic no escribe, para no cambiar a ciegas un beat que no esta a la vista.
     */
    public static boolean showsTheCursorBeat(Editor editor, Playhead playhead) {
        return soundingPosition(editor, playhead).isEmpty();
    }

    private static Optional<BeatPosition> soundingPosition(Editor editor, Playhead playhead) {
        Cursor cursor = editor.cursor();
        return playhead.on(cursor.track()).filter(position -> isValid(editor.currentTrack(), position));
    }

    private static boolean isValid(Track track, BeatPosition position) {
        if (position.measure() < 0 || position.measure() >= track.measureCount()) {
            return false;
        }
        Measure measure = track.measure(position.measure());
        return position.beat() >= 0 && position.beat() < measure.beats().size();
    }

    private void installWriting(InstrumentEditing editing) {
        fretboard.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!showsTheCursorBeat(editor, playhead)) {
                    return;
                }
                fretboard.noteAt(e.getX(), e.getY()).ifPresent(note -> {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        editing.pressFretAndAdvance(note);
                    } else {
                        editing.toggleFret(note);
                    }
                });
            }
        });
        keyboard.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!showsTheCursorBeat(editor, playhead)) {
                    return;
                }
                keyboard.keyAt(e.getX(), e.getY()).ifPresent(key -> {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        editing.pressKeyAndAdvance(key);
                    } else {
                        editing.toggleKey(key);
                    }
                });
            }
        });
    }

    private void refresh() {
        BeatLocation location = locationToShow(editor, playhead);
        fretboard.show(location);
        keyboard.show(location);
    }

    // ---- controles ----------------------------------------------------------

    private JComponent fretboardToolbar() {
        JPanel bar = toolbar();
        bar.add(comboOf(FretboardDisplayMode.values(), fretboard::setDisplayMode));
        bar.add(comboOf(NoteNameMode.values(), fretboard::setNoteNameMode));
        bar.add(comboOf(FretboardType.values(), fretboard::setFretboardType));
        bar.add(scalePicker(fretboard::setScale));
        bar.add(handednessCheckBox());
        bar.add(navigationButtons());
        return bar;
    }

    private JComponent keyboardToolbar() {
        JPanel bar = toolbar();
        bar.add(comboOf(KeyboardDisplayMode.values(), keyboard::setDisplayMode));
        bar.add(scalePicker(keyboard::setScale));
        bar.add(navigationButtons());
        return bar;
    }

    private JPanel toolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        bar.setOpaque(false);
        return bar;
    }

    private <T> JComboBox<T> comboOf(T[] values, java.util.function.Consumer<T> onChoice) {
        JComboBox<T> combo = new JComboBox<>(values);
        combo.setFont(combo.getFont().deriveFont(10f));
        combo.addActionListener(e -> onChoice.accept(combo.getItemAt(combo.getSelectedIndex())));
        return combo;
    }

    private JComponent scalePicker(java.util.function.Consumer<Scale> onChoice) {
        String[] roots = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        JPanel picker = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        picker.setOpaque(false);

        JComboBox<String> rootCombo = new JComboBox<>(roots);
        rootCombo.setFont(rootCombo.getFont().deriveFont(10f));
        JComboBox<ScaleType> typeCombo = new JComboBox<>(ScaleType.values());
        typeCombo.setFont(typeCombo.getFont().deriveFont(10f));

        Runnable notify = () -> onChoice.accept(
                new Scale(rootCombo.getSelectedIndex(), typeCombo.getItemAt(typeCombo.getSelectedIndex())));
        rootCombo.addActionListener(e -> notify.run());
        typeCombo.addActionListener(e -> notify.run());

        picker.add(rootCombo);
        picker.add(typeCombo);
        return picker;
    }

    private JComponent handednessCheckBox() {
        JCheckBox zurdo = new JCheckBox("Zurdo");
        zurdo.setOpaque(false);
        zurdo.setFont(zurdo.getFont().deriveFont(10f));
        zurdo.setForeground(ScoreColors.LABEL);
        zurdo.addActionListener(
                e -> fretboard.setHandedness(zurdo.isSelected() ? Handedness.LEFT_HANDED : Handedness.RIGHT_HANDED));
        return zurdo;
    }

    private JComponent navigationButtons() {
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        nav.setOpaque(false);
        JButton previous = navButton("◀", editor::moveLeft);
        JButton next = navButton("▶", editor::moveRight);
        nav.add(previous);
        nav.add(next);
        return nav;
    }

    private JButton navButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(button.getFont().deriveFont(9f));
        button.setMargin(new java.awt.Insets(0, 4, 0, 4));
        button.setFocusable(false);
        button.addActionListener(e -> action.run());
        return button;
    }

    private JPanel titled(String title, JComponent view, int viewHeight, JComponent toolbar) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(ScoreColors.SURFACE);

        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 10f));
        label.setForeground(ScoreColors.MUTED_INK);
        label.setBorder(BorderFactory.createEmptyBorder(3, 12, 2, 0));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(label, BorderLayout.WEST);
        header.add(toolbar, BorderLayout.EAST);
        header.setPreferredSize(new Dimension(0, TITLE_HEIGHT));

        box.add(header, BorderLayout.NORTH);
        box.add(view, BorderLayout.CENTER);
        int total = TITLE_HEIGHT + viewHeight;
        box.setPreferredSize(new Dimension(0, total));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, total));
        box.setMinimumSize(new Dimension(0, total));
        return box;
    }
}
