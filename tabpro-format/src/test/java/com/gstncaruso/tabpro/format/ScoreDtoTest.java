package com.gstncaruso.tabpro.format;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Score;
import org.junit.jupiter.api.Test;

class ScoreDtoTest {

    @Test
    void roundTripsABlankScore() {
        Score score = Score.blank();

        ScoreDto dto = ScoreDto.from(score);

        assertEquals(score, dto.toScore());
    }
}
