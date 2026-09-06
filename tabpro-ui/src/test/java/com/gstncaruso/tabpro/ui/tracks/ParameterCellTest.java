package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import java.awt.Component;
import java.awt.event.MouseEvent;
import org.junit.jupiter.api.Test;

class ParameterCellTest {

    @Test
    void showsAKnobByDefault() {
        Editor editor = new Editor(Score.blank());
        MixTableModel model = new MixTableModel();
        ParameterCell cell = new ParameterCell(editor, model, MixParameter.VOLUME, 0);

        assertTrue(cell.isShowingKnob());
        assertFalse(cell.isShowingNumber());
    }

    @Test
    void switchesToTheNumberWhenTheColumnHeaderIsToggled() {
        Editor editor = new Editor(Score.blank());
        MixTableModel model = new MixTableModel();
        ParameterCell cell = new ParameterCell(editor, model, MixParameter.VOLUME, 0);

        model.toggleDisplayMode(MixParameter.VOLUME);
        cell.refresh();

        assertTrue(cell.isShowingNumber());
        assertFalse(cell.isShowingKnob());
    }

    @Test
    void refreshReadsTheCurrentValueFromTheTrack() {
        Editor editor = new Editor(Score.blank());
        editor.setPan(0, 20);
        MixTableModel model = new MixTableModel();
        ParameterCell cell = new ParameterCell(editor, model, MixParameter.PAN, 0);

        cell.refresh();

        assertEquals(20, cell.currentValue());
    }

    @Test
    void movingTheKnobPushesTheValueToTheEditor() {
        Editor editor = new Editor(Score.blank());
        MixTableModel model = new MixTableModel();
        ParameterCell cell = new ParameterCell(editor, model, MixParameter.REVERB, 0);
        Potentiometer knob = cell.knob();

        knob.dispatchEvent(pressAt(knob, 100));
        knob.dispatchEvent(dragTo(knob, 40));

        assertEquals(60, editor.currentTrack().channel().reverb());
    }

    @Test
    void typingInTheNumberFieldPushesTheValueToTheEditor() {
        Editor editor = new Editor(Score.blank());
        MixTableModel model = new MixTableModel();
        model.toggleDisplayMode(MixParameter.CHORUS);
        ParameterCell cell = new ParameterCell(editor, model, MixParameter.CHORUS, 0);

        cell.numberField().setValue(55);

        assertEquals(55, editor.currentTrack().channel().chorus());
    }

    private static MouseEvent pressAt(Component target, int y) {
        return new MouseEvent(target, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, 5, y, 1, false);
    }

    private static MouseEvent dragTo(Component target, int y) {
        return new MouseEvent(target, MouseEvent.MOUSE_DRAGGED, System.currentTimeMillis(), 0, 5, y, 1, false);
    }
}
