package com.gstncaruso.tabpro.midi;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Synthesizer;

/**
 * El banco SoundFont es global -una eleccion, un F2, el reemplazo libre del RSE-, pero cada
 * puerto que use el sintetizador interno de tabpro necesita su propia instancia de
 * {@link Synthesizer}: no se puede compartir un receiver entre puertos que corren canales MIDI
 * independientes sin que se pisen. Esta clase reparte el mismo archivo elegido a cada puerto que
 * lo pida, abriendo su sintetizador de a uno la primera vez que se usa.
 *
 * <p>Si a algun puerto le falla -no hay memoria, el sintetizador no abre, el archivo esta
 * corrupto- ese puerto se queda sonando con el sintetizador interno del JDK (o mudo si ni eso se
 * pudo abrir), sin afectar a los demas puertos ni tirar abajo la reproduccion. Un puerto ruteado
 * a un dispositivo MIDI externo no pasa por aca: tiene sus propios sonidos y no son nuestros (ver
 * {@link MidiPlayer#useOutputForPort}).
 */
public final class SoundFontBank implements AutoCloseable {

    private Optional<Path> file;
    private boolean active = true;
    private final Map<Integer, SoundFontSynthesizer> synthesizersByPort = new ConcurrentHashMap<>();

    /** El archivo ya resuelto (la eleccion del usuario, o el que haya encontrado el sistema). */
    public SoundFontBank(Optional<Path> file) {
        this.file = file;
    }

    /**
     * El receiver listo para ese puerto: el sintetizador interno con el banco puesto si se pudo,
     * o uno mudo si ni el sintetizador se pudo abrir. Nunca lanza: un puerto que falla no se
     * lleva puesta la reproduccion de los demas.
     */
    public Receiver receiverForPort(int port) {
        return synthesizerForPort(port).map(SoundFontSynthesizer::receiver).orElseGet(SoundFontBank::silentReceiver);
    }

    /** F2: prende o apaga el banco en todos los puertos que lo tengan cargado, a la vez. */
    public void toggle() {
        if (file.isEmpty()) {
            return;
        }
        active = !active;
        synthesizersByPort.values().forEach(SoundFontSynthesizer::toggle);
    }

    /** El usuario elige otro archivo. Si ya estaba puesto ese mismo archivo, no hace nada. */
    public void choose(Optional<Path> newFile) {
        if (newFile.equals(file)) {
            return;
        }
        file = newFile;
        active = true;
        synthesizersByPort.values().forEach(synth -> synth.choose(newFile));
    }

    /**
     * Si el banco esta (o va a quedar, apenas se abra el primer puerto) sonando en vez del
     * sintetizador interno del JDK. Una vez que algun puerto lo intento de verdad, refleja si de
     * verdad se pudo cargar en alguno: un archivo invalido nunca puede quedar activo.
     */
    public boolean active() {
        if (!active || file.isEmpty()) {
            return false;
        }
        if (synthesizersByPort.isEmpty()) {
            return true;
        }
        return synthesizersByPort.values().stream().anyMatch(synth -> synth.file().isPresent());
    }

    /** El archivo elegido, para mostrarlo en Options > MIDI Setup. */
    public Optional<Path> file() {
        return file;
    }

    /** Como esta el banco de sonido ahora, para mostrarselo al usuario. */
    public String status() {
        if (file.isEmpty()) {
            return "Sin ningún banco de sonido: suena el sintetizador interno del JDK";
        }
        String name = file.get().getFileName().toString();
        if (!active) {
            return "Banco de sonido desactivado (" + name + "): suena el sintetizador interno del JDK";
        }
        if (synthesizersByPort.values().stream().anyMatch(synth -> synth.file().isPresent())) {
            return "Sonando con " + name;
        }
        if (!synthesizersByPort.isEmpty()) {
            return "No se pudo cargar " + name + ": suena el sintetizador interno del JDK";
        }
        return "Banco elegido: " + name + " (se aplica al reproducir)";
    }

    /**
     * Un sintetizador nuevo, para un uso de una sola vez -como el render a WAVE-, con el mismo
     * banco puesto (activo o no, igual que en vivo). Quien lo pide es responsable de cerrarlo.
     */
    public Synthesizer freshSynthesizer() throws MidiUnavailableException {
        SoundFontSynthesizer synth = SoundFontSynthesizer.open(file);
        if (!active) {
            synth.toggle();
        }
        return synth.synthesizer();
    }

    @Override
    public void close() {
        synthesizersByPort.values().forEach(SoundFontSynthesizer::close);
        synthesizersByPort.clear();
    }

    private Optional<SoundFontSynthesizer> synthesizerForPort(int port) {
        SoundFontSynthesizer existing = synthesizersByPort.get(port);
        if (existing != null) {
            return Optional.of(existing);
        }
        try {
            SoundFontSynthesizer synth = SoundFontSynthesizer.open(file);
            if (!active) {
                synth.toggle();
            }
            synthesizersByPort.put(port, synth);
            return Optional.of(synth);
        } catch (MidiUnavailableException e) {
            System.err.println(
                    "El puerto " + port + " se queda sin sintetizador interno para el banco de sonido: "
                            + e.getMessage());
            return Optional.empty();
        }
    }

    private static Receiver silentReceiver() {
        return new Receiver() {
            @Override
            public void send(javax.sound.midi.MidiMessage message, long timeStamp) {
            }

            @Override
            public void close() {
            }
        };
    }
}
