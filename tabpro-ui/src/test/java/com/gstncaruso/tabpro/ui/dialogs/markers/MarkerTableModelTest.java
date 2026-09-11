package com.gstncaruso.tabpro.ui.dialogs.markers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class MarkerTableModelTest {

    @Test
    void theColumnHeadersAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Position", english.text("edit_dialogs.MarkerTableModel.position"));
        assertEquals("Name", english.text("edit_dialogs.MarkerPanel.name"));
    }

    @Test
    void isEmptyForAFreshScore() {
        MarkerTableModel model = new MarkerTableModel(Score.blank());

        assertEquals(0, model.getRowCount());
    }

    @Test
    void showsThePositionAndNameOfOneMarker() {
        Editor editor = new Editor(Score.blank());
        editor.setMarker(Marker.named("Intro"));

        MarkerTableModel model = new MarkerTableModel(editor.score());

        assertEquals(1, model.getRowCount());
        assertEquals(1, model.getValueAt(0, 0));
        assertEquals("Intro", model.getValueAt(0, 1));
    }

    @Test
    void ordersSeveralMarkersByMeasure() {
        Editor editor = new Editor(Score.blank());
        editor.insertMeasure();
        editor.moveToFirstMeasure();
        editor.setMarker(Marker.named("Intro"));
        editor.moveToLastMeasure();
        editor.setMarker(Marker.named("Chorus"));

        MarkerTableModel model = new MarkerTableModel(editor.score());

        assertEquals(2, model.getRowCount());
        assertEquals("Intro", model.getValueAt(0, 1));
        assertEquals("Chorus", model.getValueAt(1, 1));
    }

    @Test
    void namesItsColumnsAsInTheManual() {
        MarkerTableModel model = new MarkerTableModel(Score.blank());

        assertEquals("Posición", model.getColumnName(0));
        assertEquals("Nombre", model.getColumnName(1));
    }
}
