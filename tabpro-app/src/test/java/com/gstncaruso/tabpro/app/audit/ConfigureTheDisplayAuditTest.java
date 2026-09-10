package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.blankEditor;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponent;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findMenuItem;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.newFrame;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.pressKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.instruments.BeatViews;
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
import com.gstncaruso.tabpro.ui.score.ViewMode;
import com.gstncaruso.tabpro.ui.score.Zoom;
import com.gstncaruso.tabpro.ui.tracks.TrackPanel;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Manual, "Configure the Display" (linea 1886 del texto extraido): zoom, modo pagina, mesa de
 * mezcla y diapason/teclado, cada uno verificado contra el componente real que dice el manual
 * que tiene que cambiar (el zoom y el modo de ScoreCanvas, la visibilidad real de TrackPanel y
 * de BeatViews), nunca contra un getter de la Action.
 */
@Tag("integracion")
@ResourceLock(AuditSupport.SWING_LOCK)
class ConfigureTheDisplayAuditTest {

    @Test
    void acercarYAlejarPorLosAtajosCtrlMasYCtrlMenosCambianElZoomReal() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            Zoom inicial = canvas.zoom();

            pressKey(canvas, KeyStroke.getKeyStroke("ctrl EQUALS"));
            Zoom acercado = canvas.zoom();
            assertNotEquals(inicial, acercado, "Ctrl+ tiene que acercar el zoom real del lienzo");

            pressKey(canvas, KeyStroke.getKeyStroke("ctrl MINUS"));
            pressKey(canvas, KeyStroke.getKeyStroke("ctrl MINUS"));
            Zoom alejado = canvas.zoom();
            assertNotEquals(acercado, alejado, "Ctrl- tiene que alejar el zoom real del lienzo");

            pressKey(canvas, KeyStroke.getKeyStroke("ctrl 0"));
            assertEquals(Zoom.whole(), canvas.zoom(), "Ctrl+0 tiene que volver el zoom real al 100%");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void mesaDeMezclaPorElMenuEscondeYMuestraElTrackPanelReal() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            TrackPanel trackPanel = findComponent(frame.getContentPane(), TrackPanel.class);
            assertNotNull(trackPanel, "no encontre el TrackPanel real");
            assertEquals(true, trackPanel.isVisible(), "la mesa de mezcla arranca visible al abrir la ventana");

            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Mesa de mezcla");
            assertNotNull(item, "no encontre 'Mesa de mezcla' en el menu real");

            SwingUtilities.invokeAndWait(item::doClick);
            assertEquals(false, trackPanel.isVisible(),
                    "el menu real tiene que esconder el TrackPanel real");

            SwingUtilities.invokeAndWait(item::doClick);
            assertEquals(true, trackPanel.isVisible(),
                    "el menu real tiene que volver a mostrar el TrackPanel real");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void diapasonPorElAtajoCtrl3MuestraElBeatViewsReal() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            BeatViews beatViews = findComponent(frame.getContentPane(), BeatViews.class);
            assertNotNull(beatViews, "no encontre el BeatViews real");
            boolean antes = beatViews.isFretboardVisible();

            pressKey(canvas, KeyStroke.getKeyStroke("ctrl 3"));

            assertEquals(!antes, beatViews.isFretboardVisible(),
                    "Ctrl+3, despachado de verdad, tiene que alternar el diapason real");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void laCruzDeLaBarraDelDiapasonHaceLoMismoQueVerDiapason() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            BeatViews beatViews = findComponent(frame.getContentPane(), BeatViews.class);
            if (!beatViews.isFretboardVisible()) {
                pressKey(canvas, KeyStroke.getKeyStroke("ctrl 3"));
            }
            JButton close = AuditSupport.findButtonByAccessibleName(frame.getContentPane(), "Cerrar diapasón");
            assertNotNull(close, "no encontre la ✕ real del diapason");
            assertEquals(true, close.isShowing(), "el diapason tiene que estar abierto para poder cerrarlo con la ✕");
            assertEquals(true, AuditSupport.requestFocusAndAwait(close, 2000),
                    "no pude poner el foco en la ✕ real antes de clickearla");
            boolean antes = beatViews.isFretboardVisible();
            java.util.concurrent.CountDownLatch focusBackOnTheScore = focusGainedLatch(canvas);

            SwingUtilities.invokeAndWait(close::doClick);

            assertEquals(!antes, beatViews.isFretboardVisible(),
                    "la ✕ real tiene que hacer lo mismo que Ver > Diapasón");
            assertEquals(true,
                    focusBackOnTheScore.await(2, java.util.concurrent.TimeUnit.SECONDS),
                    "la ✕ real tiene que devolver el foco a la partitura, igual que el comando del menu");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void laCruzDeLaBarraDelTecladoHaceLoMismoQueVerTeclado() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            BeatViews beatViews = findComponent(frame.getContentPane(), BeatViews.class);
            if (!beatViews.isKeyboardVisible()) {
                pressKey(canvas, KeyStroke.getKeyStroke("ctrl 4"));
            }
            JButton close = AuditSupport.findButtonByAccessibleName(frame.getContentPane(), "Cerrar teclado");
            assertNotNull(close, "no encontre la ✕ real del teclado");
            assertEquals(true, close.isShowing(), "el teclado tiene que estar abierto para poder cerrarlo con la ✕");
            assertEquals(true, AuditSupport.requestFocusAndAwait(close, 2000),
                    "no pude poner el foco en la ✕ real antes de clickearla");
            boolean antes = beatViews.isKeyboardVisible();
            java.util.concurrent.CountDownLatch focusBackOnTheScore = focusGainedLatch(canvas);

            SwingUtilities.invokeAndWait(close::doClick);

            assertEquals(!antes, beatViews.isKeyboardVisible(),
                    "la ✕ real tiene que hacer lo mismo que Ver > Teclado");
            assertEquals(true,
                    focusBackOnTheScore.await(2, java.util.concurrent.TimeUnit.SECONDS),
                    "la ✕ real tiene que devolver el foco a la partitura, igual que el comando del menu");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void modoPaginaPorElMenuCambiaElViewModeRealDelCanvas() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            assertNotEquals(ViewMode.PAGE, canvas.viewMode(), "no arranca ya en modo pagina");

            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Modo página");
            assertNotNull(item, "no encontre 'Modo página' en el menu real");

            SwingUtilities.invokeAndWait(item::doClick);

            assertEquals(ViewMode.PAGE, canvas.viewMode(),
                    "el menu real tiene que dejar el canvas real en modo pagina");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    /** Se cuenta abajo apenas ese componente gana el foco de verdad, sin sondear con sleep. */
    private static java.util.concurrent.CountDownLatch focusGainedLatch(java.awt.Component component) {
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        component.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent event) {
                latch.countDown();
            }
        });
        return latch;
    }
}
