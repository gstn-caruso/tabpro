package com.gstncaruso.tabpro.core.model;

public record ScoreInfo(
        String title,
        String subtitle,
        String artist,
        String album,
        String lyricsAuthor,
        String musicAuthor,
        String copyright,
        String transcriber,
        String instructions,
        String notice) {

    private static final ScoreInfo EMPTY = new ScoreInfo("", "", "", "", "", "", "", "", "", "");

    public ScoreInfo {
        title = orEmpty(title);
        subtitle = orEmpty(subtitle);
        artist = orEmpty(artist);
        album = orEmpty(album);
        lyricsAuthor = orEmpty(lyricsAuthor);
        musicAuthor = orEmpty(musicAuthor);
        copyright = orEmpty(copyright);
        transcriber = orEmpty(transcriber);
        instructions = orEmpty(instructions);
        notice = orEmpty(notice);
    }

    public static ScoreInfo empty() {
        return EMPTY;
    }

    public static ScoreInfo titled(String title) {
        return EMPTY.withTitle(title);
    }

    public ScoreInfo withTitle(String title) {
        return new ScoreInfo(title, subtitle, artist, album, lyricsAuthor, musicAuthor, copyright, transcriber, instructions, notice);
    }

    public ScoreInfo withSubtitle(String subtitle) {
        return new ScoreInfo(title, subtitle, artist, album, lyricsAuthor, musicAuthor, copyright, transcriber, instructions, notice);
    }

    public ScoreInfo withArtist(String artist) {
        return new ScoreInfo(title, subtitle, artist, album, lyricsAuthor, musicAuthor, copyright, transcriber, instructions, notice);
    }

    public ScoreInfo withAlbum(String album) {
        return new ScoreInfo(title, subtitle, artist, album, lyricsAuthor, musicAuthor, copyright, transcriber, instructions, notice);
    }

    public ScoreInfo withLyricsAuthor(String lyricsAuthor) {
        return new ScoreInfo(title, subtitle, artist, album, lyricsAuthor, musicAuthor, copyright, transcriber, instructions, notice);
    }

    public ScoreInfo withMusicAuthor(String musicAuthor) {
        return new ScoreInfo(title, subtitle, artist, album, lyricsAuthor, musicAuthor, copyright, transcriber, instructions, notice);
    }

    public ScoreInfo withCopyright(String copyright) {
        return new ScoreInfo(title, subtitle, artist, album, lyricsAuthor, musicAuthor, copyright, transcriber, instructions, notice);
    }

    public ScoreInfo withTranscriber(String transcriber) {
        return new ScoreInfo(title, subtitle, artist, album, lyricsAuthor, musicAuthor, copyright, transcriber, instructions, notice);
    }

    public ScoreInfo withInstructions(String instructions) {
        return new ScoreInfo(title, subtitle, artist, album, lyricsAuthor, musicAuthor, copyright, transcriber, instructions, notice);
    }

    public ScoreInfo withNotice(String notice) {
        return new ScoreInfo(title, subtitle, artist, album, lyricsAuthor, musicAuthor, copyright, transcriber, instructions, notice);
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
