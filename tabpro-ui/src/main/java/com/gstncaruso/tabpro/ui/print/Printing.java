package com.gstncaruso.tabpro.ui.print;

import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

public interface Printing {

    void setJobName(String name);

    void setPrintable(Printable printable, PageFormat format);

    boolean printDialog();

    void print() throws PrinterException;

    PageFormat defaultPage();

    PageFormat pageDialog(PageFormat page);
}
