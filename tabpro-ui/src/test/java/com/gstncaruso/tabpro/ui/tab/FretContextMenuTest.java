package com.gstncaruso.tabpro.ui.tab;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.PercussionKit;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.junit.jupiter.api.Test;

/**
 * El manual, en Using the Mouse: "Note > 0 to 30 (right-click menu on the tablature)". El rango
 * real no esta escrito en el menu: se lo pregunta a la pista, porque una de percusion no tiene
 * trastes sino sonidos MIDI.
 */
class FretContextMenuTest {

    @Test
    void aStringedTrackOffersEveryFretFromZeroToTheMaximum() {
        JPopupMenu menu = FretContextMenu.forTrack(Track.standardGuitar("Guitarra"), number -> { });

        assertEquals(Tuning.MAX_FRET + 1, menu.getComponentCount());
        assertEquals("0", itemAt(menu, 0).getText());
        assertEquals(String.valueOf(Tuning.MAX_FRET), itemAt(menu, menu.getComponentCount() - 1).getText());
    }

    @Test
    void choosingAFretCallsBackWithThatFretNumber() {
        List<Integer> chosen = new ArrayList<>();
        JPopupMenu menu = FretContextMenu.forTrack(Track.standardGuitar("Guitarra"), chosen::add);

        itemAt(menu, 5).doClick();

        assertEquals(List.of(5), chosen);
    }

    @Test
    void aPercussionTrackOffersItsPlayableSoundsInsteadOfFrets() {
        JPopupMenu menu = FretContextMenu.forTrack(Track.percussion("Bateria"), number -> { });

        assertEquals(PercussionKit.sounds().size(), menu.getComponentCount());
    }

    @Test
    void choosingAPercussionSoundCallsBackWithItsMidiNumber() {
        List<Integer> chosen = new ArrayList<>();
        JPopupMenu menu = FretContextMenu.forTrack(Track.percussion("Bateria"), chosen::add);

        itemAt(menu, 0).doClick();

        assertEquals(List.of(PercussionKit.sounds().get(0)), chosen);
    }

    @Test
    void aPercussionSoundIsLabeledWithItsName() {
        JPopupMenu menu = FretContextMenu.forTrack(Track.percussion("Bateria"), number -> { });

        int firstSound = PercussionKit.sounds().get(0);
        assertEquals(true, itemAt(menu, 0).getText().contains(PercussionKit.nameOf(firstSound).orElseThrow()));
    }

    private static JMenuItem itemAt(JPopupMenu menu, int index) {
        return (JMenuItem) menu.getComponent(index);
    }
}
