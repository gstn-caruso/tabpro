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
@Tag("integration")
@ResourceLock(AuditSupport.SWING_LOCK)
class ToolBarsLayoutAuditTest {

    @Test
    void theEffectsToolbarStaysBetweenTheScoreAndTheMixingConsole() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            Container scoreArea = findComponent(frame.getContentPane(), ScoreCanvas.class).getParent();
            while (scoreArea != null && !(scoreArea.getLayout() instanceof BorderLayout)) {
                scoreArea = scoreArea.getParent();
            }
            assertNotNull(scoreArea, "could not find the score panel built with BorderLayout");

            Component south = ((BorderLayout) scoreArea.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
            assertTrue(south instanceof JToolBar,
                    "there must be a real toolbar below the score");
            assertNotNull(findButtonByActionName((Container) south, "Nota muerta"),
                    "the bar below the score must be the effects one, not another one");

            TrackPanel mixTable = findComponent(frame.getContentPane(), TrackPanel.class);
            assertNotNull(mixTable, "could not find the real mixing console");
            javax.swing.JSplitPane split = findComponent(frame.getContentPane(), javax.swing.JSplitPane.class);
            assertNotNull(split, "could not find the split that divides the score and the mixing console");
            assertTrue(split.getBottomComponent() == mixTable,
                    "the mixing console must be in the other half of the split, after the score");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void theViewMenusAndToolbarsMenuHidesAndShowsTheRealDocumentRow() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JToolBar documentBar = toolBarContaining(frame.getContentPane(), "Nuevo");
            assertTrue(documentBar.isVisible(), "the document row starts visible");

            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Documento y edición");
            assertNotNull(item, "could not find 'Documento y edición' in Ver > Menus y barras");

            SwingUtilities.invokeAndWait(item::doClick);
            assertEquals(false, documentBar.isVisible(),
                    "the real menu must hide the real document row");

            SwingUtilities.invokeAndWait(item::doClick);
            assertEquals(true, documentBar.isVisible(),
                    "the real menu must show the real document row again");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void theEffectsMenuHidesTheRealToolbarAndRemembersThePreference() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        com.gstncaruso.tabpro.ui.Preferences preferences = new com.gstncaruso.tabpro.ui.Preferences();
        try {
            JToolBar effectsBar = toolBarContaining(frame.getContentPane(), "Nota muerta");
            assertTrue(effectsBar.isVisible(), "the effects bar starts visible");

            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Efectos");
            assertNotNull(item, "could not find 'Efectos' in Ver > Menus y barras");

            SwingUtilities.invokeAndWait(item::doClick);
            assertEquals(false, effectsBar.isVisible(), "the real menu must hide the real effects bar");
            assertEquals(false, preferences.effectsToolBarVisible(),
                    "hiding it from the menu must be saved in the preferences");

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
        assertNotNull(button, "could not find any real action button \"" + actionLabel + "\"");
        Container parent = button.getParent();
        while (parent != null && !(parent instanceof JToolBar)) {
            parent = parent.getParent();
        }
        return (JToolBar) parent;
    }
}
