package com.gstncaruso.tabpro.ui.dialogs.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendPoint;
import com.gstncaruso.tabpro.core.model.effects.BendType;
import java.util.List;
import org.junit.jupiter.api.Test;

class BendCurveEditorTest {

    @Test
    void startsWithTheGivenPoints() {
        Bend bend = Bend.of(BendType.BEND, 4);

        BendCurveEditor editor = BendCurveEditor.of(bend);

        assertEquals(bend.points(), editor.points());
    }

    @Test
    void clickingAnEmptySpotAddsAPoint() {
        BendCurveEditor editor = new BendCurveEditor(List.of(BendPoint.at(0, 0), BendPoint.at(60, 4)));

        editor.clickAt(30, 8);

        assertTrue(editor.points().contains(BendPoint.at(30, 8)));
        assertEquals(3, editor.points().size());
    }

    @Test
    void clickingExactlyOnAPointRemovesIt() {
        BendCurveEditor editor = new BendCurveEditor(List.of(BendPoint.at(0, 0), BendPoint.at(30, 8), BendPoint.at(60, 4)));

        editor.clickAt(30, 8);

        assertEquals(List.of(BendPoint.at(0, 0), BendPoint.at(60, 4)), editor.points());
    }

    @Test
    void clickingTheSameColumnAtADifferentHeightMovesThePoint() {
        BendCurveEditor editor = new BendCurveEditor(List.of(BendPoint.at(0, 0), BendPoint.at(30, 8), BendPoint.at(60, 4)));

        editor.clickAt(30, 2);

        assertEquals(List.of(BendPoint.at(0, 0), BendPoint.at(30, 2), BendPoint.at(60, 4)), editor.points());
    }

    @Test
    void neverDropsBelowTwoPoints() {
        BendCurveEditor editor = new BendCurveEditor(List.of(BendPoint.at(0, 0), BendPoint.at(60, 4)));

        editor.clickAt(60, 4);

        assertEquals(2, editor.points().size());
    }

    @Test
    void pointsStayOrderedByPosition() {
        BendCurveEditor editor = new BendCurveEditor(List.of(BendPoint.at(0, 0), BendPoint.at(60, 4)));

        editor.clickAt(10, 2);

        assertEquals(List.of(0, 10, 60), editor.points().stream().map(BendPoint::position).toList());
    }

    @Test
    void rightClickAddsVibratoToTheNearestPoint() {
        BendCurveEditor editor = new BendCurveEditor(List.of(BendPoint.at(0, 0), BendPoint.at(60, 4)));

        editor.addVibratoAt(58);

        assertEquals(1, pointAt(editor, 60).vibrato());
    }

    @Test
    void vibratoCyclesThroughThreeLevelsAndBackToZero() {
        BendCurveEditor editor = new BendCurveEditor(List.of(BendPoint.at(0, 0), BendPoint.at(60, 4)));

        editor.addVibratoAt(60);
        editor.addVibratoAt(60);
        editor.addVibratoAt(60);
        assertEquals(3, pointAt(editor, 60).vibrato());

        editor.addVibratoAt(60);
        assertEquals(0, pointAt(editor, 60).vibrato());
    }

    @Test
    void toBendCarriesTheChosenType() {
        BendCurveEditor editor = new BendCurveEditor(List.of(BendPoint.at(0, 0), BendPoint.at(60, 4)));

        assertEquals(BendType.PREBEND, editor.toBend(BendType.PREBEND).type());
    }

    @Test
    void resetReplacesTheWholeCurve() {
        BendCurveEditor editor = new BendCurveEditor(List.of(BendPoint.at(0, 0), BendPoint.at(60, 4)));

        editor.reset(Bend.of(BendType.PREBEND, 8).points());

        assertEquals(Bend.of(BendType.PREBEND, 8).points(), editor.points());
    }

    private static BendPoint pointAt(BendCurveEditor editor, int position) {
        return editor.points().stream().filter(point -> point.position() == position).findFirst().orElseThrow();
    }
}
