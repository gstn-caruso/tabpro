package com.gstncaruso.tabpro.ui.print;

import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

/** Un PrinterJob falso: registra lo que ScorePrinting le manda, sin abrir ninguna ventana real. */
final class RecordingPrinting implements Printing {

    private String jobName;
    private Printable printable;
    private PageFormat printableFormat;
    private boolean printDialogAccepted = true;
    private boolean printCalled;
    private final PageFormat defaultPage = new PageFormat();

    @Override
    public void setJobName(String name) {
        this.jobName = name;
    }

    @Override
    public void setPrintable(Printable printable, PageFormat format) {
        this.printable = printable;
        this.printableFormat = format;
    }

    @Override
    public boolean printDialog() {
        return printDialogAccepted;
    }

    @Override
    public void print() throws PrinterException {
        printCalled = true;
    }

    @Override
    public PageFormat defaultPage() {
        return defaultPage;
    }

    @Override
    public PageFormat pageDialog(PageFormat page) {
        return page;
    }

    void cancelPrintDialog() {
        this.printDialogAccepted = false;
    }

    String jobName() {
        return jobName;
    }

    Printable printable() {
        return printable;
    }

    PageFormat printableFormat() {
        return printableFormat;
    }

    boolean printCalled() {
        return printCalled;
    }
}
