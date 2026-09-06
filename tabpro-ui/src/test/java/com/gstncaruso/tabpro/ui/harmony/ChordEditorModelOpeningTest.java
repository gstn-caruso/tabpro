package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ChordEditorModelOpeningTest {

    @Test
    void unBeatVacioArrancaConLaSeleccionInicial() {
        ChordEditorModel model = ChordEditorModel.forBeat(Beat.rest(Duration.quarter()), Tuning.standard());

        assertEquals(PitchClass.of("C"), model.selection().root());
        assertFalse(model.isCustom());
    }

    @Test
    void unBeatConNotasYSinAcordeCargaEsasNotasEnElDiagramaPrincipal() {
        Beat beat = Beat.of(Duration.quarter(), new Note(6, 0), new Note(5, 2), new Note(4, 2));

        ChordEditorModel model = ChordEditorModel.forBeat(beat, Tuning.standard());

        assertEquals(0, model.current().fretOfString(6));
        assertEquals(2, model.current().fretOfString(5));
        assertEquals(2, model.current().fretOfString(4));
    }

    @Test
    void unBeatConNotasQueFormanUnAcordeConocidoNoQuedaEnModoPersonalizado() {
        // Mi menor abierto: 0-2-2-0-0-0
        Beat beat = Beat.of(
                Duration.quarter(),
                new Note(6, 0), new Note(5, 2), new Note(4, 2),
                new Note(3, 0), new Note(2, 0), new Note(1, 0));

        ChordEditorModel model = ChordEditorModel.forBeat(beat, Tuning.standard());

        assertFalse(model.isCustom());
        assertEquals("Em", model.current().name());
    }

    @Test
    void unBeatConNotasQueNoFormanNingunAcordeConocidoQuedaEnModoPersonalizado() {
        // Mi (cuerda 6 al aire) y Sib (cuerda 5, traste 1) estan a un tritono: ningun
        // tipo de acorde de la biblioteca los explica con solo dos notas sonando.
        Beat beat = Beat.of(Duration.quarter(), new Note(6, 0), new Note(5, 1));

        ChordEditorModel model = ChordEditorModel.forBeat(beat, Tuning.standard());

        assertTrue(model.isCustom());
        assertEquals("", model.current().name());
    }

    @Test
    void unBeatQueYaTieneAcordeLoCargaTalCual() {
        // frets: cuerda1=0, cuerda2=1, cuerda3=0, cuerda4=2, cuerda5=0, cuerda6=muda.
        ChordDiagram existente = ChordDiagram.named("Am7", List.of(0, 1, 0, 2, 0, -1));
        Beat beat = Beat.rest(Duration.quarter()).withEffects(BeatEffects.none().withChord(existente));

        ChordEditorModel model = ChordEditorModel.forBeat(beat, Tuning.standard());

        assertEquals(ChordDiagram.MUTED, model.current().fretOfString(6));
        assertEquals(0, model.current().fretOfString(5));
        assertEquals(2, model.current().fretOfString(4));
    }

    @Test
    void aceptarEscribeElAcordeEnElBeat() {
        Editor editor = new Editor(Score.blank());
        ChordEditorModel model = ChordEditorModel.forBeat(editor.currentBeat(), editor.currentTrack().tuning());
        model.selectRoot(PitchClass.of("G"));

        model.applyTo(editor);

        assertTrue(editor.currentBeat().effects().chord().isPresent());
        assertEquals("G", editor.currentBeat().effects().chord().orElseThrow().name());
    }

    @Test
    void siElBeatNoTeniaNotasAceptarLasEscribe() {
        Editor editor = new Editor(Score.blank());
        ChordEditorModel model = ChordEditorModel.forBeat(editor.currentBeat(), editor.currentTrack().tuning());
        model.selectType(com.gstncaruso.tabpro.core.harmony.ChordType.MINOR);

        model.applyTo(editor);

        assertFalse(editor.currentBeat().notes().isEmpty(), "un beat vacio recibe las notas del diagrama elegido");
    }

    @Test
    void siElBeatYaTeniaNotasAceptarNoLasToca() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(5);
        Note notaOriginal = editor.currentBeat().noteOn(editor.cursor().string()).orElseThrow();
        ChordEditorModel model = ChordEditorModel.forBeat(editor.currentBeat(), editor.currentTrack().tuning());

        model.applyTo(editor);

        assertEquals(Optional.of(notaOriginal), editor.currentBeat().noteOn(notaOriginal.string()));
    }

    @Test
    void aceptarDejaElCursorDondeEstaba() {
        Editor editor = new Editor(Score.blank());
        editor.moveTo(0, 0, 3);
        ChordEditorModel model = ChordEditorModel.forBeat(editor.currentBeat(), editor.currentTrack().tuning());

        model.applyTo(editor);

        assertEquals(3, editor.cursor().string());
    }
}
