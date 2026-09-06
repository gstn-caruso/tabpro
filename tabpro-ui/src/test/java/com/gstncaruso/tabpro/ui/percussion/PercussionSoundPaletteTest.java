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

    /**
     * El alto de fila se fija a mano a proposito. Sin fijarlo, JList lo deduce del alto que pide
     * el renderer, que depende de la fuente de la maquina: el test medi­a la celda con una fuente
     * y el clic se resolvia con otra, asi que el punto caia en una fila distinta de la que el
     * test creia. Fallaba en CI y pasaba aca. Este test habla del doble clic, no de tipografia.
     */
    private static final int CELL_HEIGHT = 20;

    /**
     * La propiedad que hacia falta y no estaba: la celda que el test mide tiene que ser la misma
     * que el JList usa para resolver donde cayo el clic. Si se separan, los otros dos tests
     * hablan de una fila y prueban otra, que es como este archivo fallaba en CI.
     */
    @Test
    void theCellTheTestMeasuresIsTheCellTheListResolvesTheClickInto() {
        PercussionSoundPalette palette = new PercussionSoundPalette(sound -> { }, sound -> { });
        JList<Integer> list = sized(palette);

        for (int index = 0; index < 3; index++) {
            Rectangle bounds = list.getCellBounds(index, index);
            Point center = new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);

            assertEquals(index, list.locationToIndex(center), "la fila " + index + " no se resuelve a si misma");
        }
    }

    private static JList<Integer> sized(PercussionSoundPalette palette) {
        palette.setSize(220, 400);
        JList<Integer> list = palette.soundList();
        list.setFixedCellHeight(CELL_HEIGHT);
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
