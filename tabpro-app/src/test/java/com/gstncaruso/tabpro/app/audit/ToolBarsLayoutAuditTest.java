package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.blankEditor;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findButtonByActionName;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponent;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findMenuItem;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.newFrame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
import com.gstncaruso.tabpro.ui.tracks.TrackPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * MainFrame extends JFrame, so building a real one requires a non-headless toolkit (see
 * AuditSupport); that is why this is an audit test instead of an ordinary one.
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

    @Test
    void elMenuVerMenusYBarrasEscondeYMuestraLaFilaDeDocumentoReal() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JToolBar documentBar = toolBarContaining(frame.getContentPane(), "Nuevo");
            assertTrue(documentBar.isVisible(), "la fila de documento arranca visible");

            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Documento y edición");
            assertNotNull(item, "no encontre 'Documento y edición' en Ver > Menus y barras");

            SwingUtilities.invokeAndWait(item::doClick);
            assertEquals(false, documentBar.isVisible(),
                    "el menu real tiene que esconder la fila de documento real");

            SwingUtilities.invokeAndWait(item::doClick);
            assertEquals(true, documentBar.isVisible(),
                    "el menu real tiene que volver a mostrar la fila de documento real");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void elMenuEfectosEscondeLaBarraRealYRecuerdaLaPreferencia() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        com.gstncaruso.tabpro.ui.Preferences preferences = new com.gstncaruso.tabpro.ui.Preferences();
        try {
            JToolBar effectsBar = toolBarContaining(frame.getContentPane(), "Nota muerta");
            assertTrue(effectsBar.isVisible(), "la barra de efectos arranca visible");

            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Efectos");
            assertNotNull(item, "no encontre 'Efectos' en Ver > Menus y barras");

            SwingUtilities.invokeAndWait(item::doClick);
            assertEquals(false, effectsBar.isVisible(), "el menu real tiene que esconder la barra de efectos real");
            assertEquals(false, preferences.effectsToolBarVisible(),
                    "esconderla desde el menu tiene que quedar guardado en las preferencias");

            SwingUtilities.invokeAndWait(item::doClick);
            assertEquals(true, effectsBar.isVisible());
            assertEquals(true, preferences.effectsToolBarVisible());
        } finally {
            preferences.setEffectsToolBarVisible(true);
            AuditSupport.dispose(frame);
        }
    }

    private JToolBar toolBarContaining(Container root, String actionLabel) {
        JButton button = findButtonByActionName(root, actionLabel);
        assertNotNull(button, "no encontre ningun boton real de accion \"" + actionLabel + "\"");
        Container parent = button.getParent();
        while (parent != null && !(parent instanceof JToolBar)) {
            parent = parent.getParent();
        }
        return (JToolBar) parent;
    }
}
