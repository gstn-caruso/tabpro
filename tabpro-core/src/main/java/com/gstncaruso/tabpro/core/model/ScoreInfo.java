package com.gstncaruso.tabpro.core.model;

/** Los datos de la partitura que llenan el encabezado y el pie de la hoja. */
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

    /** Como se nombra la partitura cuando hace falta una sola linea. */
    public String heading() {
        if (title.isBlank()) {
            return artist.isBlank() ? "Sin titulo" : artist;
        }
        return artist.isBlank() ? title : title + " - " + artist;
    }

    /** Los dos autores tal como se escriben arriba a la derecha de la hoja. */
    public String credits() {
        if (musicAuthor.equals(lyricsAuthor)) {
            return musicAuthor.isBlank() ? "" : "Letra y musica: " + musicAuthor;
        }
        StringBuilder credits = new StringBuilder();
        if (!musicAuthor.isBlank()) {
            credits.append("Musica: ").append(musicAuthor);
        }
        if (!lyricsAuthor.isBlank()) {
            credits.append(credits.isEmpty() ? "" : "\n").append("Letra: ").append(lyricsAuthor);
        }
        return credits.toString();
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
