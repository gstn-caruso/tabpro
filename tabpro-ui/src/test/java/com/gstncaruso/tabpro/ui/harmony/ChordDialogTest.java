package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.harmony.Chord;
import com.gstncaruso.tabpro.core.harmony.ChordType;
import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.chords.ChordComplexity;
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

    @Test
    void lasPosicionesSonBotonesDeRadioSiempreVisiblesQueEligenLaComplejidad() {
        Editor editor = new Editor(Score.blank());
        ChordEditorModel model = ChordEditorModel.forBeat(editor.currentBeat(), Tuning.standard());
        ChordLibrary library = new ChordLibrary(scratch);

        ChordDialog.Panel panel = new ChordDialog.Panel(model, library, editor, new RecordingPlayer());

        javax.swing.JRadioButton simple = Combos.radioButtonWithText(panel, "Simple");
        assertNotNull(Combos.radioButtonWithText(panel, "Media"), "no encontre el radio 'Media'");
        assertNotNull(Combos.radioButtonWithText(panel, "Todas"), "no encontre el radio 'Todas'");
        assertNotNull(simple, "no encontre el radio 'Simple'");

        simple.doClick();

        assertEquals(ChordComplexity.SIMPLE, model.selection().complexity());
    }

    @Test
    void laCejillaSeEligeConBotonesDeRadioSiempreVisibles() {
        Editor editor = new Editor(Score.blank());
        ChordEditorModel model = ChordEditorModel.forBeat(editor.currentBeat(), Tuning.standard());
        ChordLibrary library = new ChordLibrary(scratch);

        ChordDialog.Panel panel = new ChordDialog.Panel(model, library, editor, new RecordingPlayer());

        javax.swing.JRadioButton forzar = Combos.radioButtonWithText(panel, "Forzar cejilla");
        assertNotNull(Combos.radioButtonWithText(panel, "Cualquiera"), "no encontre el radio 'Cualquiera'");
        assertNotNull(Combos.radioButtonWithText(panel, "Prohibir cejilla"), "no encontre el radio 'Prohibir cejilla'");
        assertNotNull(forzar, "no encontre el radio 'Forzar cejilla'");

        forzar.doClick();

        assertEquals(BarrePreference.FORCE, model.barrePreference());
    }

    @Test
    void elComboDeFundamentalMuestraElNombreDeLaNotaEnCastellano() {
        Editor editor = new Editor(Score.blank());
        ChordEditorModel model = ChordEditorModel.forBeat(editor.currentBeat(), Tuning.standard());
        ChordLibrary library = new ChordLibrary(scratch);

        ChordDialog.Panel panel = new ChordDialog.Panel(model, library, editor, new RecordingPlayer());

        @SuppressWarnings("unchecked")
        JComboBox<PitchClass> roots = Combos.firstWithItemType(panel, PitchClass.class);
        Component rendered = roots.getRenderer()
                .getListCellRendererComponent(new JList<>(), PitchClass.of("C"), 0, false, false);

        assertEquals("C (Do)", ((JLabel) rendered).getText());
    }

    @Test
    void elComboDeInversionMuestraFundamentalParaLaRaizYElGradoConLaNotaParaLasDemas() {
        Editor editor = new Editor(Score.blank());
        ChordEditorModel model = ChordEditorModel.forBeat(editor.currentBeat(), Tuning.standard());
        ChordLibrary library = new ChordLibrary(scratch);

        ChordDialog.Panel panel = new ChordDialog.Panel(model, library, editor, new RecordingPlayer());

        @SuppressWarnings("unchecked")
        JComboBox<com.gstncaruso.tabpro.core.harmony.Interval> inversions =
                Combos.firstWithItemType(panel, com.gstncaruso.tabpro.core.harmony.Interval.class);

        String fundamental = ((JLabel) inversions.getRenderer().getListCellRendererComponent(
                new JList<>(), com.gstncaruso.tabpro.core.harmony.Interval.ROOT, 0, false, false)).getText();
        String tercera = ((JLabel) inversions.getRenderer().getListCellRendererComponent(
                new JList<>(), com.gstncaruso.tabpro.core.harmony.Interval.MAJOR_THIRD, 0, false, false)).getText();

        assertEquals("Fundamental", fundamental);
        assertEquals("E (3)", tercera);
    }

    @Test
    void laListaDeNombresAlternativosMuestraElNombreDelAcordeEnVezDelRecordCrudo() {
        Editor editor = new Editor(Score.blank());
        ChordEditorModel model = ChordEditorModel.forBeat(editor.currentBeat(), Tuning.standard());
        ChordLibrary library = new ChordLibrary(scratch);

        ChordDialog.Panel panel = new ChordDialog.Panel(model, library, editor, new RecordingPlayer());

        JList<?> alternativeNames = Combos.firstListNamed(panel, "Nombres alternativos");
        Chord chord = Chord.of(PitchClass.of("C"), ChordType.MINOR_SEVENTH);
        String texto = Combos.renderedTextOfList(alternativeNames, chord);

        assertEquals("Cm7", texto);
    }
}
