package com.gstncaruso.tabpro.midi;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Soundbank;

/**
 * Los bancos SoundFont (.sf2, .dls) instalados en la maquina, y como leerlos sin romper si el
 * archivo no sirve. Gervill, el sintetizador del JDK, sabe tocar los dos formatos.
 */
public final class SoundFonts {

    /** Donde Linux suele dejar los bancos GM instalados por un paquete del sistema. */
    private static final List<Path> SYSTEM_DIRECTORIES = List.of(
            Path.of("/usr/share/sounds/sf2"),
            Path.of("/usr/share/soundfonts"));

    private static final List<String> EXTENSIONS = List.of(".sf2", ".dls");

    private SoundFonts() {
    }

    /** Los bancos instalados en la maquina, en las rutas estandar de Linux. */
    public static List<Path> installed() {
        return installed(SYSTEM_DIRECTORIES);
    }

    /** Lo mismo, pero buscando en las rutas que se le den (para poder probarlo sin tocar el disco real). */
    static List<Path> installed(List<Path> directories) {
        List<Path> found = new ArrayList<>();
        for (Path directory : directories) {
            addSoundFontsIn(directory, found);
        }
        return List.copyOf(found);
    }

    /** El banco de ese archivo, o vacio si no existe o esta corrupto: nunca rompe. */
    public static Optional<Soundbank> read(Path file) {
        try {
            return Optional.of(MidiSystem.getSoundbank(file.toFile()));
        } catch (InvalidMidiDataException | IOException e) {
            System.err.println("No se pudo leer el banco de sonido " + file + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    private static void addSoundFontsIn(Path directory, List<Path> found) {
        if (!Files.isDirectory(directory)) {
            return;
        }
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(directory)) {
            List<Path> here = new ArrayList<>();
            for (Path entry : entries) {
                if (isSoundFont(entry)) {
                    here.add(entry);
                }
            }
            here.sort(Comparator.comparing(p -> p.getFileName().toString()));
            found.addAll(here);
        } catch (IOException ignored) {
            // Un directorio que no se puede leer simplemente no aporta bancos.
        }
    }

    private static boolean isSoundFont(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return EXTENSIONS.stream().anyMatch(name::endsWith);
    }
}
