package com.gstncaruso.tabpro.core.files;

public class ScoreFileException extends RuntimeException {

    public ScoreFileException(String message) {
        super(message);
    }

    public ScoreFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
