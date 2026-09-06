package com.gstncaruso.tabpro.format.powertab;

import com.gstncaruso.tabpro.core.files.ScoreFileException;

/**
 * Lee la cabecera del archivo: la marca y version de PowerTab, y los datos de
 * la cancion. Solo se soporta el formato de version 1.7 (el unico que
 * escribio PowerTab Editor 1.7, y el que entienden los archivos reales) y
 * solo canciones, no lecciones.
 */
final class PowerTabHeaderReader {

    /** Los cuatro bytes "ptab" en little endian. */
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
        reader.readUnsignedByte(); // el tipo de contenido (guitarra/bajo/percusion): no tiene lugar en el modelo.
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
        reader.readMfcString(); // transcriptor de la pista de bajo: no tiene lugar en el modelo.
        String copyright = reader.readMfcString();
        String lyrics = reader.readMfcString();
        String notes = reader.readMfcString();
        reader.readMfcString(); // notas de la pista de bajo: no tiene lugar en el modelo.
        return new PowerTabHeader(title, artist, composer, lyricist, arranger, transcriber, copyright, lyrics, notes);
    }

    /** El dato de lanzamiento no tiene destino en el modelo de tabpro; solo hay que dejarlo atras. */
    private void skipReleaseInfo(PowerTabByteReader reader) {
        int releaseType = reader.readUnsignedByte();
        if (releaseType == RELEASETYPE_PUBLIC_AUDIO) {
            reader.readUnsignedByte(); // tipo de lanzamiento (single, EP, album...).
            reader.readMfcString(); // titulo del lanzamiento.
            reader.readUnsignedShort(); // anio.
            reader.readUnsignedByte(); // en vivo.
        } else if (releaseType == RELEASETYPE_PUBLIC_VIDEO) {
            reader.readMfcString(); // titulo del video.
            reader.readUnsignedByte(); // en vivo.
        } else if (releaseType == RELEASETYPE_BOOTLEG) {
            reader.readMfcString(); // titulo del bootleg.
            reader.readUnsignedShort(); // mes.
            reader.readUnsignedShort(); // dia.
            reader.readUnsignedShort(); // anio.
        }
        // RELEASETYPE_NOTRELEASED no trae nada mas.
    }
}
