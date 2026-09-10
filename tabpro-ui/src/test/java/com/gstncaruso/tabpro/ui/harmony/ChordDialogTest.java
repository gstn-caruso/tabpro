package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.harmony.ChordType;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.testsupport.Combos;
import java.awt.Component;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ChordDialogTest {

    private final Preferences scratch =
            Preferences.userRoot().node("tabpro-test/" + getClass().getSimpleName() + "/" + java.util.UUID.randomUUID());

    @AfterEach
    void limpiarElNodoDePrueba() throws BackingStoreException {
        scratch.removeNode();
    }

    @Test
    void ningunControlQuedaSinNombreNiTooltipAccesible() {
        Editor editor = new Editor(Score.blank());
        ChordEditorModel model = ChordEditorModel.forBeat(editor.currentBeat(), Tuning.standard());
        ChordLibrary library = new ChordLibrary(scratch);

        ChordDialog.Panel panel = new ChordDialog.Panel(model, library, editor, new RecordingPlayer());

        AccessibilityAssertions.assertNoViolations(panel);
    }

    @Test
    void elComboDeTipoDeAcordeMuestraElSufijoMusicalEnVezDelEnumCrudo() {
        Editor editor = new Editor(Score.blank());
        ChordEditorModel model = ChordEditorModel.forBeat(editor.currentBeat(), Tuning.standard());
        ChordLibrary library = new ChordLibrary(scratch);

        ChordDialog.Panel panel = new ChordDialog.Panel(model, library, editor, new RecordingPlayer());

        @SuppressWarnings("unchecked")
        JComboBox<ChordType> types = Combos.firstWithItemType(panel, ChordType.class);
        Component rendered = types.getRenderer()
                .getListCellRendererComponent(new JList<>(), ChordType.MINOR_SEVENTH, 0, false, false);

        assertEquals("m7", ((JLabel) rendered).getText());
    }
}
