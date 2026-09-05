package com.gstncaruso.tabpro.ui.tab;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Track;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;
import javax.swing.JComponent;
import javax.swing.Scrollable;

public final class TabCanvas extends JComponent implements Scrollable {

    private final Editor editor;

    public TabCanvas(Editor editor) {
        this.editor = editor;
        setOpaque(true);
        setFocusable(true);
        editor.addListener(this::editorChanged);
        new KeyboardEditing(editor, new FretDigits(System::currentTimeMillis)).install(this);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                layoutForCurrentWidth()
                        .hitTest(e.getX(), e.getY())
                        .ifPresent(hit -> editor.moveTo(hit.measure(), hit.beat(), hit.string()));
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Track track = currentTrack();
        TabLayout layout = TabLayout.of(track, getWidth());
        TabPainter.paint((Graphics2D) g, layout, track, editor.cursor(), Optional.empty());
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
        return TabLayout.STRING_SPACING;
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

    private void editorChanged() {
        revalidate();
        repaint();
        Cursor cursor = editor.cursor();
        scrollRectToVisible(layoutForCurrentWidth().beatBounds(cursor.measure(), cursor.beat()));
    }

    private TabLayout layoutForCurrentWidth() {
        int width = getWidth() == 0 ? 800 : getWidth();
        return TabLayout.of(currentTrack(), width);
    }

    private Track currentTrack() {
        return editor.score().track(editor.cursor().track());
    }
}
