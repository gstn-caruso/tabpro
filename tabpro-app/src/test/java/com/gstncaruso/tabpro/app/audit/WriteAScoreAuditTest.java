package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.assertAcceleratorMatchesMenu;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.editorWithANote;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.editorWithMeasures;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findButtonByActionName;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponent;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findMenuItem;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.newFrame;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.pressKey;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.typeChar;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Manual, "Write a Score" (linea 481 del texto extraido): escribir notas con los digitos del
 * traste, moverse con las flechas, y los atajos de edicion basica (silencio, puntillo, ligadura,
 * tresillo, insertar/borrar compas y beat, voces). Cada comando se ejercita por su JMenuItem real
 * -boton real en el caso de los valores de figura, que no tienen menu propio en la barra- y por
 * el KeyEvent real de su atajo, nunca invocando el Action a mano.
 */
@Tag("integracion")
class WriteAScoreAuditTest {

    @Test
    void elDigitoDelTrasteEscribeLaNotaEnElModeloReal() throws Exception {
        Editor editor = AuditSupport.blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            assertNotNull(canvas, "no encontre el ScoreCanvas real en la ventana");

            assertEquals(java.util.Optional.empty(), editor.currentNote(), "el compas arranca en silencio");

            typeChar(canvas, '3');

            assertEquals(3, editor.currentNote().orElseThrow().fret(),
                    "el digito '3' tecleado sobre el lienzo real tiene que dejar el traste 3 en el modelo");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void lasFlechasMuevenElCursorRealDelModelo() throws Exception {
        Editor editor = editorWithMeasures(1);
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);

            int cuerdaInicial = editor.cursor().string();
            pressKey(canvas, KeyStroke.getKeyStroke("DOWN"));
            assertNotEquals(cuerdaInicial, editor.cursor().string(),
                    "la flecha Abajo, despachada de verdad sobre el lienzo, tiene que mover la cuerda del cursor");

            pressKey(canvas, KeyStroke.getKeyStroke("RIGHT"));
            assertEquals(1, editor.cursor().beat(), "la flecha Derecha tiene que avanzar al beat siguiente");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void elBackspaceCrudoBorraLaNotaSinPasarPorUnComando() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);

            assertNotEquals(java.util.Optional.empty(), editor.currentNote());
            pressKey(canvas, KeyStroke.getKeyStroke("BACK_SPACE"));
            assertEquals(java.util.Optional.empty(), editor.currentNote(),
                    "Backspace, tecla cruda de KeyboardEditing, tiene que borrar la nota del cursor");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    /**
     * HALLAZGO (ver docs/auditoria-uso-real.md, "Write a Score"): el manual (linea 780 del
     * texto extraido) pide que Tab alterne tablatura/pentagrama sin mover el cursor, y
     * {@code KeyboardEditing} tiene el binding -esta comprobado en su propio test unitario-,
     * pero Tab tambien es de fabrica una tecla de foco (FORWARD_TRAVERSAL_KEYS) para cualquier
     * JComponent, y {@code ScoreCanvas} nunca llama a setFocusTraversalKeysEnabled(false). En la
     * ventana real, AWT se queda con la tecla para mover el foco antes de que
     * KeyboardEditing la vea: este test documenta que, hoy, Tab NO cambia de notacion.
     */
    @Test
    void tabCrudoNoCambiaDeNotacionPorQuedarseConElFocoAntes() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            assertNotNull(canvas.getInputMap(javax.swing.JComponent.WHEN_FOCUSED)
                    .get(KeyStroke.getKeyStroke("TAB")), "KeyboardEditing tiene que declarar el binding de Tab");

            var notacionInicial = editor.cursor().notation();
            pressKey(canvas, KeyStroke.getKeyStroke("TAB"));

            assertEquals(notacionInicial, editor.cursor().notation(),
                    "HALLAZGO: Tab no llega a KeyboardEditing porque ScoreCanvas no desactiva "
                            + "sus teclas de foco (ver informe)");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void silencioPorMenuYPorAtajoRDejanElMismoBeat() throws Exception {
        assertAcceleratorMatchesMenu("Silencio", AuditSupport::editorWithANote);
    }

    @Test
    void puntilloPorMenuYPorAtajoPuntoCoinciden() throws Exception {
        assertAcceleratorMatchesMenu("Puntillo", AuditSupport::editorWithANote);
    }

    @Test
    void ligarLaNotaPorMenuYPorAtajoLCoinciden() throws Exception {
        assertAcceleratorMatchesMenu("Ligar la nota", AuditSupport::editorWithANote);
    }

    @Test
    void tresilloPorMenuYPorAtajoBarraCoinciden() throws Exception {
        assertAcceleratorMatchesMenu("Tresillo", AuditSupport::editorWithANote);
    }

    @Test
    void insertarUnCompasPorMenuYPorAtajoCtrlInsertCoinciden() throws Exception {
        assertAcceleratorMatchesMenu("Insertar un compás", AuditSupport::blankEditor);
    }

    /**
     * HALLAZGO (ver docs/auditoria-uso-real.md, "Write a Score"): esto es justo lo que
     * AcceleratorGuard deberia arreglar -el JScrollPane que envuelve la partitura trae de
     * fabrica un "scrollHome" para Ctrl+Home-, pero en vez de sacarle la tecla al JScrollPane le
     * pone una accion que no hace nada: Swing la encuentra, la da por atendida y el atajo real
     * de "Primer compás" nunca se llega a mirar.
     */
    @Test
    void ctrlHomeQuedaMudoAunqueElMenuPrimerCompasFunciona() throws Exception {
        AuditSupport.assertAcceleratorIsSwallowedBeforeReachingTheMenu("Primer compás", () -> {
            Editor editor = editorWithMeasures(3);
            editor.moveToLastMeasure();
            return editor;
        });
    }

    /** HALLAZGO: mismo mecanismo que Ctrl+Home, con el "scrollEnd" de fabrica del JScrollPane. */
    @Test
    void ctrlFinQuedaMudoAunqueElMenuUltimoCompasFunciona() throws Exception {
        AuditSupport.assertAcceleratorIsSwallowedBeforeReachingTheMenu("Último compás", () -> editorWithMeasures(3));
    }

    @Test
    void voz2PorMenuYPorAtajoCtrl2Coinciden() throws Exception {
        assertAcceleratorMatchesMenu("Voz 2 (bajos)", AuditSupport::editorWithANote);
    }

    @Test
    void borrarElBeatPorMenuYPorAtajoCtrlDeleteCoinciden() throws Exception {
        assertAcceleratorMatchesMenu("Borrar el beat", () -> {
            Editor editor = editorWithANote();
            editor.insertBeat();
            editor.moveTo(0, 0, editor.cursor().string());
            return editor;
        });
    }

    @Test
    void elValorDeFiguraNegraSePuedeElegirDesdeElBotonRealDeLaBarra() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            var boton = findButtonByActionName(frame.getContentPane(), "Negra");
            assertNotNull(boton, "no encontre en la barra real el boton de figura Negra");

            SwingUtilities.invokeAndWait(boton::doClick);

            assertEquals(com.gstncaruso.tabpro.core.model.NoteValue.QUARTER, editor.currentBeat().duration().value(),
                    "el boton real de la barra tiene que dejar la figura en negra en el modelo");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void elValorDeFiguraTambienEstaEnElMenuNota() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Corchea");
            assertNotNull(item, "no encontre en el menu real el item de figura Corchea");

            SwingUtilities.invokeAndWait(item::doClick);

            assertEquals(com.gstncaruso.tabpro.core.model.NoteValue.EIGHTH, editor.currentBeat().duration().value(),
                    "el item real del menu Nota tiene que dejar la figura en corchea en el modelo");
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}
