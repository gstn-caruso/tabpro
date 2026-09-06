package com.gstncaruso.tabpro.ui.dialogs.print;

import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import com.gstncaruso.tabpro.ui.print.PrintSettings;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * La ventana de Imprimir del manual: arriba que hojas salen -toda la partitura o un rango-, abajo
 * de que tamano salen, con la escala en porcentaje o ajustandola sola al papel de la impresora.
 */
public final class PrintPanel extends FormPanel {

    private final int sheetCount;
    private final JRadioButton everything = new JRadioButton("Toda la partitura", true);
    private final JRadioButton aRange = new JRadioButton("Paginas");
    private final JSpinner fromSheet;
    private final JSpinner toSheet;
    private final JSpinner scalePercent = new JSpinner(new SpinnerNumberModel(
            100, PrintSettings.MIN_SCALE_PERCENT, PrintSettings.MAX_SCALE_PERCENT, 5));
    private final JCheckBox fitToPage = new JCheckBox("Ajustar a la hoja");

    public PrintPanel(int sheetCount) {
        this.sheetCount = Math.max(1, sheetCount);
        fromSheet = sheetSpinner(1);
        toSheet = sheetSpinner(this.sheetCount);

        ButtonGroup whatToPrint = new ButtonGroup();
        whatToPrint.add(everything);
        whatToPrint.add(aRange);

        addSection("Imprimir");
        addFullWidthRow(everything);
        addFullWidthRow(aRange);
        addRow("Desde la pagina", fromSheet);
        addRow("Hasta la pagina", toSheet);
        addSection("Posicion");
        addRow("Escala (%)", scalePercent);
        addFullWidthRow(fitToPage);

        everything.addActionListener(event -> refreshWhatIsEnabled());
        aRange.addActionListener(event -> refreshWhatIsEnabled());
        fitToPage.addActionListener(event -> refreshWhatIsEnabled());
        refreshWhatIsEnabled();
    }

    private JSpinner sheetSpinner(int value) {
        return new JSpinner(new SpinnerNumberModel(value, 1, Math.max(1, sheetCount), 1));
    }

    /** El rango solo se edita si se pidio un rango, y la escala solo si no se ajusta sola. */
    private void refreshWhatIsEnabled() {
        fromSheet.setEnabled(aRange.isSelected());
        toSheet.setEnabled(aRange.isSelected());
        scalePercent.setEnabled(!fitToPage.isSelected());
    }

    public PrintSettings toPrintSettings() {
        if (everything.isSelected()) {
            return PrintSettings.of(
                    1, sheetCount, sheetCount, (Integer) scalePercent.getValue(), fitToPage.isSelected());
        }
        return PrintSettings.of(
                (Integer) fromSheet.getValue(), (Integer) toSheet.getValue(), sheetCount,
                (Integer) scalePercent.getValue(), fitToPage.isSelected());
    }

    /** Para poder armar la ventana ya pidiendo un rango, y para los tests. */
    public void printOnly(int fromSheet, int toSheet) {
        aRange.setSelected(true);
        this.fromSheet.setValue(Math.clamp(fromSheet, 1, sheetCount));
        this.toSheet.setValue(Math.clamp(toSheet, 1, sheetCount));
        refreshWhatIsEnabled();
    }

    public void scaleTo(int percent) {
        fitToPage.setSelected(false);
        scalePercent.setValue(Math.clamp(percent, PrintSettings.MIN_SCALE_PERCENT, PrintSettings.MAX_SCALE_PERCENT));
        refreshWhatIsEnabled();
    }

    public void fitToPage() {
        fitToPage.setSelected(true);
        refreshWhatIsEnabled();
    }

    boolean scaleIsEditable() {
        return scalePercent.isEnabled();
    }

    boolean rangeIsEditable() {
        return fromSheet.isEnabled();
    }
}
