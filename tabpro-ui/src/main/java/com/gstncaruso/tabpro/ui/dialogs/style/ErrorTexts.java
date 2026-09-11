package com.gstncaruso.tabpro.ui.dialogs.style;

import com.gstncaruso.tabpro.core.files.ScoreFeature;
import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFileProblem;
import com.gstncaruso.tabpro.core.files.ScoreOperation;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.print.ImageExportException;

public final class ErrorTexts {

    private ErrorTexts() {
    }

    public static String of(ScoreFileException failure) {
        return render(failure, Texts::get);
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

    private static String render(ScoreFileException failure, TextSource texts) {
        Object[] arguments = failure.arguments().stream().map(argument -> localized(argument, texts)).toArray();
        String sentence = texts.text("window.error." + failure.problem().name(), arguments);
        return failure.problem() == ScoreFileProblem.DAMAGED ? sentence + "\n" + failure.getMessage() : sentence;
    }

    private static String render(ImageExportException failure, TextSource texts) {
        return texts.text("window.imageExportError." + failure.problem().name(), failure.arguments().toArray());
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
