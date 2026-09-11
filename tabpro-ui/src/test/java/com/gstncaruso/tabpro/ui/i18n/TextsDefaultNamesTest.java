package com.gstncaruso.tabpro.ui.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.DefaultNames;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class TextsDefaultNamesTest {

    private final DefaultNames spanish = new TextsDefaultNames();
    private final DefaultNames english = new TextsDefaultNames(Texts.forLocale(Locale.ENGLISH)::text);

    @Test
    void namesAGuitarTrackInSpanishByDefault() {
        assertEquals("Guitarra", spanish.guitarTrack());
    }

    @Test
    void namesAGuitarTrackInEnglish() {
        assertEquals("Guitar", english.guitarTrack());
    }

    @Test
    void namesAPercussionTrackInBothLanguages() {
        assertEquals("Batería", spanish.percussionTrack());
        assertEquals("Percussion", english.percussionTrack());
    }

    @Test
    void namesAChordInBothLanguages() {
        assertEquals("Acorde", spanish.chord());
        assertEquals("Chord", english.chord());
    }

    @Test
    void namesAMarkerInBothLanguages() {
        assertEquals("Marcador", spanish.marker());
        assertEquals("Marker", english.marker());
    }

    @Test
    void numbersAnUnnamedTrackInBothLanguages() {
        assertEquals("Pista 3", spanish.track(3));
        assertEquals("Track 3", english.track(3));
    }

    @Test
    void namesAnUnnumberedTrackInBothLanguages() {
        assertEquals("Pista", spanish.unnamedTrack());
        assertEquals("Track", english.unnamedTrack());
    }
}
