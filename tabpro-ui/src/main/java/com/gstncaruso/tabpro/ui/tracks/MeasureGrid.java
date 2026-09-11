package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.a11y.AccessibleControl;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;
import java.util.OptionalInt;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

public class MeasureGrid extends JComponent implements AccessibleControl {

    public static final int CELL_WIDTH = 15;
    public static final int NUMBER_EVERY = 5;
    private static final int NUMBER_MARGIN = 4;
    public static final int NUMBERS_HEIGHT = TrackPanel.HEADER_HEIGHT - MarkerZone.HEIGHT;

    static final Color PLAYING_TINT = new Color(
            ScoreColors.PLAYING_MEASURE.getRed(),
            ScoreColors.PLAYING_MEASURE.getGreen(),
            ScoreColors.PLAYING_MEASURE.getBlue(),
            225);

    private final Editor editor;
    private OptionalInt playingMeasure = OptionalInt.empty();
    private Cell caret;
    private boolean showsFocusRing;
    private Rectangle cursorCellArea;

    public MeasureGrid(Editor editor) {
        this.editor = editor;
        this.caret = new Cell(editor.cursor().track(), editor.cursor().measure());
        this.cursorCellArea = cellBounds(editor.cursor().track(), editor.cursor().measure());
        setOpaque(true);
        setBackground(ScoreColors.SURFACE);
        setToolTipText(Texts.get("views.MeasureGrid.name"));
        getAccessibleContext().setAccessibleName(Texts.get("views.MeasureGrid.name"));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                hitTest(e.getX(), e.getY()).ifPresent(MeasureGrid.this::goTo);
            }
        });
        installKeyboardShortcuts();
        installFocusRing();
    }

    private void installFocusRing() {
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                showsFocusRing = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                showsFocusRing = false;
                repaint();
            }
        });
    }

    public Cell caret() {
        return caret;
    }

    private void installKeyboardShortcuts() {
        InputMap inputMap = getInputMap(WHEN_FOCUSED);
        ActionMap actionMap = getActionMap();
        bindCaretMove(inputMap, actionMap, "RIGHT", 0, 1);
        bindCaretMove(inputMap, actionMap, "LEFT", 0, -1);
        bindCaretMove(inputMap, actionMap, "DOWN", 1, 0);
        bindCaretMove(inputMap, actionMap, "UP", -1, 0);
        bindCaretActivation(inputMap, actionMap, "ENTER");
    }

    private void bindCaretActivation(InputMap inputMap, ActionMap actionMap, String keyStroke) {
        String name = "measuregrid.activate." + keyStroke;
        inputMap.put(KeyStroke.getKeyStroke(keyStroke), name);
        actionMap.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goTo(caret);
            }
        });
    }

    private void bindCaretMove(InputMap inputMap, ActionMap actionMap, String keyStroke, int trackDelta, int measureDelta) {
        String name = "measuregrid.caret." + keyStroke;
        inputMap.put(KeyStroke.getKeyStroke(keyStroke), name);
        actionMap.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveCaret(trackDelta, measureDelta);
            }
        });
    }

    private void moveCaret(int trackDelta, int measureDelta) {
        Score score = editor.score();
        int track = clampIndex(caret.track() + trackDelta, score.trackCount());
        int measure = clampIndex(caret.measure() + measureDelta, score.measureCount());
        caret = new Cell(track, measure);
        repaint();
    }

    private static int clampIndex(int candidate, int count) {
        return Math.max(0, Math.min(count - 1, candidate));
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJComponent() {
                @Override
                public AccessibleRole getAccessibleRole() {
                    return AccessibleRole.PANEL;
                }
            };
        }
        return accessibleContext;
    }

    public void showPlayingMeasure(OptionalInt measure) {
        this.playingMeasure = measure;
        repaint();
    }

    public void moveCursorHighlight() {
        Rectangle next = cellBounds(editor.cursor().track(), editor.cursor().measure());
        repaint(cursorCellArea.union(next));
        cursorCellArea = next;
    }

    public Rectangle cellBounds(int track, int measure) {
        return new Rectangle(
                measure * CELL_WIDTH,
                NUMBERS_HEIGHT + track * TrackPanel.ROW_HEIGHT,
                CELL_WIDTH,
                TrackPanel.ROW_HEIGHT);
    }

    public Optional<Cell> hitTest(int x, int y) {
        Score score = editor.score();
        int measure = x / CELL_WIDTH;
        int track = (y - NUMBERS_HEIGHT) / TrackPanel.ROW_HEIGHT;
        boolean inside = x >= 0
                && y >= NUMBERS_HEIGHT
                && measure < score.measureCount()
                && track >= 0
                && track < score.trackCount();
        return inside ? Optional.of(new Cell(track, measure)) : Optional.empty();
    }

    @Override
    public Dimension getPreferredSize() {
        Score score = editor.score();
        return new Dimension(
                Math.max(1, score.measureCount()) * CELL_WIDTH,
                NUMBERS_HEIGHT + score.trackCount() * TrackPanel.ROW_HEIGHT);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Score score = editor.score();

        g.setColor(ScoreColors.SURFACE);
        g.fillRect(0, 0, getWidth(), getHeight());

        playingMeasure.ifPresent(measure -> tintPlayingColumn(g, score, measure));
        paintMeasureNumbers(g, score);

        for (int track = 0; track < score.trackCount(); track++) {
            paintRow(g, score.track(track), track, track == editor.cursor().track());
        }
        outlineCursorCell(g, score);
        playingMeasure.ifPresent(measure -> outlinePlayingColumn(g, score, measure));
        if (showsFocusRing) {
            outlineCaretCell(g);
        }
    }

    private void outlineCaretCell(Graphics2D g) {
        Rectangle cell = cellBounds(caret.track(), caret.measure());
        g.setColor(focusRingColor());
        g.setStroke(new BasicStroke(2));
        g.drawRect(cell.x + 1, cell.y + 1, cell.width - 3, cell.height - 3);
    }

    private Color focusRingColor() {
        Color fromLookAndFeel = UIManager.getColor("Component.focusColor");
        return fromLookAndFeel != null ? fromLookAndFeel : ScoreColors.ACCENT;
    }

    private void tintPlayingColumn(Graphics2D g, Score score, int measure) {
        if (isOutside(score, measure)) {
            return;
        }
        g.setColor(PLAYING_TINT);
        g.fillRect(measure * CELL_WIDTH, 0, CELL_WIDTH, getHeight());
    }

    private void outlineCursorCell(Graphics2D g, Score score) {
        Cursor cursor = editor.cursor();
        if (isOutside(score, cursor.measure())) {
            return;
        }
        Rectangle cell = cellBounds(cursor.track(), cursor.measure());
        g.setColor(ScoreColors.INK);
        g.setStroke(new BasicStroke(1));
        g.drawRect(cell.x + 1, cell.y + 1, cell.width - 3, cell.height - 3);
    }

    /** Graphics2D paints in the order it is called, so drawing this after the cell fills keeps it on top. */
    private void outlinePlayingColumn(Graphics2D g, Score score, int measure) {
        if (isOutside(score, measure)) {
            return;
        }
        g.setColor(ScoreColors.PLAYING_MEASURE);
        g.setStroke(new BasicStroke(2));
        g.drawRect(measure * CELL_WIDTH + 1, 1, CELL_WIDTH - 2, getHeight() - 2);
    }

    private boolean isOutside(Score score, int measure) {
        return measure < 0 || measure >= score.measureCount();
    }

    private void paintMeasureNumbers(Graphics2D g, Score score) {
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
        g.setColor(ScoreColors.MUTED_INK);
        int step = numberStep(score.measureCount(), g.getFontMetrics());
        for (int measure = 0; measure < score.measureCount(); measure++) {
            if (measure == 0 || (measure + 1) % step == 0) {
                g.drawString(String.valueOf(measure + 1), measure * CELL_WIDTH + 2, NUMBERS_HEIGHT - 2);
            }
        }
    }

    static int numberStep(int measureCount, FontMetrics metrics) {
        String widestNumber = String.valueOf(measureCount);
        boolean everyNumberFitsTheCell = metrics.stringWidth(widestNumber) + NUMBER_MARGIN <= CELL_WIDTH;
        return everyNumberFitsTheCell ? 1 : NUMBER_EVERY;
    }

    private Color colorOf(int trackIndex) {
        Color color = TrackColors.of(trackIndex);
        return editor.score().isAudible(trackIndex) ? color : faded(color);
    }

    private static Color faded(Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), 70);
    }

    private void paintRow(Graphics2D g, Track track, int trackIndex, boolean selected) {
        for (int measure = 0; measure < editor.score().measureCount(); measure++) {
            Rectangle cell = cellBounds(trackIndex, measure);
            if (measure >= track.measureCount()) {
                continue;
            }
            g.setColor(selected ? ScoreColors.SURFACE_HIGHLIGHT : ScoreColors.SURFACE);
            g.fillRect(cell.x + 1, cell.y + 1, cell.width - 2, cell.height - 2);
            g.setColor(ScoreColors.BORDER);
            g.drawRect(cell.x + 1, cell.y + 1, cell.width - 3, cell.height - 3);
            g.setColor(track.hasNotesIn(measure) ? colorOf(trackIndex) : ScoreColors.EMPTY_MEASURE);
            g.fillRect(cell.x + 4, cell.y + 4, cell.width - 8, cell.height - 8);
        }
    }

    private void goTo(Cell cell) {
        if (cell.track() != editor.cursor().track()) {
            editor.selectTrack(cell.track());
        }
        if (cell.measure() < editor.currentTrack().measureCount()) {
            editor.moveTo(cell.measure(), 0, editor.cursor().string());
        }
    }

    public record Cell(int track, int measure) {}
}
