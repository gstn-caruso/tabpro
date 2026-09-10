package com.gstncaruso.tabpro.ui.percussion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Test;

class PercussionStaffPickerTest {

    private static final int WIDTH = 200;

    @Test
    void hasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(new PercussionStaffPicker());
    }
    private static final int HEIGHT = PercussionStaffPicker.PREFERRED_HEIGHT;

    @Test
    void ordersTheLinesFromTheBottomOfTheStaffUp() {
        PercussionStaffPicker picker = sized();

        assertTrue(picker.yOf(PercussionLine.KICK) > picker.yOf(PercussionLine.SNARE));
        assertTrue(picker.yOf(PercussionLine.SNARE) > picker.yOf(PercussionLine.HI_HAT));
        assertTrue(picker.yOf(PercussionLine.HI_HAT) > picker.yOf(PercussionLine.CRASH));
    }

    @Test
    void readsBackTheLineYouPointAt() {
        PercussionStaffPicker picker = sized();

        for (PercussionLine line : PercussionLine.values()) {
            assertEquals(Optional.of(line), picker.lineAt(WIDTH / 2, picker.yOf(line)), line.toString());
        }
    }

    @Test
    void hasNoLineOffTheStaff() {
        PercussionStaffPicker picker = sized();

        assertEquals(Optional.empty(), picker.lineAt(0, picker.yOf(PercussionLine.SNARE)));
        assertEquals(Optional.empty(), picker.lineAt(WIDTH / 2, -5));
    }

    @Test
    void usesTheAcousticSoundByDefault() {
        PercussionStaffPicker picker = sized();

        assertEquals(38, picker.soundOf(PercussionLine.SNARE));
    }

    @Test
    void switchesToTheElectricSoundWhenAsked() {
        PercussionStaffPicker picker = sized();

        picker.setPreferElectric(true);

        assertEquals(40, picker.soundOf(PercussionLine.SNARE));
    }

    @Test
    void aSingleClickPlaysTheLinesSound() {
        List<Integer> played = new ArrayList<>();
        List<PercussionLine> added = new ArrayList<>();
        PercussionStaffPicker picker = new PercussionStaffPicker(played::add, added::add);
        picker.setSize(WIDTH, HEIGHT);

        click(picker, picker.yOf(PercussionLine.SNARE), 1);

        assertEquals(List.of(38), played);
        assertEquals(List.of(), added);
    }

    @Test
    void aDoubleClickAddsTheLineInstead() {
        List<Integer> played = new ArrayList<>();
        List<PercussionLine> added = new ArrayList<>();
        PercussionStaffPicker picker = new PercussionStaffPicker(played::add, added::add);
        picker.setSize(WIDTH, HEIGHT);

        click(picker, picker.yOf(PercussionLine.SNARE), 2);

        assertEquals(List.of(PercussionLine.SNARE), added);
    }

    private static void click(PercussionStaffPicker picker, int y, int clickCount) {
        MouseEvent event = new MouseEvent(picker, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0,
                WIDTH / 2, y, clickCount, false);
        for (var listener : picker.getMouseListeners()) {
            listener.mouseClicked(event);
        }
    }

    private static PercussionStaffPicker sized() {
        PercussionStaffPicker picker = new PercussionStaffPicker();
        picker.setSize(WIDTH, HEIGHT);
        return picker;
    }

    @Test
    void theDownArrowKeyMovesTheCaretToTheNextLine() {
        PercussionStaffPicker picker = sized();

        pressShortcut(picker, KeyStroke.getKeyStroke("DOWN"));

        assertEquals(PercussionLine.HI_HAT, picker.caret());
    }

    private static void pressShortcut(JComponent component, KeyStroke keyStroke) {
        Object name = component.getInputMap(JComponent.WHEN_FOCUSED).get(keyStroke);
        component.getActionMap().get(name).actionPerformed(new ActionEvent(component, ActionEvent.ACTION_PERFORMED, ""));
    }

    @Test
    void theUpArrowKeyMovesTheCaretToThePreviousLine() {
        PercussionStaffPicker picker = sized();
        pressShortcut(picker, KeyStroke.getKeyStroke("DOWN"));
        pressShortcut(picker, KeyStroke.getKeyStroke("DOWN"));

        pressShortcut(picker, KeyStroke.getKeyStroke("UP"));

        assertEquals(PercussionLine.HI_HAT, picker.caret());
    }

    @Test
    void theEnterKeyPlaysTheSoundUnderTheCaretJustLikeASingleClick() {
        List<Integer> played = new ArrayList<>();
        List<PercussionLine> added = new ArrayList<>();
        PercussionStaffPicker picker = new PercussionStaffPicker(played::add, added::add);
        picker.setSize(WIDTH, HEIGHT);
        pressShortcut(picker, KeyStroke.getKeyStroke("DOWN"));

        pressShortcut(picker, KeyStroke.getKeyStroke("ENTER"));

        assertEquals(List.of(picker.soundOf(PercussionLine.HI_HAT)), played);
        assertEquals(List.of(), added);
    }

    @Test
    void theSpaceKeyAddsTheLineUnderTheCaretJustLikeADoubleClick() {
        List<Integer> played = new ArrayList<>();
        List<PercussionLine> added = new ArrayList<>();
        PercussionStaffPicker picker = new PercussionStaffPicker(played::add, added::add);
        picker.setSize(WIDTH, HEIGHT);
        pressShortcut(picker, KeyStroke.getKeyStroke("DOWN"));

        pressShortcut(picker, KeyStroke.getKeyStroke("SPACE"));

        assertEquals(List.of(PercussionLine.HI_HAT), added);
    }

    @Test
    void paintsAVisibleCaretRingWhenItGetsFocus() {
        PercussionStaffPicker picker = sized();
        BufferedImage withoutFocus = paint(picker);

        gainFocus(picker);
        BufferedImage withFocus = paint(picker);

        assertTrue(differsSomewhere(withoutFocus, withFocus), "el foco tiene que verse en el dibujo");
    }

    private static void gainFocus(PercussionStaffPicker picker) {
        for (var listener : picker.getFocusListeners()) {
            listener.focusGained(new FocusEvent(picker, FocusEvent.FOCUS_GAINED));
        }
    }

    private static BufferedImage paint(PercussionStaffPicker picker) {
        BufferedImage image = new BufferedImage(picker.getWidth(), picker.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        picker.paint(g);
        g.dispose();
        return image;
    }

    private static boolean differsSomewhere(BufferedImage a, BufferedImage b) {
        for (int x = 0; x < a.getWidth(); x++) {
            for (int y = 0; y < a.getHeight(); y++) {
                if (a.getRGB(x, y) != b.getRGB(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }
}
