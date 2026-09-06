package com.gstncaruso.tabpro.ui.percussion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.JList;
import org.junit.jupiter.api.Test;

class PercussionAssistantTest {

    @Test
    void appliesOnlyToAPercussionTrack() {
        assertTrue(PercussionAssistant.appliesTo(Track.percussion("Bateria")));
        assertFalse(PercussionAssistant.appliesTo(Track.standardGuitar("Guitarra")));
    }

    @Test
    void followsTheActiveTrackOfTheEditor() {
        Editor editor = new Editor(new Score("t", 120, java.util.List.of(Track.percussion("Bateria"))));
        PercussionAssistant assistant = new PercussionAssistant(editor, new RecordingPlayer());

        assertTrue(assistant.appliesToCurrentTrack());

        editor.addTrack(Track.standardGuitar("Guitarra"));
        editor.selectTrack(1);

        assertFalse(assistant.appliesToCurrentTrack());
    }

    @Test
    void clickingASoundOnceOnlyPlaysIt() {
        Editor editor = new Editor(new Score("t", 120, java.util.List.of(Track.percussion("Bateria"))));
        RecordingPlayer player = new RecordingPlayer();
        PercussionAssistant assistant = new PercussionAssistant(editor, player);
        JList<Integer> list = sizedList(assistant);

        click(list, indexOf(list, 38), 1);

        assertEquals(java.util.List.of(new RecordingPlayer.Sounded(new Pitch(38), Track.PERCUSSION_PROGRAM)),
                player.sounded());
        assertTrue(editor.currentBeat().isRest(), "un clic simple no escribe nada");
    }

    @Test
    void doubleClickingASoundAddsItOnTheCursorsLine() {
        Editor editor = new Editor(new Score("t", 120, java.util.List.of(Track.percussion("Bateria"))));
        editor.moveTo(0, 0, 3);
        PercussionAssistant assistant = new PercussionAssistant(editor, new RecordingPlayer());
        JList<Integer> list = sizedList(assistant);

        click(list, indexOf(list, 49), 2);

        assertEquals(java.util.Optional.of(new Note(3, 49)), editor.currentBeat().noteOn(3));
    }

    @Test
    void thePickedElectricSoundIsUsedWhenTheCheckboxIsOn() {
        Editor editor = new Editor(new Score("t", 120, java.util.List.of(Track.percussion("Bateria"))));
        PercussionAssistant assistant = new PercussionAssistant(editor, new RecordingPlayer());
        assistant.electricCheckbox().setSelected(true);
        for (var listener : assistant.electricCheckbox().getActionListeners()) {
            listener.actionPerformed(null);
        }
        PercussionStaffPicker staff = assistant.staffPicker();
        staff.setSize(200, PercussionStaffPicker.PREFERRED_HEIGHT);

        clickStaff(staff, staff.yOf(PercussionLine.SNARE), 2);

        assertEquals(java.util.Optional.of(new Note(5, 40)), editor.currentBeat().noteOn(5));
    }

    private static int indexOf(JList<Integer> list, int sound) {
        for (int i = 0; i < list.getModel().getSize(); i++) {
            if (list.getModel().getElementAt(i) == sound) {
                return i;
            }
        }
        throw new IllegalArgumentException("sonido no encontrado: " + sound);
    }

    private static JList<Integer> sizedList(PercussionAssistant assistant) {
        PercussionSoundPalette palette = assistant.soundPalette();
        palette.setSize(220, 800);
        JList<Integer> list = palette.soundList();
        list.setSize(220, 800);
        list.doLayout();
        return list;
    }

    private static void click(JList<Integer> list, int index, int clickCount) {
        Rectangle bounds = list.getCellBounds(index, index);
        Point center = new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
        MouseEvent event = new MouseEvent(list, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0,
                center.x, center.y, clickCount, false);
        for (var listener : list.getMouseListeners()) {
            listener.mouseClicked(event);
        }
    }

    private static void clickStaff(PercussionStaffPicker staff, int y, int clickCount) {
        MouseEvent event = new MouseEvent(staff, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0,
                100, y, clickCount, false);
        for (var listener : staff.getMouseListeners()) {
            listener.mouseClicked(event);
        }
    }
}
