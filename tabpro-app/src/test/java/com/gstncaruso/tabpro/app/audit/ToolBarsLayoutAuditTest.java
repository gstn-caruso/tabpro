package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.blankEditor;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findButtonByActionName;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponent;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.newFrame;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
import com.gstncaruso.tabpro.ui.tracks.TrackPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import javax.swing.JToolBar;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Guitar Pro 5, manual pagina 14: la barra de efectos va abajo de la partitura, pegada a la
 * mesa de mezcla, no arriba junto a las otras tres filas. MainFrame extiende JFrame, asi que
 * armarlo de verdad exige un toolkit no headless (ver AuditSupport); por eso esta auditoria, en
 * vez de un test comun.
 */
@Tag("integracion")
@ResourceLock(AuditSupport.SWING_LOCK)
class ToolBarsLayoutAuditTest {

    @Test
    void laBarraDeEfectosQuedaEntreLaPartituraYLaMesaDeMezcla() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            Container scoreArea = findComponent(frame.getContentPane(), ScoreCanvas.class).getParent();
            while (scoreArea != null && !(scoreArea.getLayout() instanceof BorderLayout)) {
                scoreArea = scoreArea.getParent();
            }
            assertNotNull(scoreArea, "no encontre el panel de la partitura armado con BorderLayout");

            Component south = ((BorderLayout) scoreArea.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
            assertTrue(south instanceof JToolBar,
                    "abajo de la partitura tiene que haber una barra de herramientas real");
            assertNotNull(findButtonByActionName((Container) south, "Nota muerta"),
                    "la barra de abajo de la partitura tiene que ser la de efectos, no otra");

            TrackPanel mixTable = findComponent(frame.getContentPane(), TrackPanel.class);
            assertNotNull(mixTable, "no encontre la mesa de mezcla real");
            javax.swing.JSplitPane split = findComponent(frame.getContentPane(), javax.swing.JSplitPane.class);
            assertNotNull(split, "no encontre el split que reparte la partitura y la mesa de mezcla");
            assertTrue(split.getBottomComponent() == mixTable,
                    "la mesa de mezcla tiene que quedar en la otra mitad del split, despues de la partitura");
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}
