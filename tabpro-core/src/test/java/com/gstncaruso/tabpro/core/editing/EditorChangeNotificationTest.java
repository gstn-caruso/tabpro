package com.gstncaruso.tabpro.core.editing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Score;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class EditorChangeNotificationTest {

    @Test
    void cambiarLaPartituraNotificaUnCambioDeContenido() {
        Editor editor = new Editor(Score.blank());
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
}
