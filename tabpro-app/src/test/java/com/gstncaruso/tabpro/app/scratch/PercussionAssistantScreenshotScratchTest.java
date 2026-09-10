package com.gstncaruso.tabpro.app.scratch;

import com.gstncaruso.tabpro.app.Theme;
import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.playback.PlaybackListener;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.core.playback.Timeline;
import com.gstncaruso.tabpro.ui.percussion.PercussionAssistant;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class PercussionAssistantScreenshotScratchTest {

    @Test
    void capture() throws Exception {
        Theme.install();
        Editor editor = new Editor(new Score("Captura", 120, List.of(Track.percussion("Batería"))));
        PercussionAssistant assistant = new PercussionAssistant(editor, new SilentPlayer());

        assistant.setSize(assistant.getPreferredSize());
        layoutRecursively(assistant);

        BufferedImage image = new BufferedImage(
                assistant.getWidth(), assistant.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        assistant.printAll(graphics);
        graphics.dispose();

        File destination = new File(
                "/tmp/claude-1000/-home-gaston-Code-tabpro/b1197946-858b-47f1-94de-a4c8afc7f58b"
                        + "/scratchpad/asistente-percusion.png");
        ImageIO.write(image, "png", destination);
    }

    private static void layoutRecursively(Component component) {
        component.doLayout();
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                layoutRecursively(child);
            }
        }
    }

    private static final class SilentPlayer implements Player {
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
