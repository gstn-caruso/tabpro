package com.gstncaruso.tabpro.ui.print;

import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

/**
 * El {@link Printing} de producción: cada operación arranca su propio {@link PrinterJob} del
 * sistema, igual que hacía {@link ScorePrinting} antes de tener esta costura. Imprimir y
 * configurar la página son operaciones independientes -cada una con su propio job real-, así que
 * cada una guarda el suyo por separado.
 */
public final class SystemPrinting implements Printing {

    private PrinterJob printJob;
    private PrinterJob pageJob;

    @Override
    public void setJobName(String name) {
        printJob = PrinterJob.getPrinterJob();
        printJob.setJobName(name);
    }

    @Override
    public void setPrintable(Printable printable, PageFormat format) {
        printJob.setPrintable(printable, format);
    }

    @Override
    public boolean printDialog() {
        return printJob.printDialog();
    }

    @Override
    public void print() throws PrinterException {
        printJob.print();
    }

    @Override
    public PageFormat defaultPage() {
        pageJob = PrinterJob.getPrinterJob();
        return pageJob.defaultPage();
    }

    @Override
    public PageFormat pageDialog(PageFormat page) {
        return pageJob.pageDialog(page);
    }
}
