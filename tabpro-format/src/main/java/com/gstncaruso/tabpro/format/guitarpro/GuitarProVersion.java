package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    String label() {
        return "v" + generation + "." + String.format("%02d", minor);
    }

    /** GP4 and later carry the song's lyrics. */
    boolean hasLyrics() {
        return generation >= 4;
    }

    /** GP3 and GP4 declare a single global triplet feel; GP5 moves it to each measure. */
    boolean hasGlobalTripletFeel() {
        return generation < 5;
    }

    /** The key signature's octave appears since GP4. */
    boolean hasOctave() {
        return generation >= 4;
    }

    /** Since GP5 the header carries the tempo label, in addition to the numeric value. */
    boolean hasTempoLabel() {
        return generation >= 5;
    }

    /**
     * Only 5.10 writes whether the tempo shows in the score or not: once in the header
     * and once for each parameter change that touches the tempo, after its transition.
     */
    boolean hasHideTempo() {
        return this == GP5_10;
    }

    boolean hasRseMasterSettings() {
        return this == GP5_10;
    }

    /** GP5.10 adds the RSE effect name and category to every parameter change, after the wah. */
    boolean hasRseInstrumentEffect() {
        return this == GP5_10;
    }

    /** GP5 adds the list of directions (Coda, Segno, etc.) to the header. */
    boolean hasDirections() {
        return generation >= 5;
    }

    boolean hasPageSetup() {
        return generation >= 5;
    }

    /** The score header only separates the lyrics author from the music author since GP5. */
    boolean hasMusicAuthorField() {
        return generation >= 5;
    }

    /** GP5 writes two voices per measure and track; before that there is only one. */
    boolean hasSecondVoice() {
        return generation >= 5;
    }

    boolean hasTrackExtras() {
        return generation >= 5;
    }

    boolean hasTrackEffectExtras() {
        return this == GP5_10;
    }

    /** Beats and notes carry a second flags byte since GP4. */
    boolean hasSecondFlagsByte() {
        return generation >= 4;
    }

    /** GP3 writes the repeat count already reduced by one. */
    int repeatCountOffset() {
        return generation < 5 ? 1 : 0;
    }

    /** The order of the stroke speeds reverses from GP5 on. */
    boolean strokeUpFirst() {
        return generation >= 5;
    }

    /** Before GP4 the tapping/slapping/popping effect carried extra padding. */
    int slapEffectPaddingBytes() {
        return hasSecondFlagsByte() ? 0 : 4;
    }

    boolean hasGp5ChordFormat() {
        return generation >= 5;
    }

    /** GP5 stores the note's duration as a fraction; before that it was discrete. */
    boolean hasNoteDurationPercent() {
        return generation >= 5;
    }

    /** GP5 turns the slide from a number into a bitmask, so a note can carry several. */
    boolean hasSlideMask() {
        return generation >= 5;
    }

    boolean hasStructuredHarmonic() {
        return generation >= 5;
    }

    /** GP5 reverses the grace note's field order: transition first, then duration. */
    boolean hasGraceTransitionBeforeDuration() {
        return generation >= 5;
    }

    /** Whether the grace note is muted or falls on the beat is only written since GP5. */
    boolean hasGraceFlags() {
        return generation >= 5;
    }
}
