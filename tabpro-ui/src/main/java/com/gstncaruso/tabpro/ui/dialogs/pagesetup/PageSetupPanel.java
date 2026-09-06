package com.gstncaruso.tabpro.ui.dialogs.pagesetup;

import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import com.gstncaruso.tabpro.ui.page.BannerLine;
import com.gstncaruso.tabpro.ui.page.Orientation;
import com.gstncaruso.tabpro.ui.page.PageBanner;
import com.gstncaruso.tabpro.ui.page.PageElement;
import com.gstncaruso.tabpro.ui.page.PageSetup;
import com.gstncaruso.tabpro.ui.page.PaperFormat;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/** La ventana de Configurar pagina [F8]: papel, orientacion, margenes, tamano, encabezado y pie. */
public final class PageSetupPanel extends FormPanel {

    private static final int LABEL_WIDTH = 130;

    private final JComboBox<PaperFormat> paperFormat = new JComboBox<>(PaperFormat.values());
    private final JComboBox<Orientation> orientation = new JComboBox<>(Orientation.values());
    private final JSpinner marginTop = millimeterSpinner();
    private final JSpinner marginBottom = millimeterSpinner();
    private final JSpinner marginLeft = millimeterSpinner();
    private final JSpinner marginRight = millimeterSpinner();
    private final JSpinner scorePercent = new JSpinner(
            new SpinnerNumberModel(100, PageSetup.MIN_SCORE_PERCENT, PageSetup.MAX_SCORE_PERCENT, 5));
    private final List<BannerRow> headerRows = new ArrayList<>();
    private final List<BannerRow> footerRows = new ArrayList<>();

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
        addSection("Encabezado");
        addBannerRows(PageBanner.header(), headerRows);
        addSection("Pie de pagina");
        addBannerRows(PageBanner.footer(), footerRows);
        addFullWidthRow(new JLabel(
                "<html>Campos disponibles: [%title] [%subtitle] [%artist] [%album] [%words]"
                        + " [%music] [%copyright] [%transcriber] [%page] [%pages]</html>"));

        apply(initial);
    }

    private void addBannerRows(PageBanner banner, List<BannerRow> rows) {
        for (BannerLine line : banner.lines()) {
            BannerRow row = new BannerRow(line.element());
            rows.add(row);
            addFullWidthRow(row);
        }
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
        headerRows.forEach(row -> row.apply(setup.header()));
        footerRows.forEach(row -> row.apply(setup.footer()));
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
                bannerOf(PageBanner.header(), headerRows),
                bannerOf(PageBanner.footer(), footerRows));
    }

    private static PageBanner bannerOf(PageBanner empty, List<BannerRow> rows) {
        PageBanner banner = empty;
        for (BannerRow row : rows) {
            banner = banner.with(row.element, row.shown.isSelected(), row.text.getText());
        }
        return banner;
    }

    /** El casillero de un elemento del encabezado o del pie, con el texto que le toca. */
    private static final class BannerRow extends JPanel {

        private final PageElement element;
        private final JCheckBox shown;
        private final JTextField text = new JTextField();

        private BannerRow(PageElement element) {
            super(new BorderLayout(8, 0));
            this.element = element;
            this.shown = new JCheckBox(element.label());
            setOpaque(false);
            shown.setOpaque(false);
            shown.setPreferredSize(new Dimension(LABEL_WIDTH, shown.getPreferredSize().height));
            add(shown, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
        }

        private void apply(PageBanner banner) {
            shown.setSelected(banner.shows(element));
            text.setText(banner.textOf(element));
        }
    }
}
