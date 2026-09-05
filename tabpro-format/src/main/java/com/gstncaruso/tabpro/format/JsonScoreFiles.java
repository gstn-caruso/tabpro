package com.gstncaruso.tabpro.format;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFiles;
import com.gstncaruso.tabpro.core.model.Score;

public final class JsonScoreFiles implements ScoreFiles {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public Score load(Path path) {
        try {
            String json = Files.readString(path);
            ScoreDto dto = gson.fromJson(json, ScoreDto.class);
            return dto.toScore();
        } catch (IOException e) {
            throw new ScoreFileException("no se pudo leer " + path, e);
        } catch (JsonParseException e) {
            throw new ScoreFileException("el archivo " + path + " no contiene JSON valido", e);
        } catch (NullPointerException e) {
            throw new ScoreFileException("el archivo " + path + " esta vacio", e);
        }
    }

    @Override
    public void save(Score score, Path path) {
        try {
            Files.writeString(path, gson.toJson(ScoreDto.from(score)));
        } catch (IOException e) {
            throw new ScoreFileException("no se pudo escribir " + path, e);
        }
    }
}
