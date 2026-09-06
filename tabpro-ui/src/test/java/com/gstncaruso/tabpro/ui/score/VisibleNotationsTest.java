package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Track;
import org.junit.jupiter.api.Test;

class VisibleNotationsTest {

    private final Track guitar = Track.standardGuitar("Guitarra");

    @Test
    void aScoreStartsShowingBothNotations() {
        VisibleNotations both = VisibleNotations.both();

        assertTrue(both.showsStandardNotationOf(guitar));
        assertTrue(both.showsTablatureOf(guitar));
    }

    @Test
    void hidingTheStaffHidesItOnEveryTrack() {
        VisibleNotations onlyTablature = VisibleNotations.both().withStandardNotation(false);

        assertFalse(onlyTablature.showsStandardNotationOf(guitar));
        assertTrue(onlyTablature.showsTablatureOf(guitar));
    }

    @Test
    void aTrackThatAsksForNoTablatureHasNoneEvenWhenTheScoreShowsIt() {
        Track staffOnly = guitar.withSettings(
                guitar.settings().withDisplay(guitar.settings().display().withTablature(false)));

        assertFalse(VisibleNotations.both().showsTablatureOf(staffOnly));
    }

    @Test
    void hidingOneNotationBringsTheOtherBackBecauseATrackNeedsOne() {
        VisibleNotations nothingLeft = VisibleNotations.both()
                .withTablature(false)
                .withStandardNotation(false);

        assertTrue(nothingLeft.showsTablatureOf(guitar));
        assertFalse(nothingLeft.showsStandardNotationOf(guitar));
    }
}
