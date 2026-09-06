package com.gstncaruso.tabpro.ui;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.tracks.TrackPanel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

/**
 * La partitura y la mesa de mezcla comparten un JSplitPane. Ver > Intercambiar vistas los
 * cambia de lugar sin que la mesa deje de ser una franja chica.
 */
class ScoreMixSplitTest {

    private final JComponent score = new JPanel();
    private final TrackPanel mixTable = new TrackPanel(new Editor(Score.blank()));
    private final ScoreMixSplit split = new ScoreMixSplit(score, mixTable);

    @Test
    void laPartituraArrancaArribaYLaMesaAbajo() {
        assertSame(score, split.top());
        assertSame(mixTable, split.bottom());
    }

    @Test
    void intercambiarVistasPoneLaMesaArribaYLaPartituraAbajo() {
        split.toggleView();

        assertSame(mixTable, split.top());
        assertSame(score, split.bottom());
    }

    @Test
    void intercambiarDosVecesDejaTodoComoEstaba() {
        split.toggleView();
        split.toggleView();

        assertSame(score, split.top());
        assertSame(mixTable, split.bottom());
    }

    @Test
    void laMesaSigueOcupandoSuFranjaChicaDespuesDeIntercambiar() {
        layOutAt(900, 700);
        split.showMixTable();
        layOut();
        int mesaAntes = mixTable.getHeight();
        assertTrue(mesaAntes > 0 && mesaAntes < 700 / 2, "la mesa deberia ser una franja chica, midio " + mesaAntes);

        split.toggleView();
        layOut();

        int mesaDespues = mixTable.getHeight();
        assertTrue(Math.abs(mesaAntes - mesaDespues) <= 3,
                "la franja de la mesa deberia mantenerse, media " + mesaAntes + " y paso a " + mesaDespues);
        assertTrue(mesaDespues < 700 / 2, "la mesa no deberia ocupar la mitad de la ventana, midio " + mesaDespues);
    }

    private void layOutAt(int width, int height) {
        split.component().setSize(width, height);
    }

    private void layOut() {
        split.component().doLayout();
    }
}
