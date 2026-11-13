package com.gstncaruso.tabpro.ui.dialogs.style;

import com.gstncaruso.tabpro.core.files.ExportWarning;
import com.gstncaruso.tabpro.core.files.ScoreFeature;
import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreOperation;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.print.ImageExportException;
import java.awt.print.PrinterException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ErrorTexts {

    private static final Logger LOG = Logger.getLogger(ErrorTexts.class.getName());

    private ErrorTexts() {
    }

    public static String of(ScoreFileException failure) {
        LOG.log(Level.FINE, failure.getMessage(), failure);
        return render(failure, Texts::get);
    }

    public static String of(PrinterException failure) {
        LOG.log(Level.FINE, failure.getMessage(), failure);
        return Texts.get("window.error.printing");
    }

    static String of(PrinterException failure, Texts texts) {
        return texts.text("window.error.printing");
    }

    static String of(ScoreFileException failure, Texts texts) {
        return render(failure, texts::text);
    }

    public static String of(ImageExportException failure) {
        return render(failure, Texts::get);
    }

    static String of(ImageExportException failure, Texts texts) {
        return render(failure, texts::text);
    }

    public static String of(ExportWarning warning) {
        return render(warning, Texts::get);
    }

    static String of(ExportWarning warning, Texts texts) {
        return render(warning, texts::text);
    }

    private static String render(ScoreFileException failure, TextSource texts) {
        Object[] arguments = failure.arguments().stream().map(argument -> localized(argument, texts)).toArray();
        return texts.text("window.error." + failure.problem().name(), arguments);
    }

    private static String render(ImageExportException failure, TextSource texts) {
        return texts.text("window.imageExportError." + failure.problem().name(), failure.arguments().toArray());
    }

    private static String render(ExportWarning warning, TextSource texts) {
        return texts.text("window.exportWarning." + warning.loss().name(), warning.arguments().toArray());
    }

    private static Object localized(Object argument, TextSource texts) {
        return switch (argument) {
            case ScoreOperation operation -> texts.text("window.operation." + operation.name());
            case ScoreFeature feature -> texts.text("window.feature." + feature.name());
            default -> argument;
        };
    }

    private interface TextSource {
        String text(String key, Object... arguments);
    }
}
