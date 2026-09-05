package com.gstncaruso.tabpro.core.playback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.bars.DirectionJump;
import com.gstncaruso.tabpro.core.model.bars.DirectionSymbol;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * El orden en que se tocan los compases: de corrido si no hay nada especial,
 * pero respetando repeticiones, finales alternativos y saltos.
 */
class PlayOrderTest {

    @Test
    void unaPartituraSinAtributosEspecialesSeTocaDeCorrido() {
        Score score = scoreWithMeasures(3);

        PlayOrder order = PlayOrder.of(score);

        assertEquals(List.of(0, 1, 2), order.measureIndexes());
    }

    @Test
    void unaRepeticionSimpleSeTocaLasVecesQueIndicaElCierre() {
        Score score = scoreWithMeasures(2);
        score = withAttributes(score, 0, MeasureAttributes.plain().withRepeatOpen(true));
        score = withAttributes(score, 1, MeasureAttributes.plain().withRepeatCount(2));

        PlayOrder order = PlayOrder.of(score);

        assertEquals(List.of(0, 1, 0, 1), order.measureIndexes());
    }

    @Test
    void unaRepeticionDeUnSoloCompasSeTocaLasVecesQueIndicaElCierre() {
        Score score = scoreWithMeasures(1);
        score = withAttributes(score, 0,
                MeasureAttributes.plain().withRepeatOpen(true).withRepeatCount(3));

        PlayOrder order = PlayOrder.of(score);

        assertEquals(List.of(0, 0, 0), order.measureIndexes());
    }

    @Test
    void sinRepeticionAbiertaVuelveAlPrincipioDeLaPartitura() {
        Score score = scoreWithMeasures(3);
        score = withAttributes(score, 2, MeasureAttributes.plain().withRepeatCount(2));

        PlayOrder order = PlayOrder.of(score);

        assertEquals(List.of(0, 1, 2, 0, 1, 2), order.measureIndexes());
    }

    @Test
    void unFinalAlternativoSoloSeTocaEnLaVueltaQueIndica() {
        Score score = scoreWithMeasures(5);
        score = withAttributes(score, 0, MeasureAttributes.plain().withRepeatOpen(true));
        score = withAttributes(score, 2, MeasureAttributes.plain()
                .withRepeatCount(2)
                .withAlternateEndings(List.of(1)));
        score = withAttributes(score, 3, MeasureAttributes.plain().withAlternateEndings(List.of(2)));

        PlayOrder order = PlayOrder.of(score);

        assertEquals(List.of(0, 1, 2, 0, 1, 3, 4), order.measureIndexes());
    }

    @Test
    void unDaCapoAlFineVuelveAlPrincipioYTerminaEnElFine() {
        Score score = scoreWithMeasures(3);
        score = withAttributes(score, 1, MeasureAttributes.plain().withSymbol(DirectionSymbol.FINE));
        score = withAttributes(score, 2, MeasureAttributes.plain().withJump(DirectionJump.DA_CAPO_AL_FINE));

        PlayOrder order = PlayOrder.of(score);

        assertEquals(List.of(0, 1, 2, 0, 1), order.measureIndexes());
    }

    @Test
    void unDaSegnoSaltaAlSegno() {
        Score score = scoreWithMeasures(4);
        score = withAttributes(score, 1, MeasureAttributes.plain().withSymbol(DirectionSymbol.SEGNO));
        score = withAttributes(score, 3, MeasureAttributes.plain().withJump(DirectionJump.DA_SEGNO));

        PlayOrder order = PlayOrder.of(score);

        assertEquals(List.of(0, 1, 2, 3, 1, 2, 3), order.measureIndexes());
    }

    @Test
    void unSaltoQueYaSeUsoNoVuelveADispararse() {
        Score score = scoreWithMeasures(3);
        score = withAttributes(score, 2, MeasureAttributes.plain().withJump(DirectionJump.DA_CAPO));

        PlayOrder order = PlayOrder.of(score);

        // el salto vuelve una vez al principio; la segunda vez que se pisa el
        // mismo compas 2 el salto ya se uso y se sigue de largo.
        assertEquals(List.of(0, 1, 2, 0, 1, 2), order.measureIndexes());
    }

    @Test
    void unSaltoAUnSimboloQueNoExisteNoRompeLaSecuencia() {
        Score score = scoreWithMeasures(2);
        score = withAttributes(score, 1, MeasureAttributes.plain().withJump(DirectionJump.DA_SEGNO));

        PlayOrder order = PlayOrder.of(score);

        assertEquals(List.of(0, 1), order.measureIndexes());
    }

    @Test
    void unaPartituraMalArmadaNoSeCuelgaEnUnBucleInfinito() {
        Score score = scoreWithMeasures(2);
        score = withAttributes(score, 0, MeasureAttributes.plain().withRepeatOpen(true));
        score = withAttributes(score, 1,
                MeasureAttributes.plain().withRepeatCount(Integer.MAX_VALUE));

        PlayOrder order = PlayOrder.of(score);

        assertTrue(order.measureIndexes().size() <= PlayOrder.MAX_STEPS);
    }

    private Score scoreWithMeasures(int count) {
        Score score = Score.blank();
        for (int i = 1; i < count; i++) {
            score = score.withMeasureInsertedInEveryTrackAt(i);
        }
        return score;
    }

    private Score withAttributes(Score score, int index, MeasureAttributes attributes) {
        return score.withAttributesInEveryTrackAt(index, attributes);
    }
}
