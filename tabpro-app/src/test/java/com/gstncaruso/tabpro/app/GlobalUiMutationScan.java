package com.gstncaruso.tabpro.app;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

final class GlobalUiMutationScan {

    private static final Pattern INSTALLS_THE_THEME = Pattern.compile("Theme\\.install\\(");
    private static final Pattern CHANGES_THE_FONT_SIZE = Pattern.compile("\\.useFontSize\\(");
    private static final Pattern TOGGLES_HIGH_CONTRAST = Pattern.compile("\\.useHighContrast\\(");
    private static final Pattern SETS_UP_FLATLAF = Pattern.compile("Flat\\w*Laf\\.setup\\(");
    private static final Pattern UPDATES_THE_FLATLAF_UI = Pattern.compile("FlatLaf\\.updateUI\\(");
    private static final Pattern WRITES_TO_UI_MANAGER = Pattern.compile("UIManager\\.put\\(");
    private static final Pattern SWITCHES_THE_LOOK_AND_FEEL = Pattern.compile("UIManager\\.setLookAndFeel\\(");
    private static final Pattern INSTALLS_THE_INTERFACE_LANGUAGE = Pattern.compile("Texts\\.install\\(");
    private static final Pattern THEME_VARIABLE = Pattern.compile("\\bTheme\\s+(\\w+)\\s*[=;]");
    private static final Pattern ISOLATED = Pattern.compile("@Isolated\\b");
    private static final Pattern DECLARES_A_TEST = Pattern.compile("@(Test|ParameterizedTest)\\b");

    private GlobalUiMutationScan() {
    }

    static List<Path> unisolatedMutators(Path root) {
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(path -> path.toString().endsWith(".java"))
                    .filter(GlobalUiMutationScan::isATestClass)
                    .filter(GlobalUiMutationScan::mutatesGlobalUiState)
                    .filter(path -> !isIsolated(path))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static boolean isATestClass(Path file) {
        return DECLARES_A_TEST.matcher(JavaSources.withoutComments(JavaSources.read(file))).find();
    }

    private static boolean mutatesGlobalUiState(Path file) {
        String code = JavaSources.withoutComments(JavaSources.read(file));
        return INSTALLS_THE_THEME.matcher(code).find()
                || CHANGES_THE_FONT_SIZE.matcher(code).find()
                || TOGGLES_HIGH_CONTRAST.matcher(code).find()
                || SETS_UP_FLATLAF.matcher(code).find()
                || UPDATES_THE_FLATLAF_UI.matcher(code).find()
                || WRITES_TO_UI_MANAGER.matcher(code).find()
                || SWITCHES_THE_LOOK_AND_FEEL.matcher(code).find()
                || INSTALLS_THE_INTERFACE_LANGUAGE.matcher(code).find()
                || appliesARealTheme(code);
    }

    private static boolean appliesARealTheme(String code) {
        Matcher declarations = THEME_VARIABLE.matcher(code);
        while (declarations.find()) {
            String variable = declarations.group(1);
            if (Pattern.compile("\\b" + Pattern.quote(variable) + "\\.apply\\(").matcher(code).find()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isIsolated(Path file) {
        return ISOLATED.matcher(JavaSources.read(file)).find();
    }
}
