package com.gstncaruso.tabpro.ui.dialogs.style;

import com.gstncaruso.tabpro.core.files.ScoreFeature;
import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFileProblem;
import com.gstncaruso.tabpro.core.files.ScoreOperation;
import com.gstncaruso.tabpro.ui.i18n.Texts;

public final class ErrorTexts {

    private ErrorTexts() {
    }

    public static String of(ScoreFileException failure) {
        return render(failure, Texts::get);
    }

    static String of(ScoreFileException failure, Texts texts) {
        return render(failure, texts::text);
    }

    private static String render(ScoreFileException failure, TextSource texts) {
        Object[] arguments = failure.arguments().stream().map(argument -> localized(argument, texts)).toArray();
        String sentence = texts.text("window.error." + failure.problem().name(), arguments);
        return failure.problem() == ScoreFileProblem.DAMAGED ? sentence + "\n" + failure.getMessage() : sentence;
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
