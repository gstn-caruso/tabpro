package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.blankEditor;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findButton;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponents;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findMenuItem;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.newFrame;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.tabContent;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.withDialog;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import com.gstncaruso.tabpro.ui.MainFrame;
import java.awt.Container;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@Tag("integration")
@ResourceLock(AuditSupport.SWING_LOCK)
class AddLyricsAndMarkersAuditTest {

    @Test
    void lyricsTypedInTheScoreInformationTabReachTheModel() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Información de la partitura…");
            assertNotNull(item, "could not find 'Información de la partitura…' in the real menu");

            withDialog(item::doClick, dialog -> {
                Container lyricsTab = tabContent(dialog, "Letra");
                assertNotNull(lyricsTab, "could not find the real 'Letra' tab");

                Container firstLineTab = tabContent(lyricsTab, "Línea 1");
                assertNotNull(firstLineTab, "could not find the real tab for line 1");

                var areas = findComponents(firstLineTab, JTextArea.class);
                assertTrue(areas.size() >= 1, "could not find the real text area for the first line");
                areas.get(0).setText("Hello world");

                findButton(dialog, "Aceptar").doClick();
            });

            assertEquals("Hello world", editor.score().lyrics().line(0).text(),
                    "what was typed in the real field for the first line must end up in the model");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void insertHereInTheMarkersDialogLeavesTheMarkerInTheCursorBar() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Insertar un marcador…");
            assertNotNull(item, "could not find 'Insertar un marcador…' in the real menu");
            assertTrue(editor.score().attributesOf(0).marker().isEmpty(), "the bar starts without a marker");

            withDialog(item::doClick, dialog -> {
                JTextField name = AuditSupport.findComponent(dialog, JTextField.class);
                assertNotNull(name, "could not find the real marker name field");
                name.setText("Chorus");

                findButton(dialog, "Insertar aquí").doClick();
                findButton(dialog, "Cerrar").doClick();
            });

            var marker = editor.score().attributesOf(0).marker();
            assertTrue(marker.isPresent(), "the real 'Insertar aquí' button must leave a marker in the model");
            assertEquals("Chorus", marker.get().name(),
                    "what was typed in the real field must be the marker's name");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void editingTheCursorMarkerInTheDialogLeavesTheNewNameInTheModel() throws Exception {
        Editor editor = blankEditor();
        editor.setMarker(Marker.named("Intro"));
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Editar el marcador…");
            assertNotNull(item, "could not find 'Editar el marcador…' in the real menu");

            withDialog(item::doClick, dialog -> {
                JTextField name = AuditSupport.findComponent(dialog, JTextField.class);
                assertNotNull(name, "could not find the real marker name field");
                name.setText("Chorus");

                findButton(dialog, "Guardar cambios").doClick();
                findButton(dialog, "Cerrar").doClick();
            });

            var marker = editor.score().attributesOf(0).marker();
            assertTrue(marker.isPresent(), "the real 'Guardar cambios' button must leave the marker in the model");
            assertEquals("Chorus", marker.get().name(),
                    "what was edited in the real field must end up in the model");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void deletingInTheMarkersTableRemovesTheMarkerFromTheModel() throws Exception {
        Editor editor = blankEditor();
        editor.setMarker(Marker.named("Intro"));
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Lista de marcadores…");
            assertNotNull(item, "could not find 'Lista de marcadores…' in the real menu");

            withDialog(item::doClick, dialog -> {
                JTable table = AuditSupport.findComponent(dialog, JTable.class);
                assertNotNull(table, "could not find the real markers table");
                table.setRowSelectionInterval(0, 0);

                findButton(dialog, "Borrar").doClick();
                findButton(dialog, "Cerrar").doClick();
            });

            assertTrue(editor.score().attributesOf(0).marker().isEmpty(),
                    "the real 'Borrar' button must remove the marker from the model");
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}
