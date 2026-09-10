package com.gstncaruso.tabpro.ui.browser;

import com.gstncaruso.tabpro.core.files.ScoreFiles;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ScoreBrowserPanelTest {

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        ScoreBrowserPanel panel = new ScoreBrowserPanel(new NoOpScoreFiles(), path -> { }, new NoOpSound(), () -> { });

        AccessibilityAssertions.assertNoViolations(panel);
    }

    private static final class NoOpScoreFiles implements ScoreFiles {

        @Override
        public Score load(Path path) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void save(Score score, Path path) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class NoOpSound implements BrowserPlayback.Sound {

        @Override
        public void play(Score score, int bars, Runnable onFinished) {
        }

        @Override
        public void stop() {
        }
    }
}
