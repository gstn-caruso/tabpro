package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.harmony.Interval;
import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.harmony.ScaleTone;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class ScaleDegreesViewTest {

    @Test
    void theAccessibleNameIsAvailableInEnglish() {
        assertEquals("Scale Degrees", Texts.forLocale(Locale.ENGLISH).text("views.ScaleDegreesView.name"));
    }

    @Test
    void withoutAChosenScaleThereIsNoDegreeToPaint() {
        ScaleDegreesView view = new ScaleDegreesView();

        assertEquals(0, view.degreeCount());
    }

    @Test
    void showsTheNoteNameAndIntervalOfEachDegree() {
        ScaleDegreesView view = new ScaleDegreesView();

        view.show(List.of(
                new ScaleTone(PitchClass.of("C"), Interval.ROOT, 1),
                new ScaleTone(PitchClass.of("D"), Interval.MAJOR_SECOND, 2)));

        assertEquals(2, view.degreeCount());
        assertEquals("C", view.noteLabel(0));
        assertEquals("1", view.intervalLabel(0));
        assertEquals("D", view.noteLabel(1));
        assertEquals("2", view.intervalLabel(1));
    }

    @Test
    void theColumnsAreEvenlySpacedAndInIncreasingOrder() {
        ScaleDegreesView view = new ScaleDegreesView();
        view.setSize(420, 70);
        view.show(List.of(
                new ScaleTone(PitchClass.of("C"), Interval.ROOT, 1),
                new ScaleTone(PitchClass.of("D"), Interval.MAJOR_SECOND, 2),
                new ScaleTone(PitchClass.of("E"), Interval.MAJOR_THIRD, 3)));

        assertEquals(view.degreeX(1) - view.degreeX(0), view.degreeX(2) - view.degreeX(1));
        assertTrue(view.degreeX(0) < view.degreeX(1));
        assertTrue(view.degreeX(1) < view.degreeX(2));
    }

    @Test
    void hasAFixedAccessibleNameAndADescriptionThatListsTheDegrees() {
        ScaleDegreesView view = new ScaleDegreesView();

        assertEquals("Grados de la escala", view.getAccessibleContext().getAccessibleName());

        view.show(List.of(
                new ScaleTone(PitchClass.of("C"), Interval.ROOT, 1),
                new ScaleTone(PitchClass.of("D"), Interval.MAJOR_SECOND, 2)));

        assertEquals("C 1, D 2", view.getAccessibleContext().getAccessibleDescription());
    }
}
