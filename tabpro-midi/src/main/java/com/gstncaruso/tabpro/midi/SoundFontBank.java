package com.gstncaruso.tabpro.midi;

import com.sun.media.sound.AudioSynthesizer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Soundbank;
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

    private final Supplier<Synthesizer> synthesizers;
    private Optional<Path> file;
    private boolean active = true;
    private boolean anyPortTried;
    private final Map<Integer, SoundFontSynthesizer> synthesizersByPort = new ConcurrentHashMap<>();

    /** El archivo ya resuelto (la eleccion del usuario, o el que haya encontrado el sistema). */
    public SoundFontBank(Optional<Path> file) {
        this(file, SoundFontSynthesizer::systemSynthesizer);
    }

    /**
     * Con el sintetizador del sistema elegido de afuera (null si no hay ninguno disponible), para
     * poder probar que pasa en una maquina sin placa de sonido sin depender de si esta maquina de
     * pruebas tiene una de verdad -la misma costura que MidiPlayer.PortOutput ya usa con su
     * Supplier&lt;Sequencer&gt;.
     */
    SoundFontBank(Optional<Path> file, Supplier<Synthesizer> synthesizers) {
        this.file = file;
        this.synthesizers = synthesizers;
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
     * sintetizador interno del JDK. La regla no depende de por que fallo -archivo invalido,
     * sintetizador que no abre-: esta activo si y solo si sus instrumentos quedaron cargados de
     * verdad en algun puerto, o si todavia no se probo ninguno (recien elegido, antes de tocar).
     */
    public boolean active() {
        if (!active || file.isEmpty()) {
            return false;
        }
        if (!anyPortTried) {
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
        if (anyPortTried) {
            return "No se pudo cargar " + name + ": suena el sintetizador interno del JDK";
        }
        return "Banco elegido: " + name + " (se aplica al reproducir)";
    }

    /**
     * Un sintetizador nuevo, para un uso de una sola vez: el render a WAVE es offline, asi que
     * nunca hace falta abrir una linea de audio real (Synthesizer.open()) para conseguirlo -algo
     * que en una maquina sin placa de sonido revienta con MidiUnavailableException aunque nadie
     * vaya a reproducir nada de verdad. En cambio, el banco se carga reciennaden el momento en que
     * quien use este sintetizador (WaveRenderer) lo abra para renderizar fuera de tiempo real
     * (AudioSynthesizer.openStream), que Gervill puede hacer sin ninguna placa de sonido. Quien lo
     * pide es responsable de cerrarlo.
     */
    public Synthesizer freshSynthesizer() throws MidiUnavailableException {
        Synthesizer synth = synthesizers.get();
        if (synth == null) {
            throw new MidiUnavailableException("no hay ningun sintetizador disponible para renderizar el audio");
        }
        if (!active || file.isEmpty() || !(synth instanceof AudioSynthesizer audioSynth)) {
            return synth;
        }
        Optional<Soundbank> bank = SoundFonts.read(file.get());
        if (bank.isEmpty()) {
            return synth;
        }
        return loadingOnFirstOpen(audioSynth, bank.get());
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
        anyPortTried = true;
        try {
            SoundFontSynthesizer synth = SoundFontSynthesizer.open(file, synthesizers);
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

    /**
     * Envuelve el sintetizador en un proxy que carga el banco justo despues de que se abra para
     * renderizar offline (openStream), y nunca antes: cargar instrumentos antes de esa apertura no
     * funciona (Gervill los descarta), y abrir el sintetizador nosotros mismos de antemano
     * rompería la apertura que hace despues quien renderiza.
     */
    private static Synthesizer loadingOnFirstOpen(AudioSynthesizer real, Soundbank bank) {
        InvocationHandler handler = (proxy, method, args) -> {
            Object result = method.invoke(real, args);
            if (method.getName().equals("openStream")) {
                real.unloadAllInstruments(real.getDefaultSoundbank());
                real.loadAllInstruments(bank);
            }
            return result;
        };
        return (Synthesizer) Proxy.newProxyInstance(
                SoundFontBank.class.getClassLoader(), new Class<?>[] {AudioSynthesizer.class}, handler);
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
