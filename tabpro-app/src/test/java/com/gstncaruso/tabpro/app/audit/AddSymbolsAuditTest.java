package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.editorWithANote;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findButton;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponent;
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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Manual, "Add Symbols" (linea 961 del texto extraido): bend, trino y armonicos, cada uno con su
 * propia solapa en la misma ventana de efectos (NoteEffectsDialog). Los tres pasan por el mismo
 * camino: el atajo real (una tecla, sin pasar por un menu con Robot) abre el dialogo modal real
 * -detectado por su WINDOW_OPENED, sin Robot-, se tocan sus controles reales (casilla "Activo",
 * spinner, combo) y se lee lo que quedo en el modelo despues de aceptar.
 */
@Tag("integracion")
@ResourceLock(AuditSupport.SWING_LOCK)
class AddSymbolsAuditTest {

    @Test
    void bendPorElAtajoBAbreElDialogoRealYElValorElegidoLlegaAlModelo() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Bend…");
            assertNotNull(item, "no encontre 'Bend…' en el menu real");
            assertEquals(javax.swing.KeyStroke.getKeyStroke("B"), item.getAccelerator());

            withDialog(() -> canvas.dispatchEvent(pressed(canvas, KeyEvent.VK_B)), dialog -> {
                Container bendTab = tabContent(dialog, "Bend");
                assertNotNull(bendTab, "no encontre la solapa Bend en el dialogo real");

                JCheckBox activo = findComponent(bendTab, JCheckBox.class);
                assertNotNull(activo);
                if (!activo.isSelected()) {
                    activo.doClick();
                }

                @SuppressWarnings("unchecked")
                JComboBox<BendType> tipo = (JComboBox<BendType>) findComponent(bendTab, JComboBox.class);
                assertNotNull(tipo);
                tipo.setSelectedItem(BendType.PREBEND);

                JSpinner altura = findComponent(bendTab, JSpinner.class);
                assertNotNull(altura);
                altura.setValue(8);

                findButton(dialog, "Aceptar").doClick();
            });

            var bend = editor.currentNote().orElseThrow().effects().bend();
            assertTrue(bend.isPresent(), "el bend elegido en el dialogo real tiene que llegar al modelo");
            assertEquals(BendType.PREBEND, bend.get().type(),
                    "el tipo elegido en el combo real del dialogo tiene que ser el que quedo en el modelo");
            assertEquals(8, bend.get().peakQuarterTones(),
                    "la altura elegida en el spinner real del dialogo tiene que ser la que quedo en el modelo");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void trinoPorElMenuAbreElDialogoRealYElValorElegidoLlegaAlModelo() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Trino…");
            assertNotNull(item, "no encontre 'Trino…' en el menu real");

            withDialog(item::doClick, dialog -> {
                Container trillTab = tabContent(dialog, "Trino");
                assertNotNull(trillTab);

                JCheckBox activo = findComponent(trillTab, JCheckBox.class);
                if (!activo.isSelected()) {
                    activo.doClick();
                }

                JSpinner fret = findComponent(trillTab, JSpinner.class);
                fret.setValue(7);

                findButton(dialog, "Aceptar").doClick();
            });

            var trill = editor.currentNote().orElseThrow().effects().trill();
            assertTrue(trill.isPresent(), "el trino elegido en el dialogo real tiene que llegar al modelo");
            assertEquals(7, trill.map(Trill::fret).orElseThrow(),
                    "el traste elegido en el spinner real tiene que ser el que quedo en el modelo");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void armonicosPorElMenuAbreElDialogoRealYElTipoElegidoLlegaAlModelo() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Armónicos…");
            assertNotNull(item, "no encontre 'Armónicos…' en el menu real");

            withDialog(item::doClick, dialog -> {
                Container harmonicsTab = tabContent(dialog, "Armonicos");
                assertNotNull(harmonicsTab);

                JCheckBox activo = findComponent(harmonicsTab, JCheckBox.class);
                if (!activo.isSelected()) {
                    activo.doClick();
                }

                @SuppressWarnings("unchecked")
                JComboBox<HarmonicType> tipo = (JComboBox<HarmonicType>) findComponent(harmonicsTab, JComboBox.class);
                tipo.setSelectedItem(HarmonicType.ARTIFICIAL);

                findButton(dialog, "Aceptar").doClick();
            });

            var harmonic = editor.currentNote().orElseThrow().effects().harmonic();
            assertEquals(HarmonicType.ARTIFICIAL, harmonic.orElseThrow(),
                    "el tipo de armonico elegido en el combo real tiene que ser el que quedo en el modelo");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    private static Container tabContent(JDialog dialog, String tabTitle) {
        JTabbedPane tabs = findComponent(dialog, JTabbedPane.class);
        assertNotNull(tabs, "no encontre el JTabbedPane real de efectos de nota");
        int index = tabs.indexOfTab(tabTitle);
        assertTrue(index >= 0, "no encontre la solapa \"" + tabTitle + "\"");
        return (Container) tabs.getComponentAt(index);
    }

    private static KeyEvent pressed(java.awt.Component target, int keyCode) {
        return new KeyEvent(target, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyCode, KeyEvent.CHAR_UNDEFINED);
    }
}
