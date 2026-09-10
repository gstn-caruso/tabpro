package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.editorWithMeasures;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponent;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.newFrame;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.pressKey;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Auditoria visual 2, hallazgo 2: {@code Editor.moveCursor()} no reseteaba el ancla de la
 * seleccion, asi que cualquier navegacion pura la dejaba resaltada para siempre en vez de
 * colapsarla; y "Seleccionar todo" (Ctrl+A) tampoco se veia porque {@code ScoreCanvas} pintaba
 * su propia seleccion, armada solo desde el mouse, nunca la del {@code Editor}. Este test cierra
 * los dos hallazgos con las teclas reales -Ctrl+A y una flecha sin Shift- despachadas sobre el
 * lienzo real, mirando lo mismo que {@code PageScorePainter} usa para pintar:
 * {@code ScoreCanvas#selection()}.
 */
@Tag("integracion")
@ResourceLock(AuditSupport.SWING_LOCK)
class SelectionPaintingAuditTest {

    @Test
    void ctrlAPintaLaSeleccionDeTodaLaPartituraEnElLienzoReal() throws Exception {
        Editor editor = editorWithMeasures(2);
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);

            pressKey(canvas, KeyStroke.getKeyStroke("ctrl A"));

            assertTrue(canvas.selection().isPresent(),
                    "Ctrl+A, despachado de verdad sobre el lienzo, tiene que dejar algo para pintar");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void unaFlechaSinShiftLimpiaLaSeleccionQueDejoCtrlA() throws Exception {
        Editor editor = editorWithMeasures(2);
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            pressKey(canvas, KeyStroke.getKeyStroke("ctrl A"));

            pressKey(canvas, KeyStroke.getKeyStroke("RIGHT"));

            assertTrue(canvas.selection().isEmpty(),
                    "una flecha sin Shift, despachada de verdad, tiene que limpiar lo que dejo Ctrl+A");
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}
