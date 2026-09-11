package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.blankEditor;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.editorWithANote;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findButton;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponent;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findMenuItem;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.repoFile;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.withDialog;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.app.CombinedExchange;
import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.files.ScoreExchange;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.format.JsonScoreFiles;
import com.gstncaruso.tabpro.format.exchange.NotationExchange;
import com.gstncaruso.tabpro.format.guitarpro.GuitarProFile;
import com.gstncaruso.tabpro.midi.MidiScoreExporter;
import com.gstncaruso.tabpro.midi.SoundExchange;
import com.gstncaruso.tabpro.midi.WaveRenderer;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.i18n.TextsDefaultNames;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.imageio.ImageIO;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;

@Tag("integration")
@ResourceLock(AuditSupport.SWING_LOCK)
class ImportExportAuditTest {

    private final ScoreExchange exchange = new CombinedExchange(
            new NotationExchange(new TextsDefaultNames()),
            new SoundExchange(new WaveRenderer(ImportExportAuditTest::systemSynthesizer)));

    @BeforeEach
    @AfterEach
    void clearRecentFiles() {
        java.util.prefs.Preferences.userRoot().node("com/gstncaruso/tabpro").remove("recentFiles");
    }

    private MainFrame newFrame(Editor editor) throws Exception {
        return AuditSupport.newFrame(editor, new JsonScoreFiles(), exchange);
    }

    @Test
    void openingThroughTheMenuReadsAnOwnTabproFileTheSameAsItWasSaved(@TempDir Path tempDir) throws Exception {
        Score original = Score.blank(new TextsDefaultNames());
        Path path = tempDir.resolve("own.tabpro");
        new JsonScoreFiles().save(original, path);

        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Abrir…");
            assertNotNull(item, "could not find 'Abrir…' in the real menu");

            withDialog(item::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                assertNotNull(chooser, "could not find the real JFileChooser");
                chooser.setSelectedFile(path.toFile());
                chooser.approveSelection();
            });

            assertEquals(original, editor.score(), "opening a real .tabpro must leave the model equal to the file");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void openingThroughTheMenuRecognizesAGuitarProFileWithoutGoingThroughImport() throws Exception {
        Path path = repoFile("tabpro-format/src/test/resources/guitarpro/tabpro-features.gp5");
        assertTrue(Files.exists(path), "could not find the real Guitar Pro fixture in tabpro-format");

        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Abrir…");

            withDialog(item::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                chooser.setSelectedFile(path.toFile());
                chooser.approveSelection();
            });

            assertEquals(3, editor.score().trackCount(), "the same Abrir must recognize a real .gp5");
            assertEquals("Lead Guitar", editor.score().track(0).name());
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void savingWithoutAFileYetOpensSaveAsAndTheSecondTimeOverwritesWithoutAsking(@TempDir Path tempDir)
            throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem save = findMenuItem(frame.getJMenuBar(), "Guardar");
            assertNotNull(save, "could not find 'Guardar' in the real menu");
            assertEquals(KeyStroke.getKeyStroke("ctrl S"), save.getAccelerator());

            Path path = tempDir.resolve("first-time.tabpro");
            withDialog(save::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                assertNotNull(chooser, "with no file yet, Guardar must open the real Guardar como chooser");
                chooser.setSelectedFile(path.toFile());
                chooser.approveSelection();
            });

            assertTrue(Files.exists(path));
            assertEquals(editor.score(), new JsonScoreFiles().load(path));

            editor.setFret(5);
            SwingUtilities.invokeAndWait(save::doClick);

            assertEquals(
                    editor.score(), new JsonScoreFiles().load(path),
                    "with a file already chosen, Guardar must overwrite it without asking where again");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void aRecentlySavedFileAppearsInOpenRecentAndChoosingItReallyOpensIt(@TempDir Path tempDir) throws Exception {
        Path path = tempDir.resolve("recent.tabpro");
        Editor firstEditor = editorWithANote();
        MainFrame firstFrame = newFrame(firstEditor);
        try {
            JMenuItem saveAs = findMenuItem(firstFrame.getJMenuBar(), "Guardar como…");
            assertNotNull(saveAs, "could not find 'Guardar como…' in the real menu");

            withDialog(saveAs::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                chooser.setSelectedFile(path.toFile());
                chooser.approveSelection();
            });
        } finally {
            AuditSupport.dispose(firstFrame);
        }

        Editor secondEditor = blankEditor();
        MainFrame secondFrame = newFrame(secondEditor);
        try {
            JMenuItem recent = findMenuItem(secondFrame.getJMenuBar(), path.getFileName().toString());
            assertNotNull(recent, "the just-saved file must appear in Archivo > Abrir reciente");

            SwingUtilities.invokeAndWait(recent::doClick);

            assertEquals(
                    new JsonScoreFiles().load(path), secondEditor.score(),
                    "choosing the file in Abrir reciente must really open it");
        } finally {
            AuditSupport.dispose(secondFrame);
        }
    }

    @Test
    void importingMidiThroughTheMenuOffersTheRealTracksAndQuickImportBringsThemToTheModel(@TempDir Path tempDir)
            throws Exception {
        Path midiPath = tempDir.resolve("other.mid");
        new MidiScoreExporter().export(Score.blank(new TextsDefaultNames()), midiPath);

        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenu importMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Importar");
            assertNotNull(importMenu, "could not find the real 'Importar' submenu");
            JMenuItem item = AuditSupport.findMenuItem(importMenu, "MIDI…");
            assertNotNull(item, "could not find 'MIDI…' inside Importar");

            withDialog(item::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                if (chooser != null) {
                    chooser.setSelectedFile(midiPath.toFile());
                    chooser.approveSelection();
                    return;
                }
                JList<?> trackList = findComponent(dialog, JList.class);
                assertNotNull(trackList, "could not find the real MIDI track list");
                assertEquals(1, trackList.getModel().getSize(), "the .mid has a single audible track");
                trackList.setSelectedIndex(0);

                JButton quickImport = findButton(dialog, "Import rápido (reemplaza la partitura)");
                assertNotNull(quickImport, "could not find the real quick import button");
                quickImport.doClick();

                findButton(dialog, "Cerrar").doClick();
            });

            assertEquals(1, editor.score().trackCount());
            assertEquals("Guitarra", editor.score().track(0).name(),
                    "the real quick import must bring the track name from the foreign MIDI");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void importingAsciiTablatureThroughTheMenuBringsTheRealNotesToTheActiveTrack() throws Exception {
        String tab = "|--5--0--|\n" + "|--------|\n".repeat(5);

        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenu importMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Importar");
            JMenuItem item = AuditSupport.findMenuItem(importMenu, "Tablatura ASCII…");
            assertNotNull(item, "could not find 'Tablatura ASCII…' inside Importar");

            withDialog(item::doClick, dialog -> {
                JTextArea text = findComponent(dialog, JTextArea.class);
                assertNotNull(text, "could not find the real text area for ASCII import");
                text.setText(tab);
                findButton(dialog, "Importar").doClick();
            });

            assertEquals(List.of(new Note(1, 5)), editor.score().track(0).measure(0).beat(0).notes());
            assertEquals(List.of(new Note(1, 0)), editor.score().track(0).measure(0).beat(1).notes());
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void importingMusicXmlThroughTheMenuReadsTheKeySignatureAndTheRealNotesFromTheFile() throws Exception {
        Path path = repoFile("tabpro-format/src/test/resources/musicxml/armadura-en-fa.musicxml");
        assertTrue(Files.exists(path), "could not find the real MusicXML fixture in tabpro-format");

        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenu importMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Importar");
            JMenuItem item = AuditSupport.findMenuItem(importMenu, "MusicXML…");
            assertNotNull(item, "could not find 'MusicXML…' inside Importar");

            withDialog(item::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                chooser.setSelectedFile(path.toFile());
                chooser.approveSelection();
            });

            assertEquals(-1, editor.score().attributesOf(0).keySignature().accidentals(),
                    "F major is 1 flat (fifths=-1)");
            Track track = editor.score().track(0);
            assertEquals(65, track.pitchOf(track.measure(0).beat(0).notes().get(0)).midiNumber(), "F4");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void importingPowerTabThroughTheMenuReadsARealFileFromThePowerTabEditorRepository() throws Exception {
        Path path = repoFile("tabpro-format/src/test/resources/powertab/guitars.ptb");
        assertTrue(Files.exists(path), "could not find the real PowerTab fixture in tabpro-format");

        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenu importMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Importar");
            JMenuItem item = AuditSupport.findMenuItem(importMenu, "PowerTab…");
            assertNotNull(item, "could not find 'PowerTab…' inside Importar");

            withDialog(item::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                chooser.setSelectedFile(path.toFile());
                chooser.approveSelection();
            });

            assertTrue(editor.score().trackCount() >= 1,
                    "importing a real .ptb from the powertabeditor repository must leave at least one track");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void importingTablEditThroughTheMenuReadsAMinimalTef3WithTheSameLayoutAsTheRealReader(@TempDir Path tempDir)
            throws Exception {
        byte[] bytes = TabEditMinimalFixture.oneTrackOneMeasureScore(
                "Test Song", 140, "Test Guitar", List.of(3, 5, 7, 8));
        Path path = tempDir.resolve("test.tef");
        Files.write(path, bytes);

        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenu importMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Importar");
            JMenuItem item = AuditSupport.findMenuItem(importMenu, "TablEdit…");
            assertNotNull(item, "could not find 'TablEdit…' inside Importar");

            withDialog(item::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                chooser.setSelectedFile(path.toFile());
                chooser.approveSelection();
            });

            assertEquals(1, editor.score().trackCount());
            assertEquals("Test Guitar", editor.score().track(0).name());
            assertEquals(140, editor.score().tempo());
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void importingGuitarProFromTheImportSubmenuUsesTheSameReaderAsOpen() throws Exception {
        Path path = repoFile("tabpro-format/src/test/resources/guitarpro/tabpro-features.gp5");

        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenu importMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Importar");
            JMenuItem item = AuditSupport.findMenuItem(importMenu, "Guitar Pro…");
            assertNotNull(item, "could not find 'Guitar Pro…' inside Importar");

            withDialog(item::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                chooser.setSelectedFile(path.toFile());
                chooser.approveSelection();
            });

            assertEquals(3, editor.score().trackCount());
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void exportingMidiThroughTheMenuWritesAFileThatMidiSystemReadsWithItsNotes(@TempDir Path tempDir) throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenu exportMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Exportar");
            JMenuItem item = AuditSupport.findMenuItem(exportMenu, "MIDI…");
            assertNotNull(item, "could not find 'MIDI…' inside Exportar");

            Path path = tempDir.resolve("exported.mid");
            withDialog(item::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                chooser.setSelectedFile(path.toFile());
                chooser.approveSelection();
            });

            assertTrue(Files.exists(path));
            Sequence sequence = MidiSystem.getSequence(path.toFile());
            assertTrue(hasNoteOn(sequence), "the exported MIDI must bring at least one real note");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void exportingWaveThroughTheMenuAsksForQualityAndWritesRealAudio(@TempDir Path tempDir) throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenu exportMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Exportar");
            JMenuItem item = AuditSupport.findMenuItem(exportMenu, "WAVE…");
            assertNotNull(item, "could not find 'WAVE…' inside Exportar");

            Path path = tempDir.resolve("exported.wav");
            withDialog(item::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                if (chooser != null) {
                    chooser.setSelectedFile(path.toFile());
                    chooser.approveSelection();
                    return;
                }
                String ok = UIManager.getString("OptionPane.okButtonText");
                JButton okButton = findButton(dialog, ok);
                assertNotNull(okButton, "could not find the real button to accept the WAVE quality");
                okButton.doClick();
            });

            assertTrue(Files.exists(path));
            try (AudioInputStream in = AudioSystem.getAudioInputStream(path.toFile())) {
                assertTrue(in.getFrameLength() > 0, "the exported WAVE must have real audio, not empty");
            }
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void exportingAsciiTablatureThroughTheMenuWritesTheActiveTrackAndReimportsWithTheSameNote(@TempDir Path tempDir)
            throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenu exportMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Exportar");
            JMenuItem item = AuditSupport.findMenuItem(exportMenu, "Tablatura ASCII…");
            assertNotNull(item, "could not find 'Tablatura ASCII…' inside Exportar");

            Path path = tempDir.resolve("exported.tab");
            withDialog(item::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                if (chooser != null) {
                    chooser.setSelectedFile(path.toFile());
                    chooser.approveSelection();
                    return;
                }
                findButton(dialog, "Exportar…").doClick();
                findButton(dialog, "Cerrar").doClick();
            });

            assertTrue(Files.exists(path));
            Score reimported = exchange.importAscii(path);
            assertTrue(containsFret(reimported, 3), "the exported ASCII tablature must bring back fret 3");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void exportingAsciiTablatureThroughTheMenuShowsTheNoteFretInThePreview() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenu exportMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Exportar");
            JMenuItem item = AuditSupport.findMenuItem(exportMenu, "Tablatura ASCII…");
            assertNotNull(item, "could not find 'Tablatura ASCII…' inside Exportar");

            withDialog(item::doClick, dialog -> {
                JTextArea preview = findComponent(dialog, JTextArea.class);
                assertNotNull(preview, "could not find the real ASCII export preview");
                assertTrue(preview.getText().contains("3"),
                        "the preview must show the real fret of the note: " + preview.getText());
                findButton(dialog, "Cerrar").doClick();
            });
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void exportingMusicXmlThroughTheMenuWritesAFileTheRealImporterReadsBack(@TempDir Path tempDir) throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenu exportMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Exportar");
            JMenuItem item = AuditSupport.findMenuItem(exportMenu, "MusicXML…");
            assertNotNull(item, "could not find 'MusicXML…' inside Exportar");

            Path path = tempDir.resolve("exported.musicxml");
            withDialog(item::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                chooser.setSelectedFile(path.toFile());
                chooser.approveSelection();
            });

            assertTrue(Files.exists(path));
            Score reimported = exchange.importMusicXml(path);
            assertEquals(1, reimported.trackCount());
            assertTrue(containsFret(reimported, 3), "the exported MusicXML must bring back fret 3");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void exportingGuitarPro4ThroughTheMenuWritesAFileTheRealGuitarProReaderRecognizes(@TempDir Path tempDir)
            throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenu exportMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Exportar");
            JMenuItem item = AuditSupport.findMenuItem(exportMenu, "Guitar Pro 4…");
            assertNotNull(item, "could not find 'Guitar Pro 4…' inside Exportar");

            Path path = tempDir.resolve("exported.gp4");
            withDialog(item::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                assertNotNull(chooser,
                        "with no losses to warn about, Exportar Guitar Pro 4 must go straight to the real chooser");
                chooser.setSelectedFile(path.toFile());
                chooser.approveSelection();
            });

            assertTrue(Files.exists(path));
            Score reread = new GuitarProFile(new TextsDefaultNames()).read(path);
            assertEquals(1, reread.trackCount());
            assertTrue(containsFret(reread, 3), "the exported .gp4 must bring back fret 3");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void exportingImageThroughTheMenuWritesARealPng(@TempDir Path tempDir) throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenu exportMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Exportar");
            JMenuItem item = AuditSupport.findMenuItem(exportMenu, "Imagen…");
            assertNotNull(item, "could not find 'Imagen…' inside Exportar");

            Path path = tempDir.resolve("exported.png");
            withDialog(item::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                chooser.setSelectedFile(path.toFile());
                chooser.approveSelection();
            });

            assertTrue(Files.exists(path));
            BufferedImage image = ImageIO.read(path.toFile());
            assertNotNull(image, "the exported PNG must be a real image, not garbage");
            assertTrue(image.getWidth() > 0 && image.getHeight() > 0);
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void exportingPdfThroughTheMenuWritesARealPdf(@TempDir Path tempDir) throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenu exportMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Exportar");
            JMenuItem item = AuditSupport.findMenuItem(exportMenu, "PDF…");
            assertNotNull(item, "could not find 'PDF…' inside Exportar");

            Path path = tempDir.resolve("exported.pdf");
            withDialog(item::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                chooser.setSelectedFile(path.toFile());
                chooser.approveSelection();
            });

            assertTrue(Files.exists(path));
            String content = new String(Files.readAllBytes(path), StandardCharsets.ISO_8859_1);
            assertTrue(content.startsWith("%PDF-"), "the exported PDF must start with the real header");
            assertTrue(content.contains("/Type /Catalog"));
            assertTrue(content.trim().endsWith("%%EOF"));
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    private static boolean containsFret(Score score, int fret) {
        for (int t = 0; t < score.trackCount(); t++) {
            Track track = score.track(t);
            for (int m = 0; m < track.measureCount(); m++) {
                for (var beat : track.measure(m).beats()) {
                    for (var note : beat.notes()) {
                        if (note.fret() == fret) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean hasNoteOn(Sequence sequence) {
        for (javax.sound.midi.Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                javax.sound.midi.MidiMessage message = track.get(i).getMessage();
                if (message instanceof ShortMessage shortMessage
                        && shortMessage.getCommand() == ShortMessage.NOTE_ON
                        && shortMessage.getData2() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Synthesizer systemSynthesizer() {
        try {
            return MidiSystem.getSynthesizer();
        } catch (MidiUnavailableException e) {
            throw new IllegalStateException(e);
        }
    }
}
