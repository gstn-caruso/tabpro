package com.gstncaruso.tabpro.ui.percussion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JList;
import org.junit.jupiter.api.Test;

class PercussionSoundPaletteTest {

    @Test
    void ningunControlQuedaSinNombreNiTooltipAccesible() {
        AccessibilityAssertions.assertNoViolations(new PercussionSoundPalette(sound -> { }, sound -> { }));
    }

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

    @Test
    void laGrillaTieneCuatroColumnasEnElOrdenDeGP5() {
        PercussionSoundPalette palette = new PercussionSoundPalette(sound -> { }, sound -> { });
        JList<Integer> list = sized(palette);

        List<Integer> xPerIndex = new ArrayList<>();
        List<Integer> yPerIndex = new ArrayList<>();
        for (int index = 0; index < list.getModel().getSize(); index++) {
            Rectangle bounds = list.getCellBounds(index, index);
            xPerIndex.add(bounds.x);
            yPerIndex.add(bounds.y);
        }

        assertEquals(4, xPerIndex.stream().distinct().count(), "la grilla no tiene cuatro columnas");
        assertEquals(xPerIndex.get(0), xPerIndex.get(1), "el primer y segundo sonido van en la misma columna");
        assertTrue(yPerIndex.get(1) > yPerIndex.get(0), "el segundo sonido va debajo del primero, como en GP5");
    }

    @Test
    void elTamanoDeCeldaQuedaFijoDesdeLaConstruccionYNoSeMideEnCadaLayout() {
        PercussionSoundPalette palette = new PercussionSoundPalette(sound -> { }, sound -> { });
        JList<Integer> list = palette.soundList();

        assertTrue(list.getFixedCellHeight() > 0,
                "el alto de fila deberia quedar fijo desde la construccion, no medido en cada layout");
        assertTrue(list.getFixedCellWidth() > 0,
                "el ancho de columna deberia quedar fijo desde la construccion, no medido en cada layout");
    }

    private static final int CELL_HEIGHT = 20;

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
