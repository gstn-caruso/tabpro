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
import org.junit.jupiter.api.parallel.ResourceLock;

@Tag("integration")
@ResourceLock(AuditSupport.SWING_LOCK)
class WriteAScoreAuditTest {

    @Test
    void theFretDigitWritesTheNoteInTheRealModel() throws Exception {
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
    void theArrowsMoveTheRealCursorOfTheModel() throws Exception {
        Editor editor = editorWithMeasures(1);
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);

            int initialString = editor.cursor().string();
            pressKey(canvas, KeyStroke.getKeyStroke("DOWN"));
            assertNotEquals(initialString, editor.cursor().string(),
                    "la flecha Abajo, despachada de verdad sobre el lienzo, tiene que mover la cuerda del cursor");

            pressKey(canvas, KeyStroke.getKeyStroke("RIGHT"));
            assertEquals(1, editor.cursor().beat(), "la flecha Derecha tiene que avanzar al beat siguiente");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void rawBackspaceDeletesTheNoteWithoutGoingThroughACommand() throws Exception {
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
     * Swing treats Tab as a focus traversal key by default; {@code ScoreCanvas} disables
     * FORWARD_TRAVERSAL_KEYS so a real Tab, dispatched on the real canvas, reaches its own
     * {@code KeyboardEditing} binding instead.
     */
    @Test
    void rawTabChangesNotationWithoutMovingTheCursor() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            assertNotNull(canvas.getInputMap(javax.swing.JComponent.WHEN_FOCUSED)
                    .get(KeyStroke.getKeyStroke("TAB")), "KeyboardEditing tiene que declarar el binding de Tab");

            var initialNotation = editor.cursor().notation();
            int initialBar = editor.cursor().measure();
            int initialBeat = editor.cursor().beat();
            int initialString = editor.cursor().string();
            pressKey(canvas, KeyStroke.getKeyStroke("TAB"));

            assertNotEquals(initialNotation, editor.cursor().notation(),
                    "Tab, despachado de verdad sobre el lienzo, tiene que alternar tablatura/pentagrama");
            assertEquals(initialBar, editor.cursor().measure(), "Tab no tiene que mover el cursor de compas");
            assertEquals(initialBeat, editor.cursor().beat(), "Tab no tiene que mover el cursor de beat");
            assertEquals(initialString, editor.cursor().string(), "Tab no tiene que mover el cursor de cuerda");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void restByMenuAndByRShortcutLeaveTheSameBeat() throws Exception {
        assertAcceleratorMatchesMenu("Silencio", AuditSupport::editorWithANote);
    }

    @Test
    void dottedByMenuAndByDotShortcutMatch() throws Exception {
        assertAcceleratorMatchesMenu("Puntillo", AuditSupport::editorWithANote);
    }

    @Test
    void tyingTheNoteByMenuAndByLShortcutMatch() throws Exception {
        assertAcceleratorMatchesMenu("Ligar la nota", AuditSupport::editorWithANote);
    }

    @Test
    void tripletByMenuAndBySlashShortcutMatch() throws Exception {
        assertAcceleratorMatchesMenu("Tresillo", AuditSupport::editorWithANote);
    }

    @Test
    void insertingABarByMenuAndByCtrlInsertShortcutMatch() throws Exception {
        assertAcceleratorMatchesMenu("Insertar un compás", AuditSupport::blankEditor);
    }

    /**
     * The JScrollPane wrapping the score comes with a built-in "scrollHome" bound to Ctrl+Home;
     * AcceleratorGuard leaves it with no action registered (processKeyBinding returns false), so
     * the key keeps propagating up to the real "First measure" shortcut.
     */
    @Test
    void ctrlHomeByMenuAndByShortcutMoveTheCursorToTheFirstBar() throws Exception {
        assertAcceleratorMatchesMenu("Primer compás", () -> {
            Editor editor = editorWithMeasures(3);
            editor.moveToLastMeasure();
            return editor;
        });
    }

    /** Same mechanism as Ctrl+Home, with the JScrollPane's built-in "scrollEnd". */
    @Test
    void ctrlEndByMenuAndByShortcutMoveTheCursorToTheLastBar() throws Exception {
        assertAcceleratorMatchesMenu("Último compás", () -> editorWithMeasures(3));
    }

    @Test
    void voice2ByMenuAndByCtrl2ShortcutMatch() throws Exception {
        assertAcceleratorMatchesMenu("Voz 2 (bajos)", AuditSupport::editorWithANote);
    }

    @Test
    void deletingTheBeatByMenuAndByCtrlDeleteShortcutMatch() throws Exception {
        assertAcceleratorMatchesMenu("Borrar el beat", () -> {
            Editor editor = editorWithANote();
            editor.insertBeat();
            editor.moveTo(0, 0, editor.cursor().string());
            return editor;
        });
    }

    @Test
    void theQuarterNoteValueCanBeChosenFromTheRealToolbarButton() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            var button = findButtonByActionName(frame.getContentPane(), "Negra");
            assertNotNull(button, "no encontre en la barra real el boton de figura Negra");

            SwingUtilities.invokeAndWait(button::doClick);

            assertEquals(com.gstncaruso.tabpro.core.model.NoteValue.QUARTER, editor.currentBeat().duration().value(),
                    "el boton real de la barra tiene que dejar la figura en negra en el modelo");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void theNoteValueIsAlsoInTheNoteMenu() throws Exception {
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
