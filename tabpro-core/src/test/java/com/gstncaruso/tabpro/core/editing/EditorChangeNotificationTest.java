package com.gstncaruso.tabpro.core.editing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TestDefaultNames;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class EditorChangeNotificationTest {

    @Test
    void changingTheScoreNotifiesAContentChange() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
        List<EditorChange> received = new ArrayList<>();
        editor.addListener(new EditorListener() {
            @Override
            public void editorChanged() {
            }

            @Override
            public void editorChanged(EditorChange change) {
                received.add(change);
            }
        });

        editor.setFret(5);

        assertEquals(List.of(EditorChange.CONTENT), received);
    }

    @Test
    void movingTheCursorNotifiesACursorChange() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
        List<EditorChange> received = new ArrayList<>();
        editor.addListener(new EditorListener() {
            @Override
            public void editorChanged() {
            }

            @Override
            public void editorChanged(EditorChange change) {
                received.add(change);
            }
        });

        editor.moveDown();

        assertEquals(List.of(EditorChange.CURSOR), received);
    }

    @Test
    void startingASelectionNotifiesACursorChange() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
        List<EditorChange> received = new ArrayList<>();
        editor.addListener(new EditorListener() {
            @Override
            public void editorChanged() {
            }

            @Override
            public void editorChanged(EditorChange change) {
                received.add(change);
            }
        });

        editor.startSelection(false);

        assertEquals(List.of(EditorChange.CURSOR), received);
    }
}
