package com.gstncaruso.tabpro.ui.dialogs.markers;

import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.List;
import javax.swing.table.AbstractTableModel;

final class MarkerTableModel extends AbstractTableModel {

    private final String[] columns =
            {Texts.get("edit_dialogs.MarkerTableModel.position"), Texts.get("edit_dialogs.MarkerPanel.name")};

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
        return columns[column];
    }

    @Override
    public Object getValueAt(int row, int column) {
        MarkerList.Positioned positioned = rows.get(row);
        return column == 0 ? positioned.measureIndex() + 1 : positioned.marker().name();
    }
}
