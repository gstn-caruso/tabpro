package com.gstncaruso.tabpro.core.editing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EditorListenerTest {

    @Test
    void laVarianteConElTipoDeCambioLlamaALaViejaPorDefecto() {
        int[] calls = new int[1];
        EditorListener listener = () -> calls[0]++;

        listener.editorChanged(EditorChange.CONTENT);

        assertEquals(1, calls[0]);
    }
}
