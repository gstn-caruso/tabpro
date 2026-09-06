package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.editing.Selection;
import com.gstncaruso.tabpro.core.playback.Playhead;
import com.gstncaruso.tabpro.ui.tab.FretDigits;
import com.gstncaruso.tabpro.ui.tab.KeyboardEditing;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;
import javax.swing.JComponent;
import javax.swing.Scrollable;

/**
 * El lienzo de la partitura: dibuja todas las pistas segun el {@link ViewMode} y el {@link Zoom}
 * elegidos, y traduce los clics a movimientos del cursor o, arrastrando, a una seleccion
 * multiple.
 */
public final class ScoreCanvas extends JComponent implements Scrollable {

    private static final int FALLBACK_WIDTH = 900;

    private final Editor editor;
    private Playhead playhead = Playhead.silent();
    private ViewMode viewMode = ViewMode.SCREEN_VERTICAL;
    private Zoom zoom = Zoom.whole();
    private Cursor selectionAnchor;
    private Selection selection;

    public ScoreCanvas(Editor editor) {
        this.editor = editor;
        setOpaque(true);
        setFocusable(true);
        setBackground(ScoreColors.BACKGROUND);
        editor.addListener(this::editorChanged);
        new KeyboardEditing(editor, new FretDigits(System::currentTimeMillis)).install(this);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                clearSelection();
                moveCursorTo(e.getX(), e.getY());
                selectionAnchor = editor.cursor();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                extendSelectionTo(e.getX(), e.getY());
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    // ---- Modo de vista y zoom: la API que usa el menu Ver de la ventana principal ----

    public ViewMode viewMode() {
        return viewMode;
    }

    public void setViewMode(ViewMode viewMode) {
        this.viewMode = viewMode;
        revalidate();
        repaint();
    }

    public Zoom zoom() {
        return zoom;
    }

    public void setZoom(Zoom zoom) {
        this.zoom = zoom;
        revalidate();
        repaint();
    }

    public void zoomIn() {
        setZoom(zoom.in());
    }

    public void zoomOut() {
        setZoom(zoom.out());
    }

    // ---- Seleccion multiple: la ventana principal puede leerla, fijarla o limpiarla ----

    public Optional<Selection> selection() {
        return Optional.ofNullable(selection);
    }

    public void setSelection(Selection selection) {
        this.selection = selection;
        repaint();
    }

    public void clearSelection() {
        selection = null;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        PageScorePainter.paint((Graphics2D) g, editor.score(), editor.cursor(), playhead, selection(), viewport());
    }

    public void showPlayhead(Playhead playhead) {
        this.playhead = playhead;
        repaint();
        playhead.on(editor.cursor().track())
                .ifPresent(position -> scrollRectToVisible(PageScorePainter.boundsOf(
                        editor.score(), viewport(), position.track(), position.measure(), position.beat())));
    }

    @Override
    public Dimension getPreferredSize() {
        return PageScorePainter.canvasSize(editor.score(), viewport());
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
        // Solo la pantalla vertical envuelve al ancho disponible; las demas tienen su propio
        // ancho (el de la hoja, o el de la partitura entera sin envolver) y se scrollean.
        return viewMode == ViewMode.SCREEN_VERTICAL;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    private void moveCursorTo(int x, int y) {
        PageScorePainter.hitTest(editor.score(), viewport(), x, y).ifPresent(hit -> {
            if (hit.track() != editor.cursor().track()) {
                editor.selectTrack(hit.track());
            }
            editor.moveTo(hit.measure(), hit.beat(), hit.string());
        });
    }

    private void extendSelectionTo(int x, int y) {
        if (selectionAnchor == null) {
            return;
        }
        PageScorePainter.hitTest(editor.score(), viewport(), x, y).ifPresent(hit -> {
            if (hit.track() != selectionAnchor.track()) {
                return;
            }
            setSelection(Selection.of(selectionAnchor, selectionAnchor.at(hit.measure(), hit.beat()), false));
        });
    }

    private void editorChanged() {
        revalidate();
        repaint();
        scrollRectToVisible(cursorBounds(editor.cursor()));
    }

    private Rectangle cursorBounds(Cursor cursor) {
        return PageScorePainter.boundsOf(
                editor.score(), viewport(), cursor.track(), cursor.measure(), cursor.beat());
    }

    private ScoreViewport viewport() {
        return ScoreViewport.of(viewMode, zoom, viewportWidth());
    }

    private int viewportWidth() {
        return getWidth() == 0 ? FALLBACK_WIDTH : getWidth();
    }
}
