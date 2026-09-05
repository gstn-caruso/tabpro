package com.gstncaruso.tabpro.format;

import java.util.List;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;

public record ScoreDto(int format, String title, int tempo, List<TrackDto> tracks) {

    public static final int CURRENT_FORMAT = 2;
    private static final int OLDEST_READABLE_FORMAT = 1;

    public static ScoreDto from(Score score) {
        List<TrackDto> tracks = score.tracks().stream().map(TrackDto::from).toList();
        return new ScoreDto(CURRENT_FORMAT, score.title(), score.tempo(), tracks);
    }

    public Score toScore() {
        if (format < OLDEST_READABLE_FORMAT || format > CURRENT_FORMAT) {
            throw new ScoreFileException("version de formato no soportada: " + format);
        }
        if (tracks == null) {
            throw new ScoreFileException("falta el campo tracks");
        }
        try {
            List<Track> domainTracks = tracks.stream().map(TrackDto::toTrack).toList();
            return new Score(title, tempo, domainTracks);
        } catch (IllegalArgumentException e) {
            throw new ScoreFileException("la partitura no cumple sus invariantes: " + e.getMessage(), e);
        }
    }
}
