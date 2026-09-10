package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import java.util.List;
import org.junit.jupiter.api.Test;

class TabBottomRoomTest {

    private static final int WIDTH = 770;

    @Test
    void aTrackLeavesRoomUnderItsLastString() {
        Score score = new Score("Test", 120, List.of(Track.standardGuitar("Guitarra")));
        ScoreLayout layout = ScoreLayout.of(score, WIDTH);

        int lastString = layout.stringY(0, 0, score.track(0).stringCount());
        int trackBottom = layout.trackTop(0, 0) + layout.trackHeight(0);

        assertTrue(
                trackBottom - lastString >= ScoreLayout.TAB_BOTTOM_PADDING,
                "the last string's number is clipped: " + (trackBottom - lastString) + " pixels left over");
    }

    @Test
    void theLastTrackOfASystemAlsoLeavesRoom() {
        Score score = new Score(
                "Test", 120, List.of(Track.standardGuitar("Guitarra"), Track.standardBass("Bajo")));
        ScoreLayout layout = ScoreLayout.of(score, WIDTH);

        int lastString = layout.stringY(1, 0, score.track(1).stringCount());
        int systemBottom = layout.systemTop(0) + layout.systemHeight();

        assertTrue(
                systemBottom - lastString >= ScoreLayout.TAB_BOTTOM_PADDING,
                "the system ends above the last string of the track below");
    }
}
