package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.findMenuItem;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.newFrame;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.bars.DirectionSymbol;
import com.gstncaruso.tabpro.ui.MainFrame;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@Tag("integration")
@ResourceLock(AuditSupport.SWING_LOCK)
class ViewSwitchThenCursorMoveAuditTest {

    @Test
    void theFourViewsAndMovingTheCursorToTheLastBarNeitherHangNorThrow() throws Exception {
        Editor editor = editorWithDirections();
        MainFrame frame = newFrame(editor);
        try {
            clickMenu(frame, "Modo pergamino");
            clickMenu(frame, "Pantalla vertical");
            clickMenu(frame, "Pantalla horizontal");
            clickMenu(frame, "Modo página");

            assertDoesNotThrow(editor::moveToLastMeasure,
                    "moving the cursor right after switching views must not throw");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    private static void clickMenu(MainFrame frame, String label) throws Exception {
        JMenuItem item = findMenuItem(frame.getJMenuBar(), label);
        assertNotNull(item, "could not find '" + label + "' in the real menu");
        SwingUtilities.invokeAndWait(item::doClick);
    }

    private static Editor editorWithDirections() {
        List<Measure> measures = new ArrayList<>();
        for (int i = 0; i < 19; i++) {
            measures.add(Measure.empty(TimeSignature.fourFour(), Duration.quarter()));
        }
        Track guitar = Track.standardGuitar("Guitarra").withMeasures(measures)
                .mappingMeasure(2, measure -> measure.mappingAttributes(
                        attrs -> attrs.withSymbol(DirectionSymbol.SEGNO)))
                .mappingMeasure(10, measure -> measure.mappingAttributes(
                        attrs -> attrs.withSymbol(DirectionSymbol.CODA)));
        return new Editor(new Score("Directions", 120, List.of(guitar)));
    }
}
