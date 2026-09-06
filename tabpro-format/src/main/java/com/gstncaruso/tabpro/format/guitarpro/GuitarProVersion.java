package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Las versiones del formato binario de Guitar Pro que sabemos leer, con las
 * diferencias de layout entre ellas resueltas por polimorfismo en vez de
 * cascadas de {@code if} desparramadas por los lectores.
 */
enum GuitarProVersion {

    GP3(3, 0),
    GP4(4, 6),
    GP5_00(5, 0),
    GP5_10(5, 10);

    private static final Pattern HEADER_PATTERN =
            Pattern.compile("FICHIER GUITAR PRO (?:v|L)(\\d+)\\.(\\d+)");

    private final int generation;
    private final int minor;

    GuitarProVersion(int generation, int minor) {
        this.generation = generation;
        this.minor = minor;
    }

    /** Reconoce la cabecera de version y elige la variante que corresponde. */
    static GuitarProVersion parse(String header) {
        Matcher matcher = HEADER_PATTERN.matcher(header.strip());
        if (!matcher.matches()) {
            throw new ScoreFileException("no es un archivo Guitar Pro reconocido: \"" + header + "\"");
        }
        int major = Integer.parseInt(matcher.group(1));
        int minorVersion = Integer.parseInt(matcher.group(2));
        return switch (major) {
            case 3 -> GP3;
            case 4 -> GP4;
            case 5 -> minorVersion >= 10 ? GP5_10 : GP5_00;
            default -> throw new ScoreFileException(
                    "version de Guitar Pro no soportada: \"" + header + "\"");
        };
    }

    int generation() {
        return generation;
    }

    /** Titulo de referencia, solo para mensajes de error legibles. */
    String label() {
        return "v" + generation + "." + String.format("%02d", minor);
    }

    /** GP4 en adelante trae letra de la cancion. */
    boolean hasLyrics() {
        return generation >= 4;
    }

    /** GP3 y GP4 declaran un unico triplet feel global; GP5 lo mueve a cada compas. */
    boolean hasGlobalTripletFeel() {
        return generation < 5;
    }

    /** El octavado de la clave aparece desde GP4. */
    boolean hasOctave() {
        return generation >= 4;
    }

    /** Desde GP5 la cabecera trae el rotulo de tempo, ademas del valor numerico. */
    boolean hasTempoLabel() {
        return generation >= 5;
    }

    boolean hasHideTempo() {
        return this == GP5_10;
    }

    boolean hasRseMasterSettings() {
        return this == GP5_10;
    }

    /** GP5 agrega la lista de direcciones (Coda, Segno, etc.) en la cabecera. */
    boolean hasDirections() {
        return generation >= 5;
    }

    boolean hasPageSetup() {
        return generation >= 5;
    }

    /** El encabezado de la partitura separa letra de musica recien en GP5. */
    boolean hasMusicAuthorField() {
        return generation >= 5;
    }

    /** GP5 escribe dos voces por compas y pista; antes hay una sola. */
    boolean hasSecondVoice() {
        return generation >= 5;
    }

    boolean hasTrackExtras() {
        return generation >= 5;
    }

    boolean hasTrackEffectExtras() {
        return this == GP5_10;
    }

    /** Beats y notas traen un segundo byte de banderas desde GP4. */
    boolean hasSecondFlagsByte() {
        return generation >= 4;
    }

    /** GP3 escribe la cuenta de repeticion ya restada en uno. */
    int repeatCountOffset() {
        return generation < 5 ? 1 : 0;
    }

    /** El orden de las velocidades del rasgueo se invierte a partir de GP5. */
    boolean strokeUpFirst() {
        return generation >= 5;
    }

    /** Antes de GP4 el efecto de tapping/slapping/popping llevaba relleno de mas. */
    int slapEffectPaddingBytes() {
        return hasSecondFlagsByte() ? 0 : 4;
    }

    boolean hasGp5ChordFormat() {
        return generation >= 5;
    }

    /** GP5 guarda la duracion de la nota como fraccion; antes era discreta. */
    boolean hasNoteDurationPercent() {
        return generation >= 5;
    }

    boolean hasStructuredHarmonic() {
        return generation >= 5;
    }
}
