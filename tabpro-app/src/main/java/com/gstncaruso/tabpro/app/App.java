package com.gstncaruso.tabpro.app;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.files.ScoreExchange;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.playback.PlaybackListener;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.core.playback.Timeline;
import com.gstncaruso.tabpro.format.JsonScoreFiles;
import com.gstncaruso.tabpro.format.exchange.NotationExchange;
import com.gstncaruso.tabpro.midi.MidiPlayer;
import com.gstncaruso.tabpro.midi.SoundExchange;
import com.gstncaruso.tabpro.midi.SoundFontBank;
import com.gstncaruso.tabpro.midi.WaveRenderer;
import com.gstncaruso.tabpro.ui.MainFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.Optional;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.swing.SwingUtilities;

public class App {

    public static void main(String[] args) {
        Theme theme = Theme.install();
        Editor editor = new Editor(Score.blank());

        /*
         * Vacio: recien queda elegido cuando MainFrame se construye y aplica lo guardado en
         * MidiSetupPreferences (ver useMidiSetup), igual que ya pasa con el dispositivo de cada
         * puerto. Es global (no por puerto): cada puerto que use el sintetizador interno de
         * tabpro le pide su propia instancia a este mismo banco (ver SoundFontBank).
         */
        SoundFontBank soundBank = new SoundFontBank(Optional.empty());
        Optional<MidiPlayer> midiPlayer = openMidiPlayer(soundBank);
        midiPlayer.ifPresent(App::warmUpInBackground);
        Player player = midiPlayer.<Player>map(midi -> midi).orElseGet(App::silentPlayer);
        MidiDeviceSetup devices = new MidiDeviceSetup(midiPlayer, soundBank);
        Optional<Path> fileToOpen = fileFrom(args);

        SwingUtilities.invokeLater(() -> {
            ScoreExchange exchange = new CombinedExchange(
                    new NotationExchange(),
                    new SoundExchange(new WaveRenderer(() -> synthesizerForWaveExport(soundBank))));
            MainFrame frame = new MainFrame(editor, new JsonScoreFiles(), player, theme, devices, exchange, new Microphone());
            frame.setIconImages(AppIcon.sizes());
            midiPlayer.ifPresent(midi -> frame.addWindowListener(closeOnDispose(midi, soundBank)));
            frame.setVisible(true);
            fileToOpen.ifPresent(frame::openOnStartup);
        });
    }

    /** El archivo que el escritorio pasa al abrir una partitura con tabpro. */
    private static Optional<Path> fileFrom(String[] args) {
        return args.length == 0 ? Optional.empty() : Optional.of(Path.of(args[0]));
    }

    private static Optional<MidiPlayer> openMidiPlayer(SoundFontBank soundBank) {
        try {
            Sequencer sequencer = MidiSystem.getSequencer();
            return Optional.of(new MidiPlayer(sequencer, soundBank::receiverForPort));
        } catch (MidiUnavailableException e) {
            System.err.println("MIDI no disponible, la reproducción quedará silenciada: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * El sintetizador que renderiza el WAVE: una instancia nueva de un solo uso (WaveRenderer la
     * cierra sola al terminar) con el mismo banco de sonido global que esta sonando en vivo, para
     * que el archivo suene igual que la reproduccion.
     */
    private static Synthesizer synthesizerForWaveExport(SoundFontBank soundBank) {
        try {
            return soundBank.freshSynthesizer();
        } catch (MidiUnavailableException e) {
            throw new IllegalStateException("No hay sintetizador disponible para exportar WAVE.", e);
        }
    }

    private static void warmUpInBackground(MidiPlayer midi) {
        new Thread(midi::open, "midi-open").start();
    }

    private static WindowAdapter closeOnDispose(MidiPlayer midi, SoundFontBank soundBank) {
        return new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                midi.close();
                soundBank.close();
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
