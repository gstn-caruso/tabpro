package com.gstncaruso.tabpro.format.powertab;

import com.gstncaruso.tabpro.core.files.ScoreFileException;

/**
 * Reads the file header: the PowerTab marker and version, and the song data. Only the
 * version 1.7 format is supported (the only one PowerTab Editor 1.7 wrote, and the one
 * real files use) and only songs, not lessons.
 */
final class PowerTabHeaderReader {

    /** The four bytes "ptab", little endian. */
    private static final int MARKER = 0x62617470;

    private static final int VERSION_1_7 = 4;

    private static final int FILETYPE_SONG = 0;
    private static final int FILETYPE_LESSON = 1;

    private static final int RELEASETYPE_PUBLIC_AUDIO = 0;
    private static final int RELEASETYPE_PUBLIC_VIDEO = 1;
    private static final int RELEASETYPE_BOOTLEG = 2;

    private static final int AUTHORTYPE_KNOWN = 0;

    PowerTabHeader read(PowerTabByteReader reader) {
        int marker = reader.readInt();
        if (marker != MARKER) {
            throw new ScoreFileException("el archivo no es un archivo de PowerTab");
        }
        int version = reader.readUnsignedShort();
        if (version != VERSION_1_7) {
            throw new ScoreFileException(
                    "solo se soporta el formato de PowerTab version 1.7; este archivo trae la version "
                            + version);
        }
        int fileType = reader.readUnsignedByte();
        if (fileType == FILETYPE_LESSON) {
            throw new ScoreFileException("las lecciones de PowerTab todavia no estan soportadas, solo las canciones");
        }
        if (fileType != FILETYPE_SONG) {
            throw new ScoreFileException("tipo de archivo de PowerTab desconocido: " + fileType);
        }
        return readSong(reader);
    }

    private PowerTabHeader readSong(PowerTabByteReader reader) {
        reader.readUnsignedByte(); // content type (guitar/bass/drums): has no place in the model.
        String title = reader.readMfcString();
        String artist = reader.readMfcString();
        skipReleaseInfo(reader);
        String composer = "";
        String lyricist = "";
        if (reader.readUnsignedByte() == AUTHORTYPE_KNOWN) {
            composer = reader.readMfcString();
            lyricist = reader.readMfcString();
        }
        String arranger = reader.readMfcString();
        String transcriber = reader.readMfcString();
        reader.readMfcString(); // bass track transcriber: has no place in the model.
        String copyright = reader.readMfcString();
        String lyrics = reader.readMfcString();
        String notes = reader.readMfcString();
        reader.readMfcString(); // bass track notes: has no place in the model.
        return new PowerTabHeader(title, artist, composer, lyricist, arranger, transcriber, copyright, lyrics, notes);
    }

    /** The release data has no place in the tabpro model; it only needs to be skipped. */
    private void skipReleaseInfo(PowerTabByteReader reader) {
        int releaseType = reader.readUnsignedByte();
        if (releaseType == RELEASETYPE_PUBLIC_AUDIO) {
            reader.readUnsignedByte(); // release type (single, EP, album...).
            reader.readMfcString(); // release title.
            reader.readUnsignedShort(); // year.
            reader.readUnsignedByte(); // live.
        } else if (releaseType == RELEASETYPE_PUBLIC_VIDEO) {
            reader.readMfcString(); // video title.
            reader.readUnsignedByte(); // live.
        } else if (releaseType == RELEASETYPE_BOOTLEG) {
            reader.readMfcString(); // bootleg title.
            reader.readUnsignedShort(); // month.
            reader.readUnsignedShort(); // day.
            reader.readUnsignedShort(); // year.
        }
        // RELEASETYPE_NOTRELEASED carries nothing else.
    }
}
