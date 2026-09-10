package com.gstncaruso.tabpro.ui.instruments;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import com.gstncaruso.tabpro.core.playback.Playhead;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.ui.EdtEditorListener;
import com.gstncaruso.tabpro.ui.dialogs.style.LabeledListCellRenderer;
import com.gstncaruso.tabpro.ui.icons.Icons;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

public final class BeatViews extends JPanel {

    private static final int TITLE_HEIGHT = 24;

    private final Editor editor;
    private final FretboardView fretboard = new FretboardView();
    private final KeyboardView keyboard = new KeyboardView();
    private final JPanel fretboardBox;
    private final JPanel keyboardBox;
    private Playhead playhead = Playhead.silent();
    private Runnable onCloseFretboard = () -> setFretboardVisible(false);
    private Runnable onCloseKeyboard = () -> setKeyboardVisible(false);

    public BeatViews(Editor editor, Player player) {
        this.editor = editor;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(ScoreColors.SURFACE);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ScoreColors.BORDER));

        fretboardBox = titled("Diapasón", fretboard, FretboardView.PREFERRED_HEIGHT, fretboardToolbar(),
                "Cerrar diapasón", () -> onCloseFretboard.run());
        keyboardBox = titled("Teclado", keyboard, KeyboardView.PREFERRED_HEIGHT, keyboardToolbar(),
                "Cerrar teclado", () -> onCloseKeyboard.run());
        add(fretboardBox);
        add(keyboardBox);

        installWriting(new InstrumentEditing(editor, player));
        editor.addListener(EdtEditorListener.onEdt(this::refresh));
        refresh();
    }

    public void showScale(int rootPitchClass, java.util.Collection<Integer> semitones) {
        Scale scale = Scale.of(rootPitchClass, semitones);
        fretboard.setScale(scale);
        keyboard.setScale(scale);
    }

    public void prepareForScalesTool() {
        setKeyboardVisible(true);
        fretboard.setDisplayMode(FretboardDisplayMode.BEAT_AND_SCALE);
        keyboard.setDisplayMode(KeyboardDisplayMode.BEAT_AND_SCALE);
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

    public void setOnCloseFretboard(Runnable action) {
        this.onCloseFretboard = java.util.Objects.requireNonNull(action);
    }

    public void setOnCloseKeyboard(Runnable action) {
        this.onCloseKeyboard = java.util.Objects.requireNonNull(action);
    }

    public void setKeyboardVisible(boolean visible) {
        keyboardBox.setVisible(visible);
        revalidate();
        repaint();
    }

    public FretboardView fretboard() {
        return fretboard;
    }

    public KeyboardView keyboard() {
        return keyboard;
    }

    public static Tuning tuningToShow(Editor editor) {
        return editor.currentTrack().tuning();
    }

    public static Beat beatToShow(Editor editor, Playhead playhead) {
        return locationToShow(editor, playhead).beat();
    }

    public static BeatLocation locationToShow(Editor editor, Playhead playhead) {
        Cursor cursor = editor.cursor();
        Track track = editor.currentTrack();
        return soundingPosition(editor, playhead)
                .map(position -> new BeatLocation(track, position.measure(), VoicePart.LEAD, position.beat()))
                .orElseGet(() -> new BeatLocation(track, cursor.measure(), cursor.voice(), cursor.beat()));
    }

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
                        writeFretNote(editing, note);
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
                        writeKeyNote(editing, key);
                    }
                });
            }
        });
        fretboard.onCaretActivated(note -> {
            if (showsTheCursorBeat(editor, playhead)) {
                writeFretNote(editing, note);
            }
        });
        keyboard.onCaretActivated(key -> {
            if (showsTheCursorBeat(editor, playhead)) {
                writeKeyNote(editing, key);
            }
        });
    }

    private void writeFretNote(InstrumentEditing editing, Note note) {
        editing.toggleFret(note);
    }

    private void writeKeyNote(InstrumentEditing editing, int midiNumber) {
        editing.toggleKey(midiNumber);
    }

    private void refresh() {
        BeatLocation location = locationToShow(editor, playhead);
        fretboard.show(location);
        keyboard.show(location);
    }

    private JComponent fretboardToolbar() {
        JPanel bar = toolbar();
        bar.add(comboOf("Modo de vista del diapasón", FretboardDisplayMode.values(), fretboard::setDisplayMode));
        bar.add(comboOf("Modo de nombres de nota", NoteNameMode.values(), fretboard::setNoteNameMode));
        bar.add(comboOf("Modo de etiqueta de escala", ScaleLabelMode.values(), fretboard::setScaleLabelMode));
        bar.add(comboOf("Tipo de diapasón", FretboardType.values(), fretboard::setFretboardType));
        bar.add(scalePicker(fretboard::setScale));
        bar.add(handednessToggle());
        bar.add(navigationButtons());
        return bar;
    }

    private JComponent keyboardToolbar() {
        JPanel bar = toolbar();
        bar.add(comboOf("Modo de vista del teclado", KeyboardDisplayMode.values(), keyboard::setDisplayMode));
        bar.add(scalePicker(keyboard::setScale));
        bar.add(navigationButtons());
        return bar;
    }

    private JPanel toolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        bar.setOpaque(false);
        return bar;
    }

    private <T> JComboBox<T> comboOf(String name, T[] values, java.util.function.Consumer<T> onChoice) {
        JComboBox<T> combo = new JComboBox<>(values);
        combo.setRenderer(new LabeledListCellRenderer());
        combo.setFont(combo.getFont().deriveFont(10f));
        combo.getAccessibleContext().setAccessibleName(name);
        combo.setToolTipText(name);
        combo.addActionListener(e -> onChoice.accept(combo.getItemAt(combo.getSelectedIndex())));
        return combo;
    }

    private JComponent scalePicker(java.util.function.Consumer<Scale> onChoice) {
        String[] roots = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        JPanel picker = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        picker.setOpaque(false);

        JComboBox<String> rootCombo = new JComboBox<>(roots);
        rootCombo.setFont(rootCombo.getFont().deriveFont(10f));
        rootCombo.getAccessibleContext().setAccessibleName("Nota raíz de la escala");
        rootCombo.setToolTipText("Nota raíz de la escala");
        JComboBox<ScaleType> typeCombo = new JComboBox<>(ScaleType.values());
        typeCombo.setRenderer(new LabeledListCellRenderer());
        typeCombo.setFont(typeCombo.getFont().deriveFont(10f));
        typeCombo.getAccessibleContext().setAccessibleName("Tipo de escala");
        typeCombo.setToolTipText("Tipo de escala");

        Runnable notify = () -> onChoice.accept(
                new Scale(rootCombo.getSelectedIndex(), typeCombo.getItemAt(typeCombo.getSelectedIndex())));
        rootCombo.addActionListener(e -> notify.run());
        typeCombo.addActionListener(e -> notify.run());

        picker.add(rootCombo);
        picker.add(typeCombo);
        return picker;
    }

    private JComponent handednessToggle() {
        JToggleButton zurdo = new JToggleButton(Icons.handedness());
        zurdo.setToolTipText("Zurdo: invierte el diapasón");
        zurdo.getAccessibleContext().setAccessibleName("Zurdo");
        zurdo.setFocusable(false);
        zurdo.setMargin(new java.awt.Insets(0, 4, 0, 4));
        zurdo.addActionListener(
                e -> fretboard.setHandedness(zurdo.isSelected() ? Handedness.LEFT_HANDED : Handedness.RIGHT_HANDED));
        return zurdo;
    }

    private JComponent navigationButtons() {
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        nav.setOpaque(false);
        JButton previous = navButton("◀", "Beat anterior", editor::moveLeft);
        JButton next = navButton("▶", "Beat siguiente", editor::moveRight);
        nav.add(previous);
        nav.add(next);
        return nav;
    }

    private JButton navButton(String text, String name, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(button.getFont().deriveFont(9f));
        button.setMargin(new java.awt.Insets(0, 4, 0, 4));
        button.setFocusable(false);
        button.getAccessibleContext().setAccessibleName(name);
        button.setToolTipText(name);
        button.addActionListener(e -> action.run());
        return button;
    }

    private JPanel titled(
            String title,
            JComponent view,
            int viewHeight,
            JComponent toolbar,
            String closeAccessibleName,
            Runnable onClose) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(ScoreColors.SURFACE);

        PanelTitleBar header = new PanelTitleBar(title, toolbar, closeAccessibleName);
        header.onClose(onClose);
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
