package com.gstncaruso.tabpro.format;

import java.util.List;

import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;

public record ScoreDto(int format, String title, int tempo, List<TrackDto> tracks) {

    public static final int CURRENT_FORMAT = 1;

    public static ScoreDto from(Score score) {
        List<TrackDto> tracks = score.tracks().stream().map(TrackDto::from).toList();
        return new ScoreDto(CURRENT_FORMAT, score.title(), score.tempo(), tracks);
    }

    public Score toScore() {
        List<Track> domainTracks = tracks.stream().map(TrackDto::toTrack).toList();
        return new Score(title, tempo, domainTracks);
    }
}
