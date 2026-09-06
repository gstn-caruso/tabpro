package com.gstncaruso.tabpro.ui.percussion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JList;
import org.junit.jupiter.api.Test;

class PercussionSoundPaletteTest {

    @Test
    void aSingleClickPlaysTheSound() {
        List<Integer> played = new ArrayList<>();
        List<Integer> added = new ArrayList<>();
        PercussionSoundPalette palette = new PercussionSoundPalette(played::add, added::add);
        JList<Integer> list = sized(palette);

        click(list, 0, 1);

        assertEquals(List.of(list.getModel().getElementAt(0)), played);
        assertEquals(List.of(), added);
    }

    @Test
    void aDoubleClickAddsTheSoundInstead() {
        List<Integer> played = new ArrayList<>();
        List<Integer> added = new ArrayList<>();
        PercussionSoundPalette palette = new PercussionSoundPalette(played::add, added::add);
        JList<Integer> list = sized(palette);

        click(list, 0, 2);

        assertEquals(List.of(list.getModel().getElementAt(0)), added);
    }

    private static JList<Integer> sized(PercussionSoundPalette palette) {
        palette.setSize(220, 400);
        JList<Integer> list = palette.soundList();
        list.setSize(220, 400);
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
}
