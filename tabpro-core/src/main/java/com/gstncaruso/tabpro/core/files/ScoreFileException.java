package com.gstncaruso.tabpro.core.files;

import java.nio.file.Path;
import java.util.List;

public class ScoreFileException extends RuntimeException {

    private final ScoreFileProblem problem;
    private final List<Object> arguments;

    public ScoreFileException(String message) {
        super(message);
        this.problem = null;
        this.arguments = List.of();
    }

    public ScoreFileException(String message, Throwable cause) {
        super(message, cause);
        this.problem = null;
        this.arguments = List.of();
    }

    private ScoreFileException(ScoreFileProblem problem, List<Object> arguments, String detail, Throwable cause) {
        super(detail, cause);
        this.problem = problem;
        this.arguments = arguments;
    }

    public static ScoreFileException cannotRead(Path path, Throwable cause) {
        return new ScoreFileException(ScoreFileProblem.CANNOT_READ, List.of(path), "could not read " + path, cause);
    }

    public static ScoreFileException cannotWrite(Path path, Throwable cause) {
        return new ScoreFileException(ScoreFileProblem.CANNOT_WRITE, List.of(path), "could not write " + path, cause);
    }

    public static ScoreFileException cannotExport(Path path, Throwable cause) {
        return new ScoreFileException(
                ScoreFileProblem.CANNOT_EXPORT, List.of(path), "could not export " + path, cause);
    }

    public ScoreFileProblem problem() {
        return problem;
    }

    public List<Object> arguments() {
        return arguments;
    }
}
