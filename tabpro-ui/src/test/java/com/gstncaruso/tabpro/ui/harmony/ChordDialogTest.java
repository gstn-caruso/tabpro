package com.gstncaruso.tabpro.ui.harmony;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
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
}
