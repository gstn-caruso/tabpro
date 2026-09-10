package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.assertAcceleratorMatchesMenu;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.dispatchKeyAndDetectDialog;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.editorWithANote;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.editorWithMeasures;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponent;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findMenuItem;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.newFrame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Manual, tablas "Keyboard Shortcuts" (Reference, pp. 79 a 81, transcritas ya letra por letra en
 * ManualKeyboardShortcutsTest contra el catalogo de comandos). Esta clase no repite esa
 * comparacion estatica: ejercita el camino real -KeyEvent despachado sobre el lienzo real de una
 * MainFrame real- para los atajos que la partitura vive adentro de un JScrollPane y un
 * JSplitPane, que traen atajos de fabrica propios.
 */
@Tag("integracion")
@ResourceLock(AuditSupport.SWING_LOCK)
class KeyboardShortcutsAuditTest {

    /**
     * Barrido exhaustivo y no adivinado: recorre todos los JMenuItem reales de la barra real y
     * pregunta, para cada acelerador, si el JScrollPane o el JSplitPane reales de esa misma
     * ventana ya tienen esa tecla ocupada en su WHEN_ANCESTOR_OF_FOCUSED_COMPONENT -diga lo que
     * diga la tecla-. Da exactamente cinco: Ctrl+Home, Ctrl+Fin, F6, F8 y Ctrl+Tab, las mismas
     * que ya documenta el comentario de AcceleratorGuard. Si el dia de manana aparece una sexta,
     * este test la va a mostrar en la salida sin que haga falta adivinarla a mano.
     */
    @Test
    void lasUnicasCincoTeclasQueElScrollPaneYElSplitPaneYaOcupabanSonLasDocumentadas() throws Exception {
        Editor editor = AuditSupport.blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JScrollPane scrollPane = findComponent(frame.getContentPane(), JScrollPane.class);
            JSplitPane splitPane = findComponent(frame.getContentPane(), JSplitPane.class);
            assertNotNull(scrollPane);
            assertNotNull(splitPane);

            List<String> colisiones = new ArrayList<>();
            for (JMenuItem item : AuditSupport.allMenuItems(frame.getJMenuBar())) {
                KeyStroke accelerator = item.getAccelerator();
                if (accelerator == null) {
                    continue;
                }
                boolean ocupadaEnScroll =
                        scrollPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).get(accelerator) != null;
                boolean ocupadaEnSplit =
                        splitPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).get(accelerator) != null;
                if (ocupadaEnScroll || ocupadaEnSplit) {
                    colisiones.add(item.getText());
                }
            }

            assertEquals(
                    java.util.Set.of("Primer compás", "Último compás", "Propiedades de la pista…",
                            "Configurar página…", "Marcador siguiente"),
                    java.util.Set.copyOf(colisiones),
                    "las cinco colisiones documentadas no cambiaron; si esto falla hay una nueva (o una que se arreglo)");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void f6AbreLasPropiedadesDeLaPistaConLaPartituraEnfocada() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);

            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Propiedades de la pista…");
            assertNotNull(item);
            assertEquals(KeyStroke.getKeyStroke("F6"), item.getAccelerator());

            boolean abrioUnDialogo = dispatchKeyAndDetectDialog(canvas, KeyStroke.getKeyStroke("F6"), 800);

            assertTrue(abrioUnDialogo,
                    "F6 con la partitura enfocada tiene que abrir 'Propiedades de la pista', "
                            + "igual que el menu Pista > Propiedades");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void f8AbreConfigurarPaginaConLaPartituraEnfocada() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);

            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Configurar página…");
            assertNotNull(item);
            assertEquals(KeyStroke.getKeyStroke("F8"), item.getAccelerator());

            boolean abrioUnDialogo = dispatchKeyAndDetectDialog(canvas, KeyStroke.getKeyStroke("F8"), 800);

            assertTrue(abrioUnDialogo,
                    "F8 con la partitura enfocada tiene que abrir 'Configurar página', "
                            + "igual que el menu Archivo > Configurar página");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void ctrlTabPorMenuYPorAtajoMuevenElCursorAlMarcadorSiguiente() throws Exception {
        assertAcceleratorMatchesMenu("Marcador siguiente", () -> {
            Editor editor = editorWithMeasures(2);
            editor.moveTo(1, 0, editor.cursor().string());
            editor.setMarker(Marker.named("Solo"));
            editor.moveToFirstMeasure();
            return editor;
        });
    }

    // ---- una muestra amplia de atajos que NO viven adentro del JScrollPane/JSplitPane ---------

    @Test
    void deshacerPorMenuYPorAtajoCtrlZCoinciden() throws Exception {
        assertAcceleratorMatchesMenu("Deshacer", () -> {
            Editor editor = editorWithANote();
            editor.setFret(5);
            return editor;
        });
    }

    @Test
    void cortarPorMenuYPorAtajoCtrlXCoinciden() throws Exception {
        assertAcceleratorMatchesMenu("Cortar", AuditSupport::editorWithANote);
    }

    @Test
    void acortarLaFiguraPorMenuYPorAtajoMasCoinciden() throws Exception {
        assertAcceleratorMatchesMenu("Acortar la figura", AuditSupport::editorWithANote);
    }

    @Test
    void subirUnSemitonoPorMenuYPorAtajoShiftMasCoinciden() throws Exception {
        assertAcceleratorMatchesMenu("Subir un semitono", AuditSupport::editorWithANote);
    }

    @Test
    void ligadoHammerPorMenuYPorAtajoHCoinciden() throws Exception {
        assertAcceleratorMatchesMenu("Ligado (hammer on / pull off)", AuditSupport::editorWithANote);
    }

    @Test
    void vibratoPorMenuYPorAtajoVCoinciden() throws Exception {
        assertAcceleratorMatchesMenu("Vibrato", AuditSupport::editorWithANote);
    }

    @Test
    void palmMutePorMenuYPorAtajoPCoinciden() throws Exception {
        assertAcceleratorMatchesMenu("Palm mute", AuditSupport::editorWithANote);
    }

    @Test
    void notaMuertaPorMenuYPorAtajoXCoinciden() throws Exception {
        assertAcceleratorMatchesMenu("Nota muerta", AuditSupport::editorWithANote);
    }

    @Test
    void atenuarLaVozInactivaPorAtajoCtrlGCambiaElCanvasReal() throws Exception {
        // No es un comando de modelo (Score/Cursor): pinta distinto, asi que se mira el canvas.
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            boolean antes = canvas.graysTheInactiveVoice();

            AuditSupport.pressKey(canvas, KeyStroke.getKeyStroke("ctrl G"));

            assertEquals(!antes, canvas.graysTheInactiveVoice(),
                    "Ctrl+G, despachado de verdad sobre el lienzo, tiene que alternar el atenuado real");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void pistaSiguientePorMenuYPorAtajoCtrlDownCoinciden() throws Exception {
        assertAcceleratorMatchesMenu("Pista siguiente", () -> {
            Editor editor = editorWithANote();
            editor.addTrack(com.gstncaruso.tabpro.core.model.Track.standardBass("Bajo"));
            editor.selectTrack(0);
            return editor;
        });
    }

    @Test
    void compasAnteriorPorMenuYPorAtajoCtrlLeftCoinciden() throws Exception {
        assertAcceleratorMatchesMenu("Compás anterior", () -> {
            Editor editor = editorWithMeasures(2);
            editor.moveToLastMeasure();
            return editor;
        });
    }
}
