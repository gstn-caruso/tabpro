package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import org.junit.jupiter.api.Test;

class ParameterCellTest {

    @Test
    void refreshReadsTheCurrentValueFromTheTrack() {
        Editor editor = new Editor(Score.blank());
        editor.setChorus(0, 20);
        ParameterCell cell = new ParameterCell(editor, MixParameter.CHORUS, 0);

        cell.refresh();

        assertEquals(20, cell.currentValue());
    }

    @Test
    void typingInTheNumberFieldPushesTheValueToTheEditor() {
        Editor editor = new Editor(Score.blank());
        ParameterCell cell = new ParameterCell(editor, MixParameter.CHORUS, 0);

        cell.numberField().setValue(55);

        assertEquals(55, editor.currentTrack().channel().chorus());
    }

    @Test
    void theNumberFieldsAccessibleNameIncludesTheParameterAndTheTrack() {
        Editor editor = new Editor(Score.blank());
        editor.addTrack(Track.standardBass("Bajo"));
        ParameterCell cell = new ParameterCell(editor, MixParameter.REVERB, 1);

        assertEquals("Reverb de Bajo", cell.numberField().getAccessibleContext().getAccessibleName());
    }
}
