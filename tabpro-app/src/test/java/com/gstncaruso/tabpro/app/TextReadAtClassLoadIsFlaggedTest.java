package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TextReadAtClassLoadIsFlaggedTest {

    @Test
    void aStaticFinalFieldInitializedFromTextsIsFlaggedButTextReadOnDemandIsNot(@TempDir Path root)
            throws IOException {
        Path culprit = write(root, "CachesItsTitle.java", """
                class CachesItsTitle {
                    private static final String TITLE = Texts.get("area.CachesItsTitle.title");
                }
                """);
        write(root, "ReadsItsTitleOnDemand.java", """
                class ReadsItsTitleOnDemand {
                    private static final String TITLE_KEY = "area.ReadsItsTitleOnDemand.title";
                    private final String label = Texts.get("area.ReadsItsTitleOnDemand.label");

                    static String title() {
                        return Texts.get(TITLE_KEY);
                    }
                }
                """);

        assertEquals(List.of(culprit), ClassLoadTextScan.filesReadingTextAtClassLoad(root));
    }

    private static Path write(Path root, String fileName, String content) throws IOException {
        Path file = root.resolve(fileName);
        Files.writeString(file, content);
        return file;
    }
}
