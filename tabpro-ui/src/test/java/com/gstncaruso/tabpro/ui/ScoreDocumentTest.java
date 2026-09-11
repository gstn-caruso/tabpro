package com.gstncaruso.tabpro.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFiles;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.ui.dialogs.info.DefaultScoreProperties;
import com.gstncaruso.tabpro.ui.dialogs.info.NewScoreDefaults;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@ResourceLock(RealPreferencesTests.SHARED_PREFERENCES_FILESYSTEM_NODE_LOCK)
class ScoreDocumentTest {

    private static final String REAL_RECOVERY_FILE_LOCK = "tabpro-real-recovery-file";

    private final List<java.util.prefs.Preferences> scratchNodes = new ArrayList<>();

    @AfterEach
    void clearsTheScratchNodes() throws java.util.prefs.BackingStoreException {
        AwaitEdt.flush();
        for (java.util.prefs.Preferences node : scratchNodes) {
            node.removeNode();
        }
    }

    @Test
    void startsWithABlankScoreAndNoPath() {
        ScoreDocument document = new ScoreDocument(new Editor(Score.blank()), new FakeScoreFiles(), testPreferences());

        assertTrue(document.path().isEmpty());
    }

    @Test
    void theRecoveryFileNameIsALanguageNeutralIdentifier() {
        ScoreDocument document = new ScoreDocument(new Editor(Score.blank()), new FakeScoreFiles(), testPreferences());

        assertEquals("tabpro-recovery" + ScoreDocument.EXTENSION, document.recoveryFile().getFileName().toString());
    }

    @Test
    @ResourceLock(REAL_RECOVERY_FILE_LOCK)
    void pendingRecoveryFallsBackToTheLegacyFileNameWhenTheNewOneIsAbsent() throws IOException {
        deleteRealRecoveryFiles();
        Path legacyRecovery = Path.of(System.getProperty("java.io.tmpdir"), "tabpro-recuperación" + ScoreDocument.EXTENSION);
        Files.createFile(legacyRecovery);
        ScoreDocument document = new ScoreDocument(new Editor(Score.blank()), new FakeScoreFiles(), testPreferences());

        try {
            assertEquals(Optional.of(legacyRecovery), document.pendingRecovery());
        } finally {
            deleteRealRecoveryFiles();
        }
    }

    @Test
    @ResourceLock(REAL_RECOVERY_FILE_LOCK)
    void discardingALegacyRecoveryRemovesTheLegacyFile() throws IOException {
        deleteRealRecoveryFiles();
        Path legacyRecovery = Path.of(System.getProperty("java.io.tmpdir"), "tabpro-recuperación" + ScoreDocument.EXTENSION);
        Files.createFile(legacyRecovery);
        ScoreDocument document = new ScoreDocument(new Editor(Score.blank()), new FakeScoreFiles(), testPreferences());

        try {
            document.discardRecovery();

            assertFalse(Files.exists(legacyRecovery));
        } finally {
            deleteRealRecoveryFiles();
        }
    }

    @Test
    void describesAnUntitledDocument() {
        ScoreDocument document = new ScoreDocument(new Editor(Score.blank()), new FakeScoreFiles(), testPreferences());

        assertEquals(ScoreDocument.untitled(), document.displayName());
    }

    @Test
    void saveWithoutAPathAsksForOne() {
        FakeScoreFiles files = new FakeScoreFiles();
        ScoreDocument document = new ScoreDocument(new Editor(Score.blank()), files, testPreferences());

        assertFalse(document.save());
        assertEquals(0, files.saveCount);
    }

    @Test
    void saveAsRemembersThePath() {
        FakeScoreFiles files = new FakeScoreFiles();
        ScoreDocument document = new ScoreDocument(new Editor(Score.blank()), files, testPreferences());
        Path path = Path.of("song.tabpro");

        document.saveAs(path);

        assertEquals(Optional.of(path), document.path());
        assertEquals(1, files.saveCount);
    }

    @Test
    void saveReusesTheRememberedPath() {
        FakeScoreFiles files = new FakeScoreFiles();
        ScoreDocument document = new ScoreDocument(new Editor(Score.blank()), files, testPreferences());
        Path path = Path.of("song.tabpro");
        document.saveAs(path);

        boolean saved = document.save();

        assertTrue(saved);
        assertEquals(2, files.saveCount);
    }

    @Test
    void openReplacesTheScoreAndRemembersThePath() {
        FakeScoreFiles files = new FakeScoreFiles();
        Path path = Path.of("song.tabpro");
        Score savedScore = Score.blank().withTitle("Saved song");
        files.scores.put(path, savedScore);
        Editor editor = new Editor(Score.blank());
        ScoreDocument document = new ScoreDocument(editor, files, testPreferences());

        document.open(path);

        assertEquals(savedScore, editor.score());
        assertEquals(Optional.of(path), document.path());
    }

    @Test
    void newForgetsThePath() {
        FakeScoreFiles files = new FakeScoreFiles();
        Editor editor = new Editor(Score.blank());
        ScoreDocument document = new ScoreDocument(editor, files, testPreferences());
        document.saveAs(Path.of("song.tabpro"));

        document.newScore();

        assertTrue(document.path().isEmpty());
        assertEquals(Score.blank(), editor.score());
    }

    @Test
    void newScoreUsesWhatIsSavedInThePropertiesByDefault() {
        java.util.prefs.Preferences defaultsNode = java.util.prefs.Preferences.userRoot()
                .node("tabpro-test/" + getClass().getSimpleName() + "/" + java.util.UUID.randomUUID());
        scratchNodes.add(defaultsNode);
        DefaultScoreProperties defaultProperties = new DefaultScoreProperties(defaultsNode);
        defaultProperties.save(new NewScoreDefaults(
                90, new TimeSignature(3, 4), KeySignature.cMajor(), "", ""));
        Editor editor = new Editor(Score.blank());
        ScoreDocument document = new ScoreDocument(
                editor, new FakeScoreFiles(), testPreferences(), () -> defaultProperties.get().newScore());

        document.newScore();

        assertEquals(90, editor.score().tempo());
        assertEquals(new TimeSignature(3, 4), editor.score().timeSignatureOf(0));
    }

    @Test
    void describesTheDocumentByItsFileName() {
        FakeScoreFiles files = new FakeScoreFiles();
        ScoreDocument document = new ScoreDocument(new Editor(Score.blank()), files, testPreferences());

        document.saveAs(Path.of("folder", "song.tabpro"));

        assertEquals("song.tabpro", document.displayName());
    }

    @Test
    void aFailedOpenKeepsThePreviousPath() {
        FakeScoreFiles files = new FakeScoreFiles();
        Path savedPath = Path.of("song.tabpro");
        Editor editor = new Editor(Score.blank());
        ScoreDocument document = new ScoreDocument(editor, files, testPreferences());
        document.saveAs(savedPath);

        assertThrows(ScoreFileException.class, () -> document.open(Path.of("does-not-exist.tabpro")));

        assertEquals(Optional.of(savedPath), document.path());
        assertEquals(Score.blank(), editor.score());
    }


    @Test
    void aFreshDocumentHasNothingToSave() {
        Editor editor = new Editor(Score.blank());
        ScoreDocument document = new ScoreDocument(editor, new FakeScoreFiles(), testPreferences());

        assertFalse(document.hasUnsavedChanges());
    }

    @Test
    void writingANoteLeavesUnsavedChanges() {
        Editor editor = new Editor(Score.blank());
        ScoreDocument document = new ScoreDocument(editor, new FakeScoreFiles(), testPreferences());

        editor.setFret(5);

        assertTrue(document.hasUnsavedChanges());
    }

    @Test
    void savingClearsTheUnsavedChanges() {
        Editor editor = new Editor(Score.blank());
        ScoreDocument document = new ScoreDocument(editor, new FakeScoreFiles(), testPreferences());
        document.saveAs(Path.of("/tmp/test.tabpro"));

        editor.setFret(5);
        document.save();

        assertFalse(document.hasUnsavedChanges());
    }

    @Test
    void theWindowTitleShowsTheFileTheChangesAndTheScore() {
        Editor editor = new Editor(Score.blank().withTitle("My song"));
        ScoreDocument document = new ScoreDocument(editor, new FakeScoreFiles(), testPreferences());

        editor.setFret(5);

        assertTrue(document.windowTitle().startsWith(ScoreDocument.untitled() + " *"));
        assertTrue(document.windowTitle().contains("My song"));
    }

    @Test
    void theWindowTitleOfAnUntitledScoreWithoutAnArtistUsesUntitledAsItsHeading() {
        ScoreDocument document = new ScoreDocument(new Editor(Score.blank()), new FakeScoreFiles(), testPreferences());

        assertEquals("Sin título — Sin título — tabpro", document.windowTitle());
    }

    @Test
    void theWindowTitleHeadingJoinsTheTitleAndTheArtistWithADash() {
        Score score = Score.blank().withInfo(ScoreInfo.titled("Sultans of Swing").withArtist("Dire Straits"));
        ScoreDocument document = new ScoreDocument(new Editor(score), new FakeScoreFiles(), testPreferences());

        assertEquals("Sin título — Sultans of Swing - Dire Straits — tabpro", document.windowTitle());
    }

    @Test
    void theWindowTitleHeadingFallsBackToTheArtistWhenThereIsNoTitle() {
        Score score = Score.blank().withInfo(ScoreInfo.empty().withArtist("Dire Straits"));
        ScoreDocument document = new ScoreDocument(new Editor(score), new FakeScoreFiles(), testPreferences());

        assertEquals("Sin título — Dire Straits — tabpro", document.windowTitle());
    }

    @Test
    void openingAFileRemembersItAmongTheRecentOnes() {
        Preferences preferences = testPreferences();
        Editor editor = new Editor(Score.blank());
        FakeScoreFiles files = new FakeScoreFiles();
        Path path = Path.of("/tmp/test.tabpro");
        files.scores.put(path, Score.blank());
        ScoreDocument document = new ScoreDocument(editor, files, preferences);

        document.open(path);

        assertEquals(java.util.List.of(path), preferences.recentFiles());
    }

    @Test
    void exposesTheRecentFilesToOfferThemInTheFileMenu() {
        Preferences preferences = testPreferences();
        Editor editor = new Editor(Score.blank());
        FakeScoreFiles files = new FakeScoreFiles();
        Path path = Path.of("/tmp/test.tabpro");
        files.scores.put(path, Score.blank());
        ScoreDocument document = new ScoreDocument(editor, files, preferences);

        document.open(path);

        assertEquals(java.util.List.of(path), document.recentFiles());
    }

    @Test
    void anImportedScoreHasNoFileOfItsOwn() {
        Editor editor = new Editor(Score.blank());
        ScoreDocument document = new ScoreDocument(editor, new FakeScoreFiles(), testPreferences());

        document.adopt(Score.blank().withTitle("Imported"));

        assertTrue(document.path().isEmpty());
        assertTrue(document.hasUnsavedChanges());
    }

    private Preferences testPreferences() {
        java.util.prefs.Preferences node = java.util.prefs.Preferences.userRoot()
                .node("com/gstncaruso/tabpro/test/" + java.util.UUID.randomUUID());
        scratchNodes.add(node);
        return new Preferences(node);
    }

    private void deleteRealRecoveryFiles() throws IOException {
        Files.deleteIfExists(Path.of(System.getProperty("java.io.tmpdir"), "tabpro-recovery" + ScoreDocument.EXTENSION));
        Files.deleteIfExists(Path.of(System.getProperty("java.io.tmpdir"), "tabpro-recuperación" + ScoreDocument.EXTENSION));
    }

    private static final class FakeScoreFiles implements ScoreFiles {

        private final Map<Path, Score> scores = new HashMap<>();
        private int saveCount = 0;

        @Override
        public Score load(Path path) {
            Score score = scores.get(path);
            if (score == null) {
                throw ScoreFileException.cannotRead(path, new NoSuchFileException(path.toString()));
            }
            return score;
        }

        @Override
        public void save(Score score, Path path) {
            saveCount++;
            scores.put(path, score);
        }
    }
}
