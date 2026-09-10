package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.editing.EditorChange;
import com.gstncaruso.tabpro.core.editing.EditorListener;
import com.gstncaruso.tabpro.core.editing.Selection;
import com.gstncaruso.tabpro.core.playback.Playhead;
import com.gstncaruso.tabpro.ui.EdtEditorListener;
import com.gstncaruso.tabpro.ui.a11y.AccessibleControl;
import com.gstncaruso.tabpro.ui.page.PageSetup;
import com.gstncaruso.tabpro.ui.tab.FretContextMenu;
import com.gstncaruso.tabpro.ui.tab.FretDigits;
import com.gstncaruso.tabpro.ui.tab.KeyboardEditing;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;
import java.util.function.Consumer;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;

public class ScoreCanvas extends JComponent implements Scrollable, AccessibleControl, ZoomHolder {

    private static final int FALLBACK_WIDTH = 900;

    private final Editor editor;
    private final TrackVisibility visibleTracks;
    private final FocusTraversal focusTraversal;
    private final java.util.List<Runnable> paginationListeners = new java.util.ArrayList<>();
    private final java.util.List<Runnable> zoomListeners = new java.util.ArrayList<>();
    private final java.util.List<Consumer<ScoreLayout.Hit>> clickListeners = new java.util.ArrayList<>();
    private VisibleNotations visibleNotations = VisibleNotations.both();
    private boolean graysTheInactiveVoice = true;
    private boolean showsDynamicNotes = false;
    private boolean autoScrollDuringPlayback = true;
    private Playhead playhead = Playhead.silent();
    private ViewMode viewMode = ViewMode.SCREEN_VERTICAL;
    private Zoom zoom = Zoom.whole();
    private PageSetup pageSetup = PageSetup.defaults();
    private Rectangle cursorArea = new Rectangle();

    public ScoreCanvas(Editor editor) {
        this(editor, new TrackVisibility());
    }

    public ScoreCanvas(Editor editor, TrackVisibility visibleTracks) {
        this(editor, visibleTracks, FocusTraversal.usingKeyboardFocusManager());
    }

    ScoreCanvas(Editor editor, TrackVisibility visibleTracks, FocusTraversal focusTraversal) {
        this.editor = editor;
        this.visibleTracks = visibleTracks;
        this.focusTraversal = focusTraversal;
        visibleTracks.onChange(() -> {
            revalidate();
            repaint();
        });
        setOpaque(true);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        setBackground(ScoreColors.BACKGROUND);
        setToolTipText("Partitura");
        getAccessibleContext().setAccessibleName("Partitura");
        this.cursorArea = currentCursorArea();
        editor.addListener(EdtEditorListener.onEdt(new EditorListener() {
            @Override
            public void editorChanged() {
                onEditorChanged(EditorChange.CONTENT);
            }

            @Override
            public void editorChanged(EditorChange change) {
                onEditorChanged(change);
            }
        }));
        new KeyboardEditing(editor, new FretDigits(System::currentTimeMillis)).install(this);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                    return;
                }
                requestFocusInWindow();
                if (e.isShiftDown()) {
                    editor.whileExtendingSelection(() -> moveCursorTo(e.getX(), e.getY()));
                    return;
                }
                editor.clearSelection();
                moveCursorTo(e.getX(), e.getY());
                if (e.isControlDown()) {
                    editor.startSelection(true);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // MouseEvent.isPopupTrigger() fires on mousePressed on some platforms, on
                // mouseReleased on Windows.
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                extendSelectionTo(e.getX(), e.getY());
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        installFocusExit();
    }

    private void installFocusExit() {
        InputMap inputMap = getInputMap(WHEN_FOCUSED);
        ActionMap actionMap = getActionMap();
        bindFocusExit(inputMap, actionMap, "ctrl F6",
                () -> focusTraversal.next(this));
        bindFocusExit(inputMap, actionMap, "ctrl shift F6",
                () -> focusTraversal.previous(this));
    }

    private void bindFocusExit(InputMap inputMap, ActionMap actionMap, String keyStroke, Runnable action) {
        String name = "scorecanvas.focusexit." + keyStroke;
        inputMap.put(KeyStroke.getKeyStroke(keyStroke), name);
        actionMap.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJComponent() {
                @Override
                public AccessibleRole getAccessibleRole() {
                    return AccessibleRole.CANVAS;
                }
            };
        }
        return accessibleContext;
    }

    public ViewMode viewMode() {
        return viewMode;
    }

    public void setViewMode(ViewMode viewMode) {
        this.viewMode = viewMode;
        repaginate();
    }

    @Override
    public Zoom zoom() {
        return zoom;
    }

    @Override
    public void setZoom(Zoom zoom) {
        this.zoom = zoom;
        revalidate();
        repaint();
        zoomListeners.forEach(Runnable::run);
    }

    public void zoomIn() {
        setZoom(zoom.in());
    }

    public void zoomOut() {
        setZoom(zoom.out());
    }

    @Override
    public void onZoomChange(Runnable listener) {
        zoomListeners.add(listener);
    }

    public PageSetup pageSetup() {
        return pageSetup;
    }

    public void setPageSetup(PageSetup pageSetup) {
        this.pageSetup = pageSetup;
        repaginate();
    }

    public Pagination pagination() {
        return PageScorePainter.paginationOf(editor.score(), viewport());
    }

    public void onPaginationChange(Runnable listener) {
        paginationListeners.add(listener);
    }

    private void repaginate() {
        revalidate();
        repaint();
        paginationListeners.forEach(Runnable::run);
    }

    public boolean isMultitrack() {
        return visibleTracks.isMultitrack();
    }

    public void setMultitrack(boolean multitrack) {
        visibleTracks.setMultitrack(multitrack);
    }

    public void setTrackShown(int track, boolean shown) {
        visibleTracks.setTurnedOn(track, shown);
    }

    public boolean showsStandardNotation() {
        return visibleNotations.standardNotation();
    }

    public boolean showsTablature() {
        return visibleNotations.tablature();
    }

    public void setStandardNotationShown(boolean shown) {
        showing(visibleNotations.withStandardNotation(shown));
    }

    public void setTablatureShown(boolean shown) {
        showing(visibleNotations.withTablature(shown));
    }

    public boolean graysTheInactiveVoice() {
        return graysTheInactiveVoice;
    }

    public void setGrayingTheInactiveVoice(boolean graying) {
        this.graysTheInactiveVoice = graying;
        repaint();
    }

    public boolean showsDynamicNotes() {
        return showsDynamicNotes;
    }

    public void setShowsDynamicNotes(boolean showsDynamicNotes) {
        this.showsDynamicNotes = showsDynamicNotes;
        repaint();
    }

    public boolean autoScrollDuringPlayback() {
        return autoScrollDuringPlayback;
    }

    public void setAutoScrollDuringPlayback(boolean autoScrollDuringPlayback) {
        this.autoScrollDuringPlayback = autoScrollDuringPlayback;
    }

    private void showing(VisibleNotations visibleNotations) {
        this.visibleNotations = visibleNotations;
        revalidate();
        repaint();
    }

    public Optional<Selection> selection() {
        return editor.selection();
    }

    @Override
    protected void paintComponent(Graphics g) {
        PageScorePainter.paint((Graphics2D) g, editor.score(), editor.cursor(), playhead, selection(), viewport());
    }

    public void showPlayhead(Playhead playhead) {
        this.playhead = playhead;
        repaint();
        if (!autoScrollDuringPlayback) {
            return;
        }
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
        return viewMode == ViewMode.SCREEN_VERTICAL;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    private Optional<ScoreLayout.Hit> moveCursorTo(int x, int y) {
        Optional<ScoreLayout.Hit> hit = PageScorePainter.hitTest(editor.score(), viewport(), x, y);
        hit.ifPresent(h -> {
            if (h.track() != editor.cursor().track()) {
                editor.selectTrack(h.track());
            }
            editor.moveTo(h.measure(), h.beat(), h.string());
            clickListeners.forEach(listener -> listener.accept(h));
        });
        return hit;
    }

    public void onClickReposition(Consumer<ScoreLayout.Hit> listener) {
        clickListeners.add(listener);
    }

    Optional<JPopupMenu> contextMenuAt(int x, int y) {
        return moveCursorTo(x, y).map(hit -> FretContextMenu.forTrack(editor.currentTrack(), editor::setFret));
    }

    private void showContextMenu(MouseEvent e) {
        contextMenuAt(e.getX(), e.getY()).ifPresent(menu -> menu.show(this, e.getX(), e.getY()));
    }

    private void extendSelectionTo(int x, int y) {
        PageScorePainter.hitTest(editor.score(), viewport(), x, y).ifPresent(hit -> {
            if (hit.track() != editor.cursor().track()) {
                return;
            }
            editor.whileExtendingSelection(() -> editor.moveTo(hit.measure(), hit.beat(), hit.string()));
        });
    }

    private void onEditorChanged(EditorChange change) {
        if (change == EditorChange.CONTENT) {
            revalidate();
            repaint();
            cursorArea = currentCursorArea();
        } else {
            Rectangle next = currentCursorArea();
            repaint(cursorArea.union(next));
            cursorArea = next;
        }
        Rectangle cursor = cursorBounds(editor.cursor());
        if (!cursor.isEmpty() && !getVisibleRect().isEmpty()) {
            scrollRectToVisible(cursor);
        }
    }

    private Rectangle currentCursorArea() {
        Rectangle area = cursorBounds(editor.cursor());
        Optional<Selection> selection = editor.selection();
        return selection.isPresent() ? area.union(selectionBounds(selection.get())) : area;
    }

    private Rectangle cursorBounds(Cursor cursor) {
        return PageScorePainter.boundsOf(
                editor.score(), viewport(), cursor.track(), cursor.measure(), cursor.beat());
    }

    private Rectangle selectionBounds(Selection selection) {
        Rectangle from = PageScorePainter.boundsOf(
                editor.score(), viewport(), selection.track(), selection.fromMeasure(), selection.fromBeat());
        Rectangle to = PageScorePainter.boundsOf(
                editor.score(), viewport(), selection.track(), selection.toMeasure(), selection.toBeat());
        return from.union(to);
    }

    private ScoreViewport viewport() {
        return new ScoreViewport(
                viewMode, zoom, viewportWidth(),
                visibleTracks.tracks().withActiveTrack(editor.cursor().track()),
                visibleNotations,
                graysTheInactiveVoice,
                pageSetup,
                showsDynamicNotes);
    }

    private int viewportWidth() {
        return getWidth() == 0 ? FALLBACK_WIDTH : getWidth();
    }
}
