package com.gstncaruso.tabpro.core.playback;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Pitch;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PlayerTest {

    @Test
    void theDefaultImplementationSoundsEachNoteOfTheSequenceInOrder() {
        List<Pitch> sounded = new ArrayList<>();
        Player player = new SilentPlayer() {
            @Override
            public void playNote(Pitch pitch, int program) {
                sounded.add(pitch);
            }
        };

        player.playSequence(List.of(new Pitch(60), new Pitch(62), new Pitch(64)), 25);

        assertEquals(List.of(new Pitch(60), new Pitch(62), new Pitch(64)), sounded);
    }

    private static class SilentPlayer implements Player {

        @Override
        public void play(Timeline timeline, PlaybackListener listener) {
        }

        @Override
        public void playNote(Pitch pitch, int program) {
        }

        @Override
        public void stop() {
        }

        @Override
        public boolean isPlaying() {
            return false;
        }
    }
}
