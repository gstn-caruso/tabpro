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

@Tag("integracion")
@ResourceLock(AuditSupport.SWING_LOCK)
class AddLyricsAndMarkersAuditTest {

    @Test
    void lyricsTypedInTheScoreInformationTabReachTheModel() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Información de la partitura…");
            assertNotNull(item, "no encontre 'Información de la partitura…' en el menu real");

            withDialog(item::doClick, dialog -> {
                Container lyricsTab = tabContent(dialog, "Letra");
                assertNotNull(lyricsTab, "no encontre la solapa real 'Letra'");

                Container firstLineTab = tabContent(lyricsTab, "Línea 1");
                assertNotNull(firstLineTab, "no encontre la pestaña real de la linea 1");

                var areas = findComponents(firstLineTab, JTextArea.class);
                assertTrue(areas.size() >= 1, "no encontre el area real de texto de la primer linea");
                areas.get(0).setText("Hola mundo");

                findButton(dialog, "Aceptar").doClick();
            });

            assertEquals("Hola mundo", editor.score().lyrics().line(0).text(),
                    "lo tecleado en el campo real de la primer linea tiene que quedar en el modelo");
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
            assertNotNull(item, "no encontre 'Insertar un marcador…' en el menu real");
            assertTrue(editor.score().attributesOf(0).marker().isEmpty(), "el compas arranca sin marcador");

            withDialog(item::doClick, dialog -> {
                JTextField name = AuditSupport.findComponent(dialog, JTextField.class);
                assertNotNull(name, "no encontre el campo real de nombre del marcador");
                name.setText("Estribillo");

                findButton(dialog, "Insertar aquí").doClick();
                findButton(dialog, "Cerrar").doClick();
            });

            var marker = editor.score().attributesOf(0).marker();
            assertTrue(marker.isPresent(), "el boton real 'Insertar aquí' tiene que dejar un marcador en el modelo");
            assertEquals("Estribillo", marker.get().name(),
                    "el nombre tecleado en el campo real tiene que ser el del marcador");
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
            assertNotNull(item, "no encontre 'Editar el marcador…' en el menu real");

            withDialog(item::doClick, dialog -> {
                JTextField name = AuditSupport.findComponent(dialog, JTextField.class);
                assertNotNull(name, "no encontre el campo real de nombre del marcador");
                name.setText("Estribillo");

                findButton(dialog, "Guardar cambios").doClick();
                findButton(dialog, "Cerrar").doClick();
            });

            var marker = editor.score().attributesOf(0).marker();
            assertTrue(marker.isPresent(), "el boton real 'Guardar cambios' tiene que dejar el marcador en el modelo");
            assertEquals("Estribillo", marker.get().name(),
                    "el nombre editado en el campo real tiene que quedar en el modelo");
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
            assertNotNull(item, "no encontre 'Lista de marcadores…' en el menu real");

            withDialog(item::doClick, dialog -> {
                JTable table = AuditSupport.findComponent(dialog, JTable.class);
                assertNotNull(table, "no encontre la tabla real de marcadores");
                table.setRowSelectionInterval(0, 0);

                findButton(dialog, "Borrar").doClick();
                findButton(dialog, "Cerrar").doClick();
            });

            assertTrue(editor.score().attributesOf(0).marker().isEmpty(),
                    "el boton real 'Borrar' tiene que quitar el marcador del modelo");
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}
