package com.gstncaruso.tabpro.ui.dialogs.markers;

import com.gstncaruso.tabpro.core.model.Score;
import java.util.List;
import javax.swing.table.AbstractTableModel;

final class MarkerTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {"Posición", "Nombre"};

    private List<MarkerList.Positioned> rows;

    MarkerTableModel(Score score) {
        rows = MarkerList.collect(score);
    }

    void refresh(Score score) {
        rows = MarkerList.collect(score);
        fireTableDataChanged();
    }

    MarkerList.Positioned rowAt(int row) {
        return rows.get(row);
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
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int row, int column) {
        MarkerList.Positioned positioned = rows.get(row);
        return column == 0 ? positioned.measureIndex() + 1 : positioned.marker().name();
    }
}
