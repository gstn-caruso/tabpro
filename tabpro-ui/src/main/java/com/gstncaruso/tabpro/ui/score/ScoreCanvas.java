package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.editing.Selection;
import com.gstncaruso.tabpro.core.playback.Playhead;
import com.gstncaruso.tabpro.ui.page.PageSetup;
import com.gstncaruso.tabpro.ui.tab.FretContextMenu;
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
import javax.swing.JPopupMenu;
import javax.swing.Scrollable;

/**
 * El lienzo de la partitura: dibuja las pistas que se ven segun el {@link ViewMode}, el
 * {@link Zoom} y las {@link VisibleTracks} elegidas, y traduce los clics a movimientos del
 * cursor o, arrastrando, a una seleccion multiple.
 */
public final class ScoreCanvas extends JComponent implements Scrollable {

    private static final int FALLBACK_WIDTH = 900;

    private final Editor editor;
    private final TrackVisibility visibleTracks;
    private final java.util.List<Runnable> paginationListeners = new java.util.ArrayList<>();
    private VisibleNotations visibleNotations = VisibleNotations.both();
    private boolean graysTheInactiveVoice = true;
    private Playhead playhead = Playhead.silent();
    private ViewMode viewMode = ViewMode.SCREEN_VERTICAL;
    private Zoom zoom = Zoom.whole();
    private PageSetup pageSetup = PageSetup.defaults();
    private Cursor selectionAnchor;
    private Selection selection;
    private boolean selectingWholeMeasures;

    public ScoreCanvas(Editor editor) {
        this(editor, new TrackVisibility());
    }

    public ScoreCanvas(Editor editor, TrackVisibility visibleTracks) {
        this.editor = editor;
        this.visibleTracks = visibleTracks;
        visibleTracks.onChange(() -> {
            revalidate();
            repaint();
        });
        setOpaque(true);
        setFocusable(true);
        setBackground(ScoreColors.BACKGROUND);
        editor.addListener(this::editorChanged);
        new KeyboardEditing(editor, new FretDigits(System::currentTimeMillis)).install(this);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                    return;
                }
                requestFocusInWindow();
                clearSelection();
                moveCursorTo(e.getX(), e.getY());
                selectionAnchor = editor.cursor();
                // El manual: "para seleccionar compases completos, apreta Ctrl mientras haces la
                // seleccion". Un clic sin arrastrar ya alcanza para seleccionar el compas entero.
                selectingWholeMeasures = e.isControlDown();
                if (selectingWholeMeasures) {
                    setSelection(Selection.of(selectionAnchor, selectionAnchor, true));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // El disparador del menu contextual es distinto por plataforma: en algunas
                // llega en mousePressed, en otras (Windows) recien en mouseReleased.
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
    }

    // ---- Modo de vista y zoom: la API que usa el menu Ver de la ventana principal ----

    public ViewMode viewMode() {
        return viewMode;
    }

    public void setViewMode(ViewMode viewMode) {
        this.viewMode = viewMode;
        repaginate();
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

    // ---- Configurar pagina: el papel sobre el que se reparte la partitura ----

    public PageSetup pageSetup() {
        return pageSetup;
    }

    /** El boton Actualizar partitura de Configurar pagina: la hoja cambia y hay que redibujar. */
    public void setPageSetup(PageSetup pageSetup) {
        this.pageSetup = pageSetup;
        repaginate();
    }

    /** En cuantas hojas quedo repartida la partitura y donde cae cada compas. */
    public Pagination pagination() {
        return PageScorePainter.paginationOf(editor.score(), viewport());
    }

    /** Avisar cuando cambia el reparto en hojas, que es lo que muestra la barra de estado. */
    public void onPaginationChange(Runnable listener) {
        paginationListeners.add(listener);
    }

    private void repaginate() {
        revalidate();
        repaint();
        paginationListeners.forEach(Runnable::run);
    }

    // ---- Vista multipista: el menu Ver la prende y apaga, la mesa de mezcla apaga pistas ----

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

    private void showing(VisibleNotations visibleNotations) {
        this.visibleNotations = visibleNotations;
        revalidate();
        repaint();
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

    private Optional<ScoreLayout.Hit> moveCursorTo(int x, int y) {
        Optional<ScoreLayout.Hit> hit = PageScorePainter.hitTest(editor.score(), viewport(), x, y);
        hit.ifPresent(h -> {
            if (h.track() != editor.cursor().track()) {
                editor.selectTrack(h.track());
            }
            editor.moveTo(h.measure(), h.beat(), h.string());
        });
        return hit;
    }

    /**
     * El manual: clic derecho sobre la tablatura ofrece "Note > 0 to 30" para escribir el
     * traste de la cuerda donde cayo el clic sin pasar por el teclado. Antes de armar el menu,
     * el clic mueve el cursor ahi mismo, igual que un clic izquierdo.
     */
    Optional<JPopupMenu> contextMenuAt(int x, int y) {
        return moveCursorTo(x, y).map(hit -> FretContextMenu.forTrack(editor.currentTrack(), editor::setFret));
    }

    private void showContextMenu(MouseEvent e) {
        contextMenuAt(e.getX(), e.getY()).ifPresent(menu -> menu.show(this, e.getX(), e.getY()));
    }

    private void extendSelectionTo(int x, int y) {
        if (selectionAnchor == null) {
            return;
        }
        PageScorePainter.hitTest(editor.score(), viewport(), x, y).ifPresent(hit -> {
            if (hit.track() != selectionAnchor.track()) {
                return;
            }
            setSelection(Selection.of(
                    selectionAnchor, selectionAnchor.at(hit.measure(), hit.beat()), selectingWholeMeasures));
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

    /** La pista activa la manda el cursor, asi que se lee recien al dibujar. */
    private ScoreViewport viewport() {
        return new ScoreViewport(
                viewMode, zoom, viewportWidth(),
                visibleTracks.tracks().withActiveTrack(editor.cursor().track()),
                visibleNotations,
                graysTheInactiveVoice,
                pageSetup);
    }

    private int viewportWidth() {
        return getWidth() == 0 ? FALLBACK_WIDTH : getWidth();
    }
}
