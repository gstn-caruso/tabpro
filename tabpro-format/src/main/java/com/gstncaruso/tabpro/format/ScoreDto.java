package com.gstncaruso.tabpro.format;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.LyricLine;
import com.gstncaruso.tabpro.core.model.Lyrics;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.core.model.Track;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public record ScoreDto(
        int format,
        String title,
        String subtitle,
        String artist,
        String album,
        String lyricsAuthor,
        String musicAuthor,
        String copyright,
        String transcriber,
        String instructions,
        String notice,
        int tempo,
        List<TrackDto> tracks,
        LyricsDto lyrics) {

    public static final int CURRENT_FORMAT = 3;
    private static final int OLDEST_READABLE_FORMAT = 1;

    public static ScoreDto from(Score score) {
        ScoreInfo info = score.info();
        return new ScoreDto(
                CURRENT_FORMAT,
                info.title(),
                blankToNull(info.subtitle()),
                blankToNull(info.artist()),
                blankToNull(info.album()),
                blankToNull(info.lyricsAuthor()),
                blankToNull(info.musicAuthor()),
                blankToNull(info.copyright()),
                blankToNull(info.transcriber()),
                blankToNull(info.instructions()),
                blankToNull(info.notice()),
                score.tempo(),
                score.tracks().stream().map(TrackDto::from).toList(),
                score.lyrics().isEmpty() ? null : LyricsDto.from(score.lyrics()));
    }

    public Score toScore() {
        if (format < OLDEST_READABLE_FORMAT || format > CURRENT_FORMAT) {
            throw new ScoreFileException("version de formato no soportada: " + format);
        }
        if (tracks == null) {
            throw new ScoreFileException("falta el campo tracks");
        }
        try {
            List<Track> domainTracks = IntStream.range(0, tracks.size())
                    .mapToObj(index -> tracks.get(index).toTrack(index))
                    .toList();
            return new Score(toInfo(), tempo, domainTracks, toLyrics());
        } catch (IllegalArgumentException e) {
            throw new ScoreFileException("la partitura no cumple sus invariantes: " + e.getMessage(), e);
        }
    }

    private ScoreInfo toInfo() {
        return new ScoreInfo(
                title, subtitle, artist, album, lyricsAuthor, musicAuthor, copyright, transcriber, instructions, notice);
    }

    private Lyrics toLyrics() {
        return lyrics == null ? Lyrics.none() : lyrics.toLyrics();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    public record LyricsDto(int trackIndex, List<LineDto> lines) {

        public static LyricsDto from(Lyrics lyrics) {
            return new LyricsDto(lyrics.trackIndex(), lyrics.lines().stream().map(LineDto::from).toList());
        }

        public Lyrics toLyrics() {
            List<LyricLine> read = new ArrayList<>();
            for (int index = 0; index < LyricLine.MAX_LINES; index++) {
                read.add(lines != null && index < lines.size() ? lines.get(index).toLine() : LyricLine.empty());
            }
            return new Lyrics(trackIndex, read);
        }
    }

    public record LineDto(int startingMeasure, String text) {

        public static LineDto from(LyricLine line) {
            return new LineDto(line.startingMeasure(), line.text());
        }

        public LyricLine toLine() {
            return new LyricLine(Math.max(1, startingMeasure), text);
        }
    }
}
