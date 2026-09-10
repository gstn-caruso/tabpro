package com.gstncaruso.tabpro.core.editing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.notation.Clef;
import com.gstncaruso.tabpro.core.notation.StaffPosition;
import java.util.List;
import org.junit.jupiter.api.Test;

class EditorNotationTest {

    @Test
    void enterAddsInStandardNotationButAdvancesInTablature() {
        Editor tabEditor = new Editor(Score.blank());
        tabEditor.enter();
        assertEquals(1, tabEditor.cursor().beat(), "in tablature, Enter has to advance to the next note");
        assertTrue(tabEditor.score().track(0).measure(0).beat(0).isRest(), "in tablature, Enter does not add anything");

        Editor staffEditor = new Editor(Score.blank());
        staffEditor.toggleNotation();
        staffEditor.enter();
        assertEquals(0, staffEditor.cursor().beat(), "in standard notation, Enter does not advance: it adds the note right there");
        assertFalse(staffEditor.currentBeat().isRest(), "in standard notation, Enter has to add a note");
    }

    @Test
    void enterInStandardNotationAddsTheNoteAtTheStringsOpenPitchWhenTheBeatIsSilent() {
        Editor editor = new Editor(Score.blank());
        editor.moveTo(0, 0, 3);
        editor.toggleNotation();

        editor.enter();

        Note added = editor.currentBeat().noteOn(3).orElseThrow();
        assertEquals(3, added.string());
        assertEquals(0, added.fret());
        assertEquals(editor.currentTrack().tuning().pitchOfString(3), editor.currentTrack().tuning().pitchOf(added));
    }

    @Test
    void enterDoesNothingInStandardNotationWhenNoStringCanReachTheCursorsPitch() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(50);
        editor.toggleNotation();
        boolean couldUndoBefore = editor.canUndo();

        editor.enter();

        Note note = editor.currentBeat().noteOn(1).orElseThrow();
        assertEquals(50, note.fret(), "with no string that can reach that pitch, Enter does not touch the note that was already there");
        assertEquals(couldUndoBefore, editor.canUndo(), "with no real change, the undo history should not move");
    }

    @Test
    void enterInStandardNotationPreservesTheExactPitchOfTheNoteAlreadyOnTheCursorsString() {
        Beat beat = Beat.of(Duration.quarter(), new Note(2, 7));
        Editor editor = editorWithFirstBeat(beat);
        editor.moveTo(0, 0, 2);
        editor.toggleNotation();
        Pitch expected = editor.currentTrack().tuning().pitchOf(new Note(2, 7));

        editor.enter();

        Note result = editor.currentBeat().noteOn(2).orElseThrow();
        assertEquals(expected, editor.currentTrack().tuning().pitchOf(result),
                "the pitch has to be the one that was there, not a semitone more or less");
    }

    @Test
    void arrowsMoveByStringInTablatureAndByStaffDegreeInStandardNotation() {
        Editor tabEditor = new Editor(Score.blank());
        tabEditor.moveTo(0, 0, 3);
        tabEditor.moveUp();
        assertEquals(2, tabEditor.cursor().string(), "in tablature, up has to go to the previous string");

        Editor staffEditor = new Editor(Score.blank());
        staffEditor.moveTo(0, 0, 3);
        staffEditor.toggleNotation();
        staffEditor.moveUp();
        assertEquals(3, staffEditor.cursor().string(),
                "in standard notation, up has to move by staff degree -from open G (string 3) to the A on"
                        + " fret 2 of the same string, the closest natural note- not jump strings"
                        + " like in tablature");
    }

    @Test
    void arrowsStayPutInStandardNotationWhenNoStringCanReachTheNextDegree() {
        Editor editor = new Editor(Score.blank());
        editor.moveTo(0, 0, 6);
        editor.toggleNotation();

        editor.moveDown();

        assertEquals(6, editor.cursor().string(), "with no string that reaches the pitch below, the cursor stays put");
    }

    @Test
    void enteringAfterMovingUpThreeStaffDegreesAddsTheNoteExactlyThreeDegreesAbove() {
        Editor editor = new Editor(Score.blank());
        editor.moveTo(0, 0, 6);
        Tuning tuning = editor.currentTrack().tuning();
        Clef clef = Clef.forTuning(tuning);
        Pitch start = tuning.pitchOfString(6);
        Pitch expected = clef.pitchAtStep(StaffPosition.of(start, clef).step() + 3).orElseThrow();
        editor.toggleNotation();

        editor.moveUp();
        editor.moveUp();
        editor.moveUp();
        editor.enter();

        assertEquals(1, editor.currentBeat().notes().size(), "it has to have added a single new note");
        Note added = editor.currentBeat().notes().getFirst();
        assertEquals(expected, tuning.pitchOf(added),
                "three arrows up have to land on the pitch three degrees above the starting point");
    }

    @Test
    void enteringAfterMovingDownThreeStaffDegreesAddsTheNoteExactlyThreeDegreesBelow() {
        Editor editor = new Editor(Score.blank());
        editor.moveTo(0, 0, 1);
        Tuning tuning = editor.currentTrack().tuning();
        Clef clef = Clef.forTuning(tuning);
        Pitch start = tuning.pitchOfString(1);
        Pitch expected = clef.pitchAtStep(StaffPosition.of(start, clef).step() - 3).orElseThrow();
        editor.toggleNotation();

        editor.moveDown();
        editor.moveDown();
        editor.moveDown();
        editor.enter();

        assertEquals(1, editor.currentBeat().notes().size(), "it has to have added a single new note");
        Note added = editor.currentBeat().notes().getFirst();
        assertEquals(expected, tuning.pitchOf(added),
                "three arrows down have to land on the pitch three degrees below the starting point");
    }

    @Test
    void tablatureNavigationNeverTouchesThePointerThatDrivesTheStaff() {
        Editor editor = new Editor(Score.blank());
        editor.moveTo(0, 0, 1);

        editor.moveDown();
        editor.moveDown();
        editor.moveUp();

        editor.toggleNotation();
        editor.enter();

        Note added = editor.currentBeat().noteOn(2).orElseThrow();
        assertEquals(editor.currentTrack().tuning().pitchOfString(2), editor.currentTrack().tuning().pitchOf(added),
                "tablature navigation must not leave any pitch stuck: when switching to standard notation,"
                        + " Enter has to add the open string pitch of where the cursor ended up");
    }

    private static Editor editorWithFirstBeat(Beat beat) {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                beat, Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter())));
        Track track = Track.standardGuitar("Test").withMeasure(0, measure);
        return new Editor(Score.blank().withTrack(0, track));
    }
}
