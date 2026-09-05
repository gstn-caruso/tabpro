package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.playback.Playhead;
import com.gstncaruso.tabpro.ui.tab.FretDigits;
import com.gstncaruso.tabpro.ui.tab.KeyboardEditing;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.Scrollable;

/** El lienzo de la partitura: dibuja todas las pistas y traduce los clics a movimientos del cursor. */
public final class ScoreCanvas extends JComponent implements Scrollable {

    private static final int FALLBACK_WIDTH = 900;

    private final Editor editor;
    private Playhead playhead = Playhead.silent();

    public ScoreCanvas(Editor editor) {
        this.editor = editor;
        setOpaque(true);
        setFocusable(true);
        setBackground(ScoreColors.BACKGROUND);
        editor.addListener(this::editorChanged);
        new KeyboardEditing(editor, new FretDigits(System::currentTimeMillis)).install(this);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                moveCursorTo(e.getX(), e.getY());
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        ScorePainter.paint((Graphics2D) g, layoutForCurrentWidth(), editor.score(), editor.cursor(), playhead);
    }

    public void showPlayhead(Playhead playhead) {
        this.playhead = playhead;
        repaint();
        playhead.on(editor.cursor().track())
                .ifPresent(position -> scrollRectToVisible(
                        layoutForCurrentWidth().beatBounds(position.track(), position.measure(), position.beat())));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(0, layoutForCurrentWidth().totalHeight());
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return ScoreLayout.STRING_SPACING * 2;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return visibleRect.height;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    private void moveCursorTo(int x, int y) {
        layoutForCurrentWidth().hitTest(x, y).ifPresent(hit -> {
            if (hit.track() != editor.cursor().track()) {
                editor.selectTrack(hit.track());
            }
            editor.moveTo(hit.measure(), hit.beat(), hit.string());
        });
    }

    private void editorChanged() {
        revalidate();
        repaint();
        Cursor cursor = editor.cursor();
        scrollRectToVisible(cursorBounds(cursor));
    }

    private Rectangle cursorBounds(Cursor cursor) {
        ScoreLayout layout = layoutForCurrentWidth();
        Rectangle beat = layout.beatBounds(cursor.track(), cursor.measure(), cursor.beat());
        int top = layout.trackTop(cursor.track(), cursor.measure());
        return new Rectangle(beat.x, top, beat.width, layout.trackHeight(cursor.track()));
    }

    private ScoreLayout layoutForCurrentWidth() {
        return ScoreLayout.of(editor.score(), getWidth() == 0 ? FALLBACK_WIDTH : getWidth());
    }
}
