package com.gstncaruso.tabpro.ui;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.files.ScoreFiles;
import java.nio.file.Path;
import java.util.Optional;

public final class ScoreDocument {

    private final Editor editor;
    private final ScoreFiles files;

    public ScoreDocument(Editor editor, ScoreFiles files) {
        this.editor = editor;
        this.files = files;
    }

    public Optional<Path> path() {
        return Optional.empty();
    }
}
