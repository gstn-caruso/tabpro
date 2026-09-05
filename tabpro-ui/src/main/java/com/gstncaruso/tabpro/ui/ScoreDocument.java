package com.gstncaruso.tabpro.ui;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.files.ScoreFiles;
import java.nio.file.Path;
import java.util.Optional;

public final class ScoreDocument {

    public static final String UNTITLED = "Sin título";

    private final Editor editor;
    private final ScoreFiles files;
    private Path path;

    public ScoreDocument(Editor editor, ScoreFiles files) {
        this.editor = editor;
        this.files = files;
    }

    public Optional<Path> path() {
        return Optional.ofNullable(path);
    }

    public String displayName() {
        return UNTITLED;
    }

    public boolean save() {
        if (path == null) {
            return false;
        }
        files.save(editor.score(), path);
        return true;
    }

    public void saveAs(Path path) {
        files.save(editor.score(), path);
        this.path = path;
    }
}
