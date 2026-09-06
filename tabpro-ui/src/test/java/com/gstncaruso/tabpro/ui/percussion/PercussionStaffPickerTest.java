package com.gstncaruso.tabpro.ui.percussion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PercussionStaffPickerTest {

    private static final int WIDTH = 200;
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
}
