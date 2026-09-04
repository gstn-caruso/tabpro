package com.gstncaruso.tabpro.core.files;

import java.nio.file.Path;

import com.gstncaruso.tabpro.core.model.Score;

public interface ScoreFiles {

    Score load(Path path);

    void save(Score score, Path path);
}
