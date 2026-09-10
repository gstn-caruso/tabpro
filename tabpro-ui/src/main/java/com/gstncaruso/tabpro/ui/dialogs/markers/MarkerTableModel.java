package com.gstncaruso.tabpro.ui.dialogs.markers;

import com.gstncaruso.tabpro.core.model.Score;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/** Las columnas Posicion/Nombre de la tabla de marcadores, en el orden en que caen los compases. */
final class MarkerTableModel extends AbstractTableModel {

    private List<MarkerList.Positioned> rows;

    MarkerTableModel(Score score) {
        rows = MarkerList.collect(score);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int row, int column) {
        return null;
    }
}
