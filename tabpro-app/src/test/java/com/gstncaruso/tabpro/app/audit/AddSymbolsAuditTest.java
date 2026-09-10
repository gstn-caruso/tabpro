package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.awaitDialog;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.awaitFocusOwner;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.dispose;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.editorWithANote;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findButton;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponent;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponents;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findMenuItem;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.newFrame;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.withDialog;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.effects.BendType;
import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import com.gstncaruso.tabpro.core.model.effects.Trill;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@Tag("integration")
@ResourceLock(AuditSupport.SWING_LOCK)
class AddSymbolsAuditTest {

    @Test
    void bendByTheBShortcutOpensTheRealDialogAndTheChosenValueReachesTheModel() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Bend…");
            assertNotNull(item, "could not find 'Bend…' in the real menu");
            assertEquals(javax.swing.KeyStroke.getKeyStroke("B"), item.getAccelerator());

            withDialog(() -> canvas.dispatchEvent(pressed(canvas, KeyEvent.VK_B)), dialog -> {
                Container bendTab = tabContent(dialog, "Bend");
                assertNotNull(bendTab, "could not find the Bend tab in the real dialog");

                JCheckBox active = findComponent(bendTab, JCheckBox.class);
                assertNotNull(active);
                if (!active.isSelected()) {
                    active.doClick();
                }

                @SuppressWarnings("unchecked")
                JComboBox<BendType> type = (JComboBox<BendType>) findComponent(bendTab, JComboBox.class);
                assertNotNull(type);
                type.setSelectedItem(BendType.PREBEND);

                JSpinner height = findComponent(bendTab, JSpinner.class);
                assertNotNull(height);
                height.setValue(8);

                findButton(dialog, "Aceptar").doClick();
            });

            var bend = editor.currentNote().orElseThrow().effects().bend();
            assertTrue(bend.isPresent(), "the bend chosen in the real dialog must reach the model");
            assertEquals(BendType.PREBEND, bend.get().type(),
                    "the type chosen in the real dialog's combo must be the one that ended up in the model");
            assertEquals(8, bend.get().peakQuarterTones(),
                    "the height chosen in the real dialog's spinner must be the one that ended up in the model");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void tremoloBarByTheMenuOpensTheRealDialogAndItsOwnChosenTypeReachesTheModel() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Palanca…");
            assertNotNull(item, "could not find 'Palanca…' in the real menu");

            withDialog(item::doClick, dialog -> {
                Container tremoloBarTab = tabContent(dialog, "Palanca");
                assertNotNull(tremoloBarTab, "could not find the Palanca tab in the real dialog");

                JCheckBox active = findComponent(tremoloBarTab, JCheckBox.class);
                assertNotNull(active);
                if (!active.isSelected()) {
                    active.doClick();
                }

                @SuppressWarnings("unchecked")
                JComboBox<BendType> type = (JComboBox<BendType>) findComponent(tremoloBarTab, JComboBox.class);
                assertNotNull(type);
                assertEquals(BendType.tremoloBarTypes(), comboValues(type),
                        "the Palanca tab must offer its own six types, not the Bend ones");
                type.setSelectedItem(BendType.DIVE);

                findButton(dialog, "Aceptar").doClick();
            });

            var tremoloBar = editor.currentBeat().effects().tremoloBar();
            assertTrue(tremoloBar.isPresent(), "the tremolo bar chosen in the real dialog must reach the model");
            assertEquals(BendType.DIVE, tremoloBar.get().type(),
                    "the type chosen in the real tremolo bar combo must be the one that ended up in the model");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void trillByTheMenuOpensTheRealDialogAndTheChosenValueReachesTheModel() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Trino…");
            assertNotNull(item, "could not find 'Trino…' in the real menu");

            withDialog(item::doClick, dialog -> {
                Container trillTab = tabContent(dialog, "Trino");
                assertNotNull(trillTab);

                JCheckBox active = findComponent(trillTab, JCheckBox.class);
                if (!active.isSelected()) {
                    active.doClick();
                }

                JSpinner fret = findComponent(trillTab, JSpinner.class);
                fret.setValue(7);

                findButton(dialog, "Aceptar").doClick();
            });

            var trill = editor.currentNote().orElseThrow().effects().trill();
            assertTrue(trill.isPresent(), "the trill chosen in the real dialog must reach the model");
            assertEquals(7, trill.map(Trill::fret).orElseThrow(),
                    "the fret chosen in the real spinner must be the one that ended up in the model");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void harmonicsByTheMenuOpensTheRealDialogAndTheChosenTypeReachesTheModel() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Armónicos…");
            assertNotNull(item, "could not find 'Armónicos…' in the real menu");

            withDialog(item::doClick, dialog -> {
                Container harmonicsTab = tabContent(dialog, "Armónicos");
                assertNotNull(harmonicsTab);

                JCheckBox active = findComponent(harmonicsTab, JCheckBox.class);
                if (!active.isSelected()) {
                    active.doClick();
                }

                @SuppressWarnings("unchecked")
                JComboBox<HarmonicType> type = (JComboBox<HarmonicType>) findComponent(harmonicsTab, JComboBox.class);
                type.setSelectedItem(HarmonicType.ARTIFICIAL);

                findButton(dialog, "Aceptar").doClick();
            });

            var harmonic = editor.currentNote().orElseThrow().effects().harmonic();
            assertEquals(HarmonicType.ARTIFICIAL, harmonic.orElseThrow(),
                    "the harmonic type chosen in the real combo must be the one that ended up in the model");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void letRingOptionsOpenTheRealWizardWithItsOwnTitleAndFocus() throws Exception {
        theOptionsWizardOpensWithItsOwnTitleAndFocus("Opciones de let ring…", "Opciones de let ring", 0);
    }

    @Test
    void palmMuteOptionsOpenTheRealWizardWithItsOwnTitleAndFocus() throws Exception {
        theOptionsWizardOpensWithItsOwnTitleAndFocus("Opciones de palm mute…", "Opciones de palm mute", 1);
    }

    @Test
    void dynamicsOptionsOpenTheRealWizardWithItsOwnTitleAndFocus() throws Exception {
        theOptionsWizardOpensWithItsOwnTitleAndFocus("Opciones de dinámica…", "Opciones de dinámica", 2);
    }

    private void theOptionsWizardOpensWithItsOwnTitleAndFocus(
            String menuLabel, String expectedTitle, int expectedComboIndex) throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), menuLabel);
            assertNotNull(item, "could not find '" + menuLabel + "' in the real menu");

            JDialog dialog = awaitDialog(item::doClick, 5000);
            try {
                assertEquals(expectedTitle, dialog.getTitle(),
                        "each options command must open with its own title");

                @SuppressWarnings("rawtypes")
                List<JComboBox> combos = findComponents(dialog, JComboBox.class);
                assertEquals(3, combos.size(), "the real wizard must bring the three combos: let ring, palm mute and dynamics");

                assertTrue(awaitFocusOwner(combos.get(expectedComboIndex), 2000),
                        "the '" + expectedTitle + "' combo must start with the real focus");
            } finally {
                SwingUtilities.invokeAndWait(() -> findButton(dialog, "Cancelar").doClick());
            }
        } finally {
            dispose(frame);
        }
    }

    private static Container tabContent(JDialog dialog, String tabTitle) {
        JTabbedPane tabs = findComponent(dialog, JTabbedPane.class);
        assertNotNull(tabs, "could not find the real JTabbedPane for note effects");
        int index = tabs.indexOfTab(tabTitle);
        assertTrue(index >= 0, "could not find the tab \"" + tabTitle + "\"");
        return (Container) tabs.getComponentAt(index);
    }

    private static KeyEvent pressed(java.awt.Component target, int keyCode) {
        return new KeyEvent(target, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyCode, KeyEvent.CHAR_UNDEFINED);
    }

    private static java.util.List<BendType> comboValues(JComboBox<BendType> combo) {
        java.util.List<BendType> values = new java.util.ArrayList<>();
        for (int i = 0; i < combo.getItemCount(); i++) {
            values.add(combo.getItemAt(i));
        }
        return values;
    }
}
