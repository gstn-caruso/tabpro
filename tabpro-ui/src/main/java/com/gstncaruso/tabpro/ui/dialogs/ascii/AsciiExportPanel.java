package com.gstncaruso.tabpro.ui.dialogs.ascii;

import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

/**
 * La ventana de export de ASCII del manual: la vista previa de la pista activa, cuantas
 * columnas entran en cada renglon, y los botones Imprimir y Exportar.
 */
public final class AsciiExportPanel extends JPanel {

    private static final int MIN_COLUMNS_PER_LINE = 10;
    private static final int MAX_COLUMNS_PER_LINE = 500;
    private static final int DEFAULT_COLUMNS_PER_LINE = 80;

    private final JSpinner columnsPerLine =
            new JSpinner(new SpinnerNumberModel(DEFAULT_COLUMNS_PER_LINE, MIN_COLUMNS_PER_LINE, MAX_COLUMNS_PER_LINE, 1));
    private final JTextArea preview = new JTextArea(20, 60);
    private final JButton printButton = DialogStyle.flatButton("Imprimir");
    private final JButton exportButton = DialogStyle.flatButton("Exportar…");

    public AsciiExportPanel() {
        super(new BorderLayout(0, DialogStyle.GAP_S));
        DialogStyle.padded(this);
        preview.setEditable(false);
        preview.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, DialogStyle.GAP_S, DialogStyle.GAP_S));
        toolbar.add(printButton);
        toolbar.add(exportButton);
        toolbar.add(new JLabel("Columnas por línea"));
        toolbar.add(columnsPerLine);

        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(preview), BorderLayout.CENTER);
    }

    public int columnsPerLine() {
        return (Integer) columnsPerLine.getValue();
    }

    public void setColumnsPerLine(int value) {
        columnsPerLine.setValue(value);
    }

    public void showPreview(String text) {
        preview.setText(text);
        preview.setCaretPosition(0);
    }

    public String previewText() {
        return preview.getText();
    }

    public JButton printButton() {
        return printButton;
    }

    public JButton exportButton() {
        return exportButton;
    }

    /** Recalcula la vista previa apenas cambia la cantidad de columnas por linea. */
    public void onColumnsChanged(Runnable listener) {
        columnsPerLine.addChangeListener(event -> listener.run());
    }
}
