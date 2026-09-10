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

/** Gervill, the JDK's built-in synthesizer, can read both the .sf2 and .dls formats. */
public final class SoundFonts {

    /** Where Linux distro packages typically install General MIDI soundfonts. */
    private static final List<Path> SYSTEM_DIRECTORIES = List.of(
            Path.of("/usr/share/sounds/sf2"),
            Path.of("/usr/share/soundfonts"));

    private static final List<String> EXTENSIONS = List.of(".sf2", ".dls");

    private SoundFonts() {
    }

    public static List<Path> installed() {
        return installed(SYSTEM_DIRECTORIES);
    }

    static List<Path> installed(List<Path> directories) {
        List<Path> found = new ArrayList<>();
        for (Path directory : directories) {
            addSoundFontsIn(directory, found);
        }
        return List.copyOf(found);
    }

    public static Optional<Soundbank> read(Path file) {
        try {
            return Optional.of(MidiSystem.getSoundbank(file.toFile()));
        } catch (InvalidMidiDataException | IOException e) {
            System.err.println("Could not read the sound bank " + file + ": " + e.getMessage());
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
        }
    }

    private static boolean isSoundFont(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return EXTENSIONS.stream().anyMatch(name::endsWith);
    }
}
