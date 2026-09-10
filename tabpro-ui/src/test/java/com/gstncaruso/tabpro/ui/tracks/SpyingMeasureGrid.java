package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.core.editing.Editor;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/** Registra las llamadas a revalidate()/repaint() para que los tests puedan verificar, sin
 * mostrar ninguna ventana, si un cambio dispara el camino completo o el incremental. */
final class SpyingMeasureGrid extends MeasureGrid {
    int revalidateCalls;
    boolean fullRepaintCalled;
    final List<Rectangle> repaintedAreas = new ArrayList<>();

    SpyingMeasureGrid(Editor editor) {
        super(editor);
    }

    @Override
    public void revalidate() {
        revalidateCalls++;
        super.revalidate();
    }

    @Override
    public void repaint() {
        fullRepaintCalled = true;
        super.repaint();
    }

    @Override
    public void repaint(Rectangle area) {
        repaintedAreas.add(area);
        super.repaint(area);
    }

    /** El propio constructor de JComponent dispara un repaint (setBackground); lo que importa
     * para estas pruebas es lo que pasa despues, con la grilla ya armada. */
    void forgetCallsMadeWhileBuilding() {
        revalidateCalls = 0;
        fullRepaintCalled = false;
        repaintedAreas.clear();
    }
}
