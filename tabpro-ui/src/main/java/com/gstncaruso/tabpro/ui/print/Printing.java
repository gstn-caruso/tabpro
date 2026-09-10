package com.gstncaruso.tabpro.ui.print;

import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

/**
 * Lo que {@link ScorePrinting} necesita de un {@link java.awt.print.PrinterJob} real: el diálogo
 * nativo de imprimir, el diálogo nativo de configurar página y disparar la impresión misma. La
 * costura existe para que un test pueda darle a {@link ScorePrinting} un {@link PrinterJob} falso
 * en vez de uno real.
 */
public interface Printing {

    void setJobName(String name);

    void setPrintable(Printable printable, PageFormat format);

    boolean printDialog();

    void print() throws PrinterException;

    PageFormat defaultPage();

    PageFormat pageDialog(PageFormat page);
}
