package com.gstncaruso.tabpro.ui;

import com.gstncaruso.tabpro.ui.tracks.TrackPanel;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JSplitPane;

/**
 * La partitura y la mesa de mezcla comparten un JSplitPane vertical. Esta clase es la
 * responsable de esa disposicion: cual va arriba y cual abajo, cuanto ocupa la mesa cuando
 * esta visible, y el intercambio que pide el manual en Ver > Intercambiar vistas.
 */
public final class ScoreMixSplit {

    private final JComponent score;
    private final TrackPanel mixTable;
    private final JSplitPane split;
    private boolean swapped;

    public ScoreMixSplit(JComponent score, TrackPanel mixTable) {
        this.score = score;
        this.mixTable = mixTable;
        this.split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, score, mixTable);
        split.setResizeWeight(1);
        split.setBorder(BorderFactory.createEmptyBorder());
    }

    public JSplitPane component() {
        return split;
    }

    public boolean isSwapped() {
        return swapped;
    }

    /** El componente que hoy esta arriba del split. */
    public java.awt.Component top() {
        return split.getTopComponent();
    }

    /** El componente que hoy esta abajo del split. */
    public java.awt.Component bottom() {
        return split.getBottomComponent();
    }

    /**
     * Ver > Intercambiar vistas: la partitura y la mesa de mezcla cambian de lugar, sin que
     * la mesa deje de ser una franja chica.
     */
    public void toggleView() {
        int mixTableHeight = mixTable.getHeight();
        swapped = !swapped;
        // JSplitPane no reubica un componente que ya es hijo suyo en la otra posicion
        // si no se lo saca primero de la que tiene.
        split.setTopComponent(null);
        split.setBottomComponent(null);
        if (swapped) {
            split.setTopComponent(mixTable);
            split.setBottomComponent(score);
        } else {
            split.setTopComponent(score);
            split.setBottomComponent(mixTable);
        }
        applyMixTableHeight(mixTableHeight);
    }

    /** La mesa ocupa lo suyo; el resto es partitura. Se llama al abrir la ventana y al volver a mostrarla. */
    public void showMixTable() {
        applyMixTableHeight(mixTable.preferredPanelHeight());
    }

    /** La mesa desaparece del todo, sin cambiar el orden de arriba/abajo. */
    public void hideMixTable() {
        split.setDividerLocation(swapped ? 0 : split.getHeight());
    }

    private void applyMixTableHeight(int height) {
        int total = split.getHeight();
        int location = swapped ? height : Math.max(0, total - split.getDividerSize() - height);
        split.setDividerLocation(location);
    }
}
