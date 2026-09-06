package com.gstncaruso.tabpro.ui.dialogs.pagesetup;

import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import com.gstncaruso.tabpro.ui.page.Orientation;
import com.gstncaruso.tabpro.ui.page.PageSetup;
import com.gstncaruso.tabpro.ui.page.PaperFormat;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/** La ventana de Configurar pagina [F8]: papel, orientacion, margenes, tamano, encabezado y pie. */
public final class PageSetupPanel extends FormPanel {

    private final JComboBox<PaperFormat> paperFormat = new JComboBox<>(PaperFormat.values());
    private final JComboBox<Orientation> orientation = new JComboBox<>(Orientation.values());
    private final JSpinner marginTop = millimeterSpinner();
    private final JSpinner marginBottom = millimeterSpinner();
    private final JSpinner marginLeft = millimeterSpinner();
    private final JSpinner marginRight = millimeterSpinner();
    private final JSpinner scorePercent = new JSpinner(
            new SpinnerNumberModel(100, PageSetup.MIN_SCORE_PERCENT, PageSetup.MAX_SCORE_PERCENT, 5));
    private final JTextField header = new JTextField();
    private final JTextField footer = new JTextField();

    public PageSetupPanel(PageSetup initial) {
        paperFormat.setRenderer((list, value, index, isSelected, hasFocus) -> new JLabel(value == null ? "" : value.label()));
        orientation.setRenderer((list, value, index, isSelected, hasFocus) -> new JLabel(value == null ? "" : value.label()));

        addRow("Papel", paperFormat);
        addRow("Orientacion", orientation);
        addSection("Margenes (mm)");
        addRow("Superior", marginTop);
        addRow("Inferior", marginBottom);
        addRow("Izquierdo", marginLeft);
        addRow("Derecho", marginRight);
        addRow("Tamano de la partitura (%)", scorePercent);
        addSection("Encabezado y pie");
        addFullWidthRow(new JLabel(
                "<html>Campos disponibles: [%title] [%subtitle] [%artist] [%album]"
                        + " [%words] [%music] [%copyright] [%page] [%pages]</html>"));
        addRow("Encabezado", header);
        addRow("Pie", footer);

        apply(initial);
    }

    private static JSpinner millimeterSpinner() {
        return new JSpinner(new SpinnerNumberModel(20, 0, 100, 1));
    }

    public void apply(PageSetup setup) {
        paperFormat.setSelectedItem(setup.paperFormat());
        orientation.setSelectedItem(setup.orientation());
        marginTop.setValue(setup.marginTop());
        marginBottom.setValue(setup.marginBottom());
        marginLeft.setValue(setup.marginLeft());
        marginRight.setValue(setup.marginRight());
        scorePercent.setValue(setup.scorePercent());
        header.setText(setup.header());
        footer.setText(setup.footer());
    }

    public PageSetup toPageSetup() {
        return new PageSetup(
                (PaperFormat) paperFormat.getSelectedItem(),
                (Orientation) orientation.getSelectedItem(),
                (Integer) marginTop.getValue(),
                (Integer) marginBottom.getValue(),
                (Integer) marginLeft.getValue(),
                (Integer) marginRight.getValue(),
                (Integer) scorePercent.getValue(),
                header.getText(),
                footer.getText());
    }
}
