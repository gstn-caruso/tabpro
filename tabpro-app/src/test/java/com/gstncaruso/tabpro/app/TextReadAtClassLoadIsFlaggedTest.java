package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void aStaticFinalFieldMentionedOnlyInACommentIsNotFlagged(@TempDir Path root) throws IOException {
        write(root, "MentionsACacheInAComment.java", """
                class MentionsACacheInAComment {
                    // private static final String TITLE = Texts.get("area.title");
                    /* static final String[] COLUMNS = {Texts.get("area.column")}; */
                }
                """);

        assertTrue(ClassLoadTextScan.filesReadingTextAtClassLoad(root).isEmpty());
    }

    @Test
    void enumConstantsBuiltFromTextsAreFlaggedButConstantsThatKeepAKeyAreNot(@TempDir Path root) throws IOException {
        Path culprit = write(root, "CachedChoice.java", """
                enum CachedChoice {
                    ON(Texts.get("area.CachedChoice.on")),
                    OFF(Texts.get("area.CachedChoice.off"));

                    private final String label;

                    CachedChoice(String label) {
                        this.label = label;
                    }
                }
                """);
        write(root, "KeyedChoice.java", """
                enum KeyedChoice {
                    ON("area.KeyedChoice.on"),
                    OFF("area.KeyedChoice.off");

                    private final String labelKey;

                    KeyedChoice(String labelKey) {
                        this.labelKey = labelKey;
                    }

                    String label() {
                        return Texts.get(labelKey);
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
