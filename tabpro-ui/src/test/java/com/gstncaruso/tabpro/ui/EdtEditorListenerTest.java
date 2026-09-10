package com.gstncaruso.tabpro.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.EditorChange;
import com.gstncaruso.tabpro.core.editing.EditorListener;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class EdtEditorListenerTest {

    @Test
    void reenviaElTipoDeCambioAlDelegadoEnElEdt() throws Exception {
        EditorChange[] received = new EditorChange[1];
        EditorListener delegate = new EditorListener() {
            @Override
            public void editorChanged() {
            }

            @Override
            public void editorChanged(EditorChange change) {
                received[0] = change;
            }
        };

        SwingUtilities.invokeAndWait(() -> EdtEditorListener.onEdt(delegate).editorChanged(EditorChange.CURSOR));

        assertEquals(EditorChange.CURSOR, received[0]);
    }

    @Test
    void entregaSincronicamenteCuandoYaEstaEnElEdt() throws Exception {
        boolean[] delivered = {false};

        SwingUtilities.invokeAndWait(() -> EdtEditorListener.onEdt(() -> delivered[0] = true).editorChanged());

        assertTrue(delivered[0], "en el EDT, la entrega tiene que ser en el acto");
    }

    @Test
    void entregaEnElEdtCuandoLlegaDeOtroHilo() throws Exception {
        CountDownLatch delivered = new CountDownLatch(1);
        boolean[] wasOnEdt = {false};
        EdtEditorListener listener = EdtEditorListener.onEdt(() -> {
            wasOnEdt[0] = SwingUtilities.isEventDispatchThread();
            delivered.countDown();
        });

        Thread background = new Thread(listener::editorChanged);
        background.start();
        background.join();

        assertTrue(delivered.await(2, TimeUnit.SECONDS), "la notificacion tiene que llegar igual, en el EDT");
        assertTrue(wasOnEdt[0], "una notificacion de otro hilo tiene que entregarse en el EDT, no en el que llamo");
    }
}
