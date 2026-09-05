package com.gstncaruso.tabpro.app;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.playback.PlaybackListener;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.core.playback.Timeline;
import com.gstncaruso.tabpro.format.JsonScoreFiles;
import com.gstncaruso.tabpro.midi.MidiPlayer;
import com.gstncaruso.tabpro.ui.MainFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.Optional;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.SwingUtilities;

public class App {

    public static void main(String[] args) {
        Theme theme = Theme.install();
        Editor editor = new Editor(Score.blank());

        Optional<MidiPlayer> midiPlayer = openMidiPlayer();
        midiPlayer.ifPresent(App::warmUpInBackground);
        Player player = midiPlayer.<Player>map(midi -> midi).orElseGet(App::silentPlayer);
        Optional<Path> fileToOpen = fileFrom(args);

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(editor, new JsonScoreFiles(), player, theme);
            frame.setIconImages(AppIcon.sizes());
            midiPlayer.ifPresent(midi -> frame.addWindowListener(closeOnDispose(midi)));
            frame.setVisible(true);
            fileToOpen.ifPresent(frame::openOnStartup);
        });
    }

    /** El archivo que el escritorio pasa al abrir una partitura con tabpro. */
    private static Optional<Path> fileFrom(String[] args) {
        return args.length == 0 ? Optional.empty() : Optional.of(Path.of(args[0]));
    }

    private static Optional<MidiPlayer> openMidiPlayer() {
        try {
            return Optional.of(new MidiPlayer(MidiSystem.getSequencer()));
        } catch (MidiUnavailableException e) {
            System.err.println("MIDI no disponible, la reproducción quedará silenciada: " + e.getMessage());
            return Optional.empty();
        }
    }

    private static void warmUpInBackground(MidiPlayer midi) {
        new Thread(midi::open, "midi-open").start();
    }

    private static WindowAdapter closeOnDispose(MidiPlayer midi) {
        return new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                midi.close();
            }
        };
    }

    private static Player silentPlayer() {
        return new Player() {
            @Override
            public void play(Timeline timeline, PlaybackListener listener) {
                listener.playbackFinished();
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
        };
    }
}
