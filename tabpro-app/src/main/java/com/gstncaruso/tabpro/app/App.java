package com.gstncaruso.tabpro.app;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.playback.PlaybackListener;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.core.playback.Timeline;
import com.gstncaruso.tabpro.format.JsonScoreFiles;
import com.gstncaruso.tabpro.midi.MidiPlayer;
import com.gstncaruso.tabpro.ui.MainFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Optional;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.SwingUtilities;

public class App {

    public static void main(String[] args) {
        FlatDarculaLaf.setup();
        Editor editor = new Editor(Score.blank());

        Optional<MidiPlayer> midiPlayer = openMidiPlayer();
        midiPlayer.ifPresent(App::warmUpInBackground);
        Player player = midiPlayer.<Player>map(midi -> midi).orElseGet(App::silentPlayer);

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(editor, new JsonScoreFiles(), player);
            midiPlayer.ifPresent(midi -> frame.addWindowListener(closeOnDispose(midi)));
            frame.setVisible(true);
        });
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
            public void stop() {
            }

            @Override
            public boolean isPlaying() {
                return false;
            }
        };
    }
}
