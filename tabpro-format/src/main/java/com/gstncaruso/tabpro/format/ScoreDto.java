package com.gstncaruso.tabpro.format;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.LyricLine;
import com.gstncaruso.tabpro.core.model.Lyrics;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.core.model.Track;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

    public static final int CURRENT_FORMAT = 5;
    private static final int OLDEST_READABLE_FORMAT = 1;

    /**
     * Desde esta version el canal configurado de una pista nueva sale del proximo par libre
     * (ver Editor.alignedToTheScore), no de un valor fijo: un archivo de una version anterior
     * pudo quedar con todas sus pistas en el mismo canal por defecto, porque hasta entonces
     * MidiSequences lo ignoraba y repartia los canales por su cuenta. Frenado en 5 a proposito
     * -no atado a CURRENT_FORMAT- para que un futuro bump de formato no vuelva a disparar esta
     * migracion sobre archivos que ya la pasaron.
     */
    private static final int FIRST_FORMAT_WITH_DISTINCT_CHANNELS = 5;

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
            if (format < FIRST_FORMAT_WITH_DISTINCT_CHANNELS && sharesOneChannelAmongItsTracks(domainTracks)) {
                domainTracks = withTheChannelsTheyUsedToSound(domainTracks);
            }
            return new Score(toInfo(), tempo, domainTracks, toLyrics());
        } catch (IllegalArgumentException e) {
            throw new ScoreFileException("la partitura no cumple sus invariantes: " + e.getMessage(), e);
        }
    }

    /**
     * La firma inconfundible de un canal que nunca sono: todas las pistas no percutivas de la
     * partitura -y hay mas de una- comparten el mismo numero. Nadie configura a mano varias
     * pistas en el mismo canal; si hay una sola pista no percutiva, o si los canales ya
     * difieren entre si (por ejemplo porque el archivo viene de importar un Guitar Pro real con
     * canales propios), no hay nada que reconstruir.
     */
    private static boolean sharesOneChannelAmongItsTracks(List<Track> tracks) {
        List<Integer> nonPercussionChannels = tracks.stream()
                .filter(track -> !track.isPercussion())
                .map(track -> track.channel().number())
                .toList();
        return nonPercussionChannels.size() >= 2 && Set.copyOf(nonPercussionChannels).size() == 1;
    }

    /**
     * El reparto automatico que hacia MidiSequences antes de que el canal configurado llegara a
     * sonar de verdad: 2n y 2n+1 por pista no percutiva, salteando siempre el canal de percusion.
     * No es una adivinanza -es el mismo calculo, reconstruido- asi que un archivo viejo vuelve a
     * sonar exactamente como sonaba, en los mismos canales de siempre. La percusion, que ya
     * suena forzada al canal 10 sin importar lo que diga su Channel, no participa del reparto.
     */
    private static List<Track> withTheChannelsTheyUsedToSound(List<Track> tracks) {
        List<Track> result = new ArrayList<>(tracks.size());
        int nonPercussionOrdinal = 0;
        for (Track track : tracks) {
            if (track.isPercussion()) {
                result.add(track);
                continue;
            }
            int clean = legacyAutomaticChannel(2 * nonPercussionOrdinal);
            int effects = legacyAutomaticChannel(2 * nonPercussionOrdinal + 1);
            nonPercussionOrdinal++;
            result.add(track.withChannel(track.channel().withNumber(clean).withEffectChannel(effects)));
        }
        return result;
    }

    /**
     * El canal (1-based) que le tocaba a la enesima ranura en el reparto que borro MidiSequences:
     * saltea siempre el canal de percusion, dando la vuelta al llegar al final del puerto.
     */
    private static int legacyAutomaticChannel(int slotNumber) {
        int usableChannels = Channel.CHANNELS_PER_PORT - 1;
        int slot = slotNumber % usableChannels;
        int zeroBased = slot < Channel.PERCUSSION_CHANNEL - 1 ? slot : slot + 1;
        return zeroBased + 1;
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
