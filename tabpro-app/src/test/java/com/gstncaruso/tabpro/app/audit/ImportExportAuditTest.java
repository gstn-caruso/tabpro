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

@Tag("integracion")
@ResourceLock(AuditSupport.SWING_LOCK)
class ImportExportAuditTest {

    private final ScoreExchange exchange = new CombinedExchange(
            new NotationExchange(), new SoundExchange(new WaveRenderer(ImportExportAuditTest::systemSynthesizer)));

    @BeforeEach
    @AfterEach
    void limpiarArchivosRecientes() {
        java.util.prefs.Preferences.userRoot().node("com/gstncaruso/tabpro").remove("recentFiles");
    }

    private MainFrame newFrame(Editor editor) throws Exception {
        return AuditSupport.newFrame(editor, new JsonScoreFiles(), exchange);
    }

    @Test
    void abrirPorElMenuLeeUnArchivoTabproPropioIgualQueSeGuardo(@TempDir Path tempDir) throws Exception {
        Score original = Score.blank();
        Path path = tempDir.resolve("propia.tabpro");
        new JsonScoreFiles().save(original, path);

        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Abrir…");
            assertNotNull(item, "no encontre 'Abrir…' en el menu real");

            withDialog(item::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                assertNotNull(chooser, "no encontre el JFileChooser real");
                chooser.setSelectedFile(path.toFile());
                chooser.approveSelection();
            });

            assertEquals(original, editor.score(), "Abrir un .tabpro real tiene que dejar el modelo igual al archivo");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void abrirPorElMenuReconoceUnArchivoDeGuitarProSinPasarPorImportar() throws Exception {
        Path path = repoFile("tabpro-format/src/test/resources/guitarpro/tabpro-features.gp5");
        assertTrue(Files.exists(path), "no encontre el fixture real de Guitar Pro en tabpro-format");

        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Abrir…");

            withDialog(item::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                chooser.setSelectedFile(path.toFile());
                chooser.approveSelection();
            });

            assertEquals(3, editor.score().trackCount(), "el mismo Abrir tiene que reconocer un .gp5 real");
            assertEquals("Lead Guitar", editor.score().track(0).name());
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void guardarSinArchivoTodaviaAbreGuardarComoYLaSegundaVezSobreescribeSinPreguntar(@TempDir Path tempDir)
            throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem save = findMenuItem(frame.getJMenuBar(), "Guardar");
            assertNotNull(save, "no encontre 'Guardar' en el menu real");
            assertEquals(KeyStroke.getKeyStroke("ctrl S"), save.getAccelerator());

            Path path = tempDir.resolve("primera-vez.tabpro");
            withDialog(save::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                assertNotNull(chooser, "sin archivo todavia, Guardar tiene que abrir el chooser real de Guardar como");
                chooser.setSelectedFile(path.toFile());
                chooser.approveSelection();
            });

            assertTrue(Files.exists(path));
            assertEquals(editor.score(), new JsonScoreFiles().load(path));

            editor.setFret(5);
            SwingUtilities.invokeAndWait(save::doClick);

            assertEquals(
                    editor.score(), new JsonScoreFiles().load(path),
                    "con archivo ya elegido, Guardar tiene que sobreescribirlo sin volver a preguntar donde");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void archivoRecienGuardadoApareceEnAbrirRecienteYAlElegirloSeAbreDeVerdad(@TempDir Path tempDir) throws Exception {
        Path path = tempDir.resolve("reciente.tabpro");
        Editor firstEditor = editorWithANote();
        MainFrame firstFrame = newFrame(firstEditor);
        try {
            JMenuItem saveAs = findMenuItem(firstFrame.getJMenuBar(), "Guardar como…");
            assertNotNull(saveAs, "no encontre 'Guardar como…' en el menu real");

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
            assertNotNull(recent, "el archivo recien guardado tiene que aparecer en Archivo > Abrir reciente");

            SwingUtilities.invokeAndWait(recent::doClick);

            assertEquals(
                    new JsonScoreFiles().load(path), secondEditor.score(),
                    "elegir el archivo en Abrir reciente tiene que abrirlo de verdad");
        } finally {
            AuditSupport.dispose(secondFrame);
        }
    }

    @Test
    void importarMidiPorElMenuOfreceLasPistasRealesYElImportRapidoLasTraeAlModelo(@TempDir Path tempDir)
            throws Exception {
        Path midiPath = tempDir.resolve("ajeno.mid");
        new MidiScoreExporter().export(Score.blank(), midiPath);

        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenu importMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Importar");
            assertNotNull(importMenu, "no encontre el submenu 'Importar' real");
            JMenuItem item = AuditSupport.findMenuItem(importMenu, "MIDI…");
            assertNotNull(item, "no encontre 'MIDI…' dentro de Importar");

            withDialog(item::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                if (chooser != null) {
                    chooser.setSelectedFile(midiPath.toFile());
                    chooser.approveSelection();
                    return;
                }
                JList<?> trackList = findComponent(dialog, JList.class);
                assertNotNull(trackList, "no encontre la lista real de pistas MIDI");
                assertEquals(1, trackList.getModel().getSize(), "el .mid tiene una sola pista audible");
                trackList.setSelectedIndex(0);

                JButton quickImport = findButton(dialog, "Import rápido (reemplaza la partitura)");
                assertNotNull(quickImport, "no encontre el boton real de Import rapido");
                quickImport.doClick();

                findButton(dialog, "Cerrar").doClick();
            });

            assertEquals(1, editor.score().trackCount());
            assertEquals("Guitarra", editor.score().track(0).name(),
                    "el Import rapido real tiene que traer el nombre de pista del MIDI ajeno");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void importarTablaturaAsciiPorElMenuTraeLasNotasRealesALaPistaActiva() throws Exception {
        String tab = "|--5--0--|\n" + "|--------|\n".repeat(5);

        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenu importMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Importar");
            JMenuItem item = AuditSupport.findMenuItem(importMenu, "Tablatura ASCII…");
            assertNotNull(item, "no encontre 'Tablatura ASCII…' dentro de Importar");

            withDialog(item::doClick, dialog -> {
                JTextArea text = findComponent(dialog, JTextArea.class);
                assertNotNull(text, "no encontre la zona de texto real del import de ASCII");
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
    void importarMusicXmlPorElMenuLeeLaArmaduraYLasNotasRealesDelArchivo() throws Exception {
        Path path = repoFile("tabpro-format/src/test/resources/musicxml/armadura-en-fa.musicxml");
        assertTrue(Files.exists(path), "no encontre el fixture real de MusicXML en tabpro-format");

        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenu importMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Importar");
            JMenuItem item = AuditSupport.findMenuItem(importMenu, "MusicXML…");
            assertNotNull(item, "no encontre 'MusicXML…' dentro de Importar");

            withDialog(item::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                chooser.setSelectedFile(path.toFile());
                chooser.approveSelection();
            });

            assertEquals(-1, editor.score().attributesOf(0).keySignature().accidentals(),
                    "fa mayor son 1 bemol (fifths=-1)");
            Track track = editor.score().track(0);
            assertEquals(65, track.pitchOf(track.measure(0).beat(0).notes().get(0)).midiNumber(), "Fa4");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void importarPowerTabPorElMenuLeeUnArchivoRealDelRepositorioDePowerTabEditor() throws Exception {
        Path path = repoFile("tabpro-format/src/test/resources/powertab/guitars.ptb");
        assertTrue(Files.exists(path), "no encontre el fixture real de PowerTab en tabpro-format");

        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenu importMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Importar");
            JMenuItem item = AuditSupport.findMenuItem(importMenu, "PowerTab…");
            assertNotNull(item, "no encontre 'PowerTab…' dentro de Importar");

            withDialog(item::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                chooser.setSelectedFile(path.toFile());
                chooser.approveSelection();
            });

            assertTrue(editor.score().trackCount() >= 1,
                    "importar un .ptb real del repositorio de powertabeditor tiene que dejar al menos una pista");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void importarTablEditPorElMenuLeeUnTef3MinimoConElMismoLayoutQueElLectorReal(@TempDir Path tempDir)
            throws Exception {
        byte[] bytes = TabEditMinimalFixture.oneTrackOneMeasureScore(
                "Cancion de prueba", 140, "Guitarra de prueba", List.of(3, 5, 7, 8));
        Path path = tempDir.resolve("prueba.tef");
        Files.write(path, bytes);

        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenu importMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Importar");
            JMenuItem item = AuditSupport.findMenuItem(importMenu, "TablEdit…");
            assertNotNull(item, "no encontre 'TablEdit…' dentro de Importar");

            withDialog(item::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                chooser.setSelectedFile(path.toFile());
                chooser.approveSelection();
            });

            assertEquals(1, editor.score().trackCount());
            assertEquals("Guitarra de prueba", editor.score().track(0).name());
            assertEquals(140, editor.score().tempo());
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void importarGuitarProDesdeElSubmenuImportarUsaElMismoLectorQueAbrir() throws Exception {
        Path path = repoFile("tabpro-format/src/test/resources/guitarpro/tabpro-features.gp5");

        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenu importMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Importar");
            JMenuItem item = AuditSupport.findMenuItem(importMenu, "Guitar Pro…");
            assertNotNull(item, "no encontre 'Guitar Pro…' dentro de Importar");

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
    void exportarMidiPorElMenuEscribeUnArchivoQueMidiSystemLeeConSusNotas(@TempDir Path tempDir) throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenu exportMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Exportar");
            JMenuItem item = AuditSupport.findMenuItem(exportMenu, "MIDI…");
            assertNotNull(item, "no encontre 'MIDI…' dentro de Exportar");

            Path path = tempDir.resolve("exportado.mid");
            withDialog(item::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                chooser.setSelectedFile(path.toFile());
                chooser.approveSelection();
            });

            assertTrue(Files.exists(path));
            Sequence sequence = MidiSystem.getSequence(path.toFile());
            assertTrue(hasNoteOn(sequence), "el MIDI exportado tiene que traer al menos una nota real");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void exportarWavePorElMenuPreguntaLaCalidadYEscribeUnAudioReal(@TempDir Path tempDir) throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenu exportMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Exportar");
            JMenuItem item = AuditSupport.findMenuItem(exportMenu, "WAVE…");
            assertNotNull(item, "no encontre 'WAVE…' dentro de Exportar");

            Path path = tempDir.resolve("exportado.wav");
            withDialog(item::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                if (chooser != null) {
                    chooser.setSelectedFile(path.toFile());
                    chooser.approveSelection();
                    return;
                }
                String ok = UIManager.getString("OptionPane.okButtonText");
                JButton okButton = findButton(dialog, ok);
                assertNotNull(okButton, "no encontre el boton real de aceptar la calidad de WAVE");
                okButton.doClick();
            });

            assertTrue(Files.exists(path));
            try (AudioInputStream in = AudioSystem.getAudioInputStream(path.toFile())) {
                assertTrue(in.getFrameLength() > 0, "el WAVE exportado tiene que tener audio real, no vacio");
            }
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void exportarTablaturaAsciiPorElMenuEscribeLaPistaActivaYSeReimportaConLaMismaNota(@TempDir Path tempDir)
            throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenu exportMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Exportar");
            JMenuItem item = AuditSupport.findMenuItem(exportMenu, "Tablatura ASCII…");
            assertNotNull(item, "no encontre 'Tablatura ASCII…' dentro de Exportar");

            Path path = tempDir.resolve("exportada.tab");
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
            assertTrue(containsFret(reimported, 3), "la tablatura ASCII exportada tiene que traer de vuelta el traste 3");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void exportarTablaturaAsciiPorElMenuMuestraElTrasteDeLaNotaEnLaVistaPrevia() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenu exportMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Exportar");
            JMenuItem item = AuditSupport.findMenuItem(exportMenu, "Tablatura ASCII…");
            assertNotNull(item, "no encontre 'Tablatura ASCII…' dentro de Exportar");

            withDialog(item::doClick, dialog -> {
                JTextArea preview = findComponent(dialog, JTextArea.class);
                assertNotNull(preview, "no encontre la vista previa real del export de ASCII");
                assertTrue(preview.getText().contains("3"),
                        "la vista previa tiene que mostrar el traste real de la nota: " + preview.getText());
                findButton(dialog, "Cerrar").doClick();
            });
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void exportarMusicXmlPorElMenuEscribeUnArchivoQueElImportadorRealLeeIgual(@TempDir Path tempDir) throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenu exportMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Exportar");
            JMenuItem item = AuditSupport.findMenuItem(exportMenu, "MusicXML…");
            assertNotNull(item, "no encontre 'MusicXML…' dentro de Exportar");

            Path path = tempDir.resolve("exportada.musicxml");
            withDialog(item::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                chooser.setSelectedFile(path.toFile());
                chooser.approveSelection();
            });

            assertTrue(Files.exists(path));
            Score reimported = exchange.importMusicXml(path);
            assertEquals(1, reimported.trackCount());
            assertTrue(containsFret(reimported, 3), "el MusicXML exportado tiene que traer de vuelta el traste 3");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void exportarGuitarPro4PorElMenuEscribeUnArchivoQueElLectorRealDeGuitarProReconoce(@TempDir Path tempDir)
            throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenu exportMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Exportar");
            JMenuItem item = AuditSupport.findMenuItem(exportMenu, "Guitar Pro 4…");
            assertNotNull(item, "no encontre 'Guitar Pro 4…' dentro de Exportar");

            Path path = tempDir.resolve("exportada.gp4");
            withDialog(item::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                assertNotNull(chooser,
                        "sin perdidas que avisar, Exportar Guitar Pro 4 tiene que ir directo al chooser real");
                chooser.setSelectedFile(path.toFile());
                chooser.approveSelection();
            });

            assertTrue(Files.exists(path));
            Score reread = new GuitarProFile().read(path);
            assertEquals(1, reread.trackCount());
            assertTrue(containsFret(reread, 3), "el .gp4 exportado tiene que traer de vuelta el traste 3");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void exportarImagenPorElMenuEscribeUnPngReal(@TempDir Path tempDir) throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenu exportMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Exportar");
            JMenuItem item = AuditSupport.findMenuItem(exportMenu, "Imagen…");
            assertNotNull(item, "no encontre 'Imagen…' dentro de Exportar");

            Path path = tempDir.resolve("exportada.png");
            withDialog(item::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                chooser.setSelectedFile(path.toFile());
                chooser.approveSelection();
            });

            assertTrue(Files.exists(path));
            BufferedImage image = ImageIO.read(path.toFile());
            assertNotNull(image, "el PNG exportado tiene que ser una imagen real, no basura");
            assertTrue(image.getWidth() > 0 && image.getHeight() > 0);
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void exportarPdfPorElMenuEscribeUnPdfReal(@TempDir Path tempDir) throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenu exportMenu = (JMenu) findMenuItem(frame.getJMenuBar(), "Exportar");
            JMenuItem item = AuditSupport.findMenuItem(exportMenu, "PDF…");
            assertNotNull(item, "no encontre 'PDF…' dentro de Exportar");

            Path path = tempDir.resolve("exportada.pdf");
            withDialog(item::doClick, dialog -> {
                JFileChooser chooser = findComponent(dialog, JFileChooser.class);
                chooser.setSelectedFile(path.toFile());
                chooser.approveSelection();
            });

            assertTrue(Files.exists(path));
            String content = new String(Files.readAllBytes(path), StandardCharsets.ISO_8859_1);
            assertTrue(content.startsWith("%PDF-"), "el PDF exportado tiene que empezar con la cabecera real");
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
