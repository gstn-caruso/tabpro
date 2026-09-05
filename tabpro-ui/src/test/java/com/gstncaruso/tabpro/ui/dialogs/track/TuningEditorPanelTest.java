package com.gstncaruso.tabpro.ui.dialogs.track;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.TuningLibrary;
import com.gstncaruso.tabpro.ui.dialogs.RecordingPlayer;
import org.junit.jupiter.api.Test;

class TuningEditorPanelTest {

    private final RecordingPlayer player = new RecordingPlayer();

    @Test
    void startsWithTheGivenTuning() {
        TuningEditorPanel panel = new TuningEditorPanel(Tuning.standard(), 25, player);

        assertEquals(Tuning.standard(), panel.toTuning());
    }

    @Test
    void switchingFamilyOffersItsTunings() {
        TuningEditorPanel panel = new TuningEditorPanel(Tuning.standardBass(), 33, player);

        panel.switchToBasses();
        panel.selectFromLibrary("Bajo Drop D");

        assertEquals("Bajo Drop D", panel.toTuning().name());
    }

    @Test
    void pickingFromTheLibraryReplacesTheTuning() {
        TuningEditorPanel panel = new TuningEditorPanel(Tuning.standard(), 25, player);

        panel.selectFromLibrary("Drop D");

        assertEquals(TuningLibrary.guitars().stream().filter(t -> t.name().equals("Drop D")).findFirst().orElseThrow(),
                panel.toTuning());
    }

    @Test
    void aTuningOutsideTheCurrentFamilyIsRejected() {
        TuningEditorPanel panel = new TuningEditorPanel(Tuning.standard(), 25, player);

        assertThrows(IllegalArgumentException.class, () -> panel.selectFromLibrary("Bajo estandar"));
    }

    @Test
    void changingStringCountGrowsOrShrinksTheTuning() {
        TuningEditorPanel panel = new TuningEditorPanel(Tuning.standard(), 25, player);

        panel.setStringCount(7);

        assertEquals(7, panel.toTuning().stringCount());
        assertEquals(Tuning.standard().strings(), panel.toTuning().strings().subList(0, 6));
    }

    @Test
    void editingAStringMakesTheTuningCustom() {
        TuningEditorPanel panel = new TuningEditorPanel(Tuning.standard(), 25, player);

        panel.setStringPitch(6, new Pitch(38));

        assertEquals(new Pitch(38), panel.toTuning().pitchOfString(6));
    }

    @Test
    void listeningPlaysTheStringWithTheTracksInstrument() {
        TuningEditorPanel panel = new TuningEditorPanel(Tuning.standard(), 25, player);

        panel.listen(1);

        assertEquals(1, player.sounded().size());
        assertEquals(Tuning.standard().pitchOfString(1), player.sounded().getFirst().pitch());
        assertEquals(25, player.sounded().getFirst().program());
    }
}
