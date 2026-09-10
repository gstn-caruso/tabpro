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
import com.gstncaruso.tabpro.ui.MainFrame;
import java.awt.Container;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Manual, "Add Lyrics" (linea 1377) y "Add Markers" (linea 1448) del texto extraido. La letra no
 * tiene comando propio en el catalogo -se escribe en la solapa "Letra" de Informacion de la
 * partitura, F5-; los marcadores se insertan desde su propio dialogo (Marcador > Insertar,
 * Shift+Insert). Los dos dialogos reales, con sus campos de texto reales.
 */
@Tag("integracion")
@ResourceLock(AuditSupport.SWING_LOCK)
class AddLyricsAndMarkersAuditTest {

    @Test
    void laLetraEscritaEnLaSolapaRealDeInformacionDeLaPartituraLlegaAlModelo() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Información de la partitura…");
            assertNotNull(item, "no encontre 'Información de la partitura…' en el menu real");

            withDialog(item::doClick, dialog -> {
                Container letra = tabContent(dialog, "Letra");
                assertNotNull(letra, "no encontre la solapa real 'Letra'");

                // Cada fila tiene ademas un JSpinner (compas inicial), y por adentro un
                // JSpinner es, el, un JFormattedTextField: se lo saca para quedarse solo con
                // los campos reales de texto de la letra.
                var campos = findComponents(letra, JTextField.class).stream()
                        .filter(field -> !(field instanceof javax.swing.JFormattedTextField))
                        .toList();
                assertTrue(campos.size() >= 1, "no encontre los campos reales de las lineas de letra");
                campos.get(0).setText("Hola mundo");

                findButton(dialog, "Aceptar").doClick();
            });

            assertEquals("Hola mundo", editor.score().lyrics().line(0).text(),
                    "lo tecleado en el campo real de la primer linea tiene que quedar en el modelo");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void insertarAquiEnElDialogoRealDeMarcadoresDejaElMarcadorEnElCompasDelCursor() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Insertar un marcador…");
            assertNotNull(item, "no encontre 'Insertar un marcador…' en el menu real");
            assertTrue(editor.score().attributesOf(0).marker().isEmpty(), "el compas arranca sin marcador");

            withDialog(item::doClick, dialog -> {
                JTextField nombre = AuditSupport.findComponent(dialog, JTextField.class);
                assertNotNull(nombre, "no encontre el campo real de nombre del marcador");
                nombre.setText("Estribillo");

                findButton(dialog, "Insertar aqui").doClick();
                findButton(dialog, "Cerrar").doClick();
            });

            var marker = editor.score().attributesOf(0).marker();
            assertTrue(marker.isPresent(), "el boton real 'Insertar aqui' tiene que dejar un marcador en el modelo");
            assertEquals("Estribillo", marker.get().name(),
                    "el nombre tecleado en el campo real tiene que ser el del marcador");
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}
