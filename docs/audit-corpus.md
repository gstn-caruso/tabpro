# Corpus audit — tabpro against real files

The previous audits (`audit-manual.md`, `audit-real-use.md`) checked the code against the manual
and against the real user path, but with very few genuinely foreign files: `tabpro-format` ships
10 of its own Guitar Pro fixtures, 17 PowerTab and 5 MusicXML. Since the last time it was tested
against an external corpus (PyGuitarPro, 60/61 files), a lot changed in how the score is painted
(Bravura typeface, glyphs, tempo, markers, selection), the toolbars, the mixer and the global
view. This audit downloads the **PyGuitarPro** test corpus again (LGPL, which is why it is never
committed: it lives in the session's scratchpad) and adds it to the project's own fixtures, to
find what breaks with real files: exceptions, hangs, pages that fail to render, exports that
fail to reopen.

Audited code: `main` branch, version 0.49.1. Corpus: 49 Guitar Pro (39 from PyGuitarPro + 10 of
our own, `.gp3`/`.gp4`/`.gp5`), 17 PowerTab (our own) and 5 MusicXML (our own) — 71 files.

## Method

1. **Automated pass, no window** (`CorpusAuditTest`, disposable, deleted before this commit): for
   each file, inside a 60 s `assertTimeoutPreemptively` —
   - opens it through the real `ScoreExchange` (`CombinedExchange(NotationExchange,
     SoundExchange)`, the same one `Archivo > Importar` uses): `importGuitarPro` /
     `importPowerTab` / `importMusicXml` depending on the source folder;
   - renders every page in Page mode (`ScoreSheets.renderPages`) and once in Parchment mode
     (`ScoreSheets.render(..., ViewMode.PARCHMENT, ...)`), off screen;
   - exports it to `.gp4`, MIDI and MusicXML into a temp directory and reopens each export with
     the matching reader;
   - saves it as `.tabpro` (`JsonScoreFiles`) and reloads it, comparing the `Score` with `equals`.
   - Run: `mvn -B -pl tabpro-tests -am test -Dtests.excluded.groups=ninguno
     -Dgroups=integration -Dtest=CorpusAuditTest` — **71/71 cases executed in 2.1 s**
     (not counting Maven/JVM startup).

2. **Pass with a real window** (`GuiSmokeAuditTest`, disposable, deleted before this commit):
   `Theme.install()` + `AuditSupport.newFrame` with a `RecordingPlayer` (it does not stop on its
   own) and its own `EventQueue` pushed onto AWT's real queue (to catch exceptions that escape
   painting, not just the ones the test itself throws), over five varied files (multitrack +
   many measures, percussion + second voice + repeat with alternate endings, two voices, repeats,
   directions): it walks the four views in the View menu, moves the cursor to the last measure
   and plays for two seconds. **Only 1 of the 5 cases could be run** — see Finding 2.

## Table — automated pass (71/71 files)

All boolean columns are about the same already-opened file; `-1`/`false` on a file that failed
to open means the following steps were not even attempted. `tabproEqual` compares the reloaded
`Score` with `equals()`. `ms` is the full pipeline time per file (step 1).

| file | format | opens | tracks | measures | renders | gp4 | midi | musicxml | tabpro | tabproEqual | ms |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 001_Funky_Guy.gp5 | gp5 | true | 2 | 2 | true | true | true | true | true | true | 194 |
| Accent-force.gp3 | gp3 | true | 1 | 1 | true | true | true | true | true | true | 13 |
| Accent-force.gp4 | gp4 | true | 1 | 1 | true | true | true | true | true | true | 12 |
| Chord Old Format.gp3 | gp3 | true | 1 | 1 | true | true | true | true | true | true | 17 |
| Chords.gp3 | gp3 | true | 1 | 8 | true | true | true | true | true | true | 22 |
| Chords.gp4 | gp4 | true | 1 | 8 | true | true | true | true | true | true | 17 |
| Chords.gp5 | gp5 | true | 1 | 8 | true | true | true | true | true | true | 16 |
| Clef.gp5 | gp5 | true | 2 | 1 | true | true | true | true | true | true | 16 |
| Demo v5.gp5 | gp5 | true | 5 | 49 | true | true | true | true | true | true | 1132 |
| Directions.gp5 | gp5 | true | 1 | 19 | true | true | true | true | true | true | 19 |
| Duration.gp3 | gp3 | true | 1 | 6 | true | true | true | true | true | true | 9 |
| Effects.gp3 | gp3 | true | 1 | 14 | true | true | true | true | true | true | 15 |
| Effects.gp4 | gp4 | true | 1 | 14 | true | true | true | true | true | true | 14 |
| Effects.gp5 | gp5 | true | 1 | 14 | true | true | true | true | true | true | 13 |
| Harmonics.gp3 | gp3 | true | 1 | 2 | true | true | true | true | true | true | 6 |
| Harmonics.gp4 | gp4 | true | 1 | 1 | true | true | true | true | true | true | 6 |
| Harmonics.gp5 | gp5 | true | 1 | 1 | true | true | true | true | true | true | 5 |
| Key.gp4 | gp4 | true | 1 | 2 | true | true | true | true | true | true | 5 |
| Key.gp5 | gp5 | true | 1 | 1 | true | true | true | true | true | true | 6 |
| Measure Header.gp3 | gp3 | true | 1 | 3 | true | true | true | true | true | true | 6 |
| Measure Header.gp4 | gp4 | true | 1 | 3 | true | true | true | true | true | true | 6 |
| Measure Header.gp5 | gp5 | true | 1 | 3 | true | true | true | true | true | true | 6 |
| No Wah.gp5 | gp5 | true | 1 | 1 | true | true | true | true | true | true | 5 |
| RSE.gp5 | gp5 | true | 2 | 2 | true | true | true | true | true | true | 6 |
| Repeat.gp4 | gp4 | true | 1 | 8 | true | true | true | true | true | true | 6 |
| Repeat.gp5 | gp5 | true | 1 | 8 | true | true | true | true | true | true | 7 |
| Slides.gp4 | gp4 | true | 1 | 2 | true | true | true | true | true | true | 6 |
| Slides.gp5 | gp5 | true | 1 | 3 | true | true | true | true | true | true | 6 |
| Strokes.gp4 | gp4 | true | 1 | 6 | true | true | true | true | true | true | 9 |
| Strokes.gp5 | gp5 | true | 1 | 6 | true | true | true | true | true | true | 9 |
| Tie.gp5 | gp5 | true | 1 | 4 | true | true | true | true | true | true | 7 |
| Unknown Chord Extension.gp5 | gp5 | true | 1 | 1 | true | true | true | true | true | true | 6 |
| Unknown-m.gp5 | gp5 | true | 1 | 5 | true | true | true | true | true | true | 11 |
| Unknown.gp5 | gp5 | true | 1 | 5 | true | true | true | true | true | true | 11 |
| Vibrato.gp4 | gp4 | true | 1 | 4 | true | true | true | true | true | true | 6 |
| Voices.gp5 | gp5 | true | 1 | 3 | true | true | true | true | true | true | 6 |
| Wah-m.gp5 | gp5 | true | 1 | 3 | true | true | true | true | true | true | 7 |
| Wah.gp5 | gp5 | true | 1 | 2 | true | true | true | true | true | true | 6 |
| chord_without_notes.gp5 | gp5 | true | 1 | 1 | true | true | true | true | true | true | 5 |
| tabpro-effects2.gp3 | gp3 | true | 1 | 3 | true | true | true | true | true | true | 6 |
| tabpro-effects2.gp4 | gp4 | true | 1 | 3 | true | true | true | true | true | true | 6 |
| tabpro-effects2.gp5 | gp5 | true | 1 | 3 | true | true | true | true | true | true | 7 |
| tabpro-features-v5.00.gp5 | gp5 | true | 3 | 4 | true | true | true | true | true | true | 14 |
| tabpro-features.gp3 | gp3 | true | 3 | 4 | true | true | true | true | true | true | 13 |
| tabpro-features.gp4 | gp4 | true | 3 | 4 | true | true | true | true | true | true | 13 |
| tabpro-features.gp5 | gp5 | true | 3 | 4 | true | true | true | true | true | true | 20 |
| tabpro-synthetic.gp3 | gp3 | true | 1 | 2 | true | true | true | true | true | true | 5 |
| tabpro-synthetic.gp4 | gp4 | true | 1 | 2 | true | true | true | true | true | true | 5 |
| tabpro-synthetic.gp5 | gp5 | true | 1 | 2 | true | true | true | true | true | true | 5 |
| alternate_endings.ptb | powertab | true | 2 | 2 | true | true | true | true | true | true | 8 |
| barlines.ptb | powertab | true | 2 | 2 | true | true | true | true | true | true | 6 |
| bends.ptb | powertab | true | 2 | 1 | true | true | true | true | true | true | 7 |
| chord_diagrams.ptb | powertab | true | 2 | 1 | true | true | true | true | true | true | 5 |
| chordtext.ptb | powertab | true | 2 | 1 | true | true | true | true | true | true | 5 |
| directions.ptb | powertab | true | 2 | 1 | true | true | true | true | true | true | 5 |
| floating_text.ptb | powertab | true | 2 | 3 | true | true | true | true | true | true | 5 |
| **guitar_ins.ptb** | powertab | **false** | -1 | -1 | false | false | false | false | false | false | 0 |
| guitars.ptb | powertab | true | 2 | 1 | true | true | true | true | true | true | 5 |
| **merge_multibar_rests.ptb** | powertab | **false** | -1 | -1 | false | false | false | false | false | false | 0 |
| notes.ptb | powertab | true | 2 | 1 | true | true | true | true | true | true | 5 |
| **positions.ptb** | powertab | **false** | -1 | -1 | false | false | false | false | false | false | 0 |
| song_header.ptb | powertab | true | 2 | 1 | true | true | true | true | true | true | 5 |
| staves.ptb | powertab | true | 3 | 1 | true | true | true | true | true | true | 6 |
| tempo_markers.ptb | powertab | true | 2 | 1 | true | true | true | true | true | true | 5 |
| tremolo_bars.ptb | powertab | true | 2 | 2 | true | true | true | true | true | true | 6 |
| volume_swells.ptb | powertab | true | 2 | 2 | true | true | true | true | true | true | 6 |
| armadura-en-fa.musicxml | musicxml | true | 1 | 1 | true | true | true | true | true | true | 6 |
| ligadura-entre-compases.musicxml | musicxml | true | 1 | 2 | true | true | true | true | true | true | 6 |
| silencio-de-compas-completo.musicxml | musicxml | true | 1 | 2 | true | true | true | true | true | true | 5 |
| tablatura-en-drop-d.musicxml | musicxml | true | 1 | 1 | true | true | true | true | true | true | 6 |
| tresillo-de-corcheas.musicxml | musicxml | true | 1 | 1 | true | true | true | true | true | true | 6 |

Summary: **49/49 Guitar Pro** and **5/5 MusicXML** pass all five steps in full. **14/17
PowerTab** pass; the other 3 are Finding 1. None of the 68 files that opened ran into a problem
when rendering, exporting/reopening GP4, MIDI or MusicXML, or on the round trip to `.tabpro`.

## Findings

### Finding 1 — Three `.ptb` files fail to open (already-known limitation, not a new bug)

`guitar_ins.ptb`, `merge_multibar_rests.ptb` and `positions.ptb` (fixtures of our own in
`tabpro-format/src/test/resources/powertab/`) throw `ScoreFileException` on open:

```
com.gstncaruso.tabpro.core.files.ScoreFileException: esta partitura reasigna el pentagrama 0 a
otra guitarra a mitad de la pieza, algo que todavia no soportamos
    at com.gstncaruso.tabpro.format.powertab.PowerTabFile.resolveGuitarPerStaff(PowerTabFile.java:169)
    ... (guitar_ins.ptb)

com.gstncaruso.tabpro.core.files.ScoreFileException: esta partitura usa un silencio de varios
compases comprimido (multibar rest), que todavia no soportamos
    at com.gstncaruso.tabpro.format.powertab.PowerTabFile.lastPositionUsedIn(PowerTabFile.java:344)
    ... (merge_multibar_rests.ptb y positions.ptb)
```

Not a new finding: `PowerTabFileTest` (`tabpro-format`) already has
`aMultibarRestIsReportedInsteadOfGuessed` and `aGuitarReassignmentIsReportedInsteadOfGuessed`,
which check exactly this — the reader reports instead of guessing, on purpose. It is listed here
anyway because it is what happens to a real user who tries to open these three files: an error
message comes up, not a hang or a corrupted read. No new fixture is needed.

### Finding 2 — `IllegalArgumentException` when moving the cursor after switching views, and the whole run hangs

**File:** `Directions.gp5` (PyGuitarPro; 1 track, 19 measures, with Coda/Segno symbols).
**Step:** `GuiSmokeAuditTest` — with the real window visible, after walking Parchment → Vertical
screen → Horizontal screen → **Page mode** (View menu), `editor.moveToLastMeasure()` to go to the
last measure fires, **synchronously on the same thread that called it** (not the EDT):
`Editor.moveCursor` → `Editor.notifyListeners` → `ScoreCanvas.editorChanged` (line 406) →
`JViewport.scrollRectToVisible` → it tries to create a **0×0-pixel** double-buffering buffer and
blows up:

```
java.lang.IllegalArgumentException: Width (0) and height (0) cannot be <= 0
    at java.desktop/sun.awt.image.SunVolatileImage.<init>(SunVolatileImage.java:74)
    at java.desktop/java.awt.GraphicsConfiguration.createCompatibleVolatileImage(GraphicsConfiguration.java:305)
    at java.desktop/javax.swing.RepaintManager.getVolatileOffscreenBuffer(RepaintManager.java:1054)
    at java.desktop/javax.swing.RepaintManager$PaintManager.paint(RepaintManager.java:1499)
    at java.desktop/javax.swing.JViewport.windowBlitPaint(JViewport.java:1684)
    at java.desktop/javax.swing.JViewport.setViewPosition(JViewport.java:1219)
    at java.desktop/javax.swing.JViewport.scrollRectToVisible(JViewport.java:445)
    at java.desktop/javax.swing.JComponent.scrollRectToVisible(JComponent.java:3195)
    at com.gstncaruso.tabpro.ui.score.ScoreCanvas.editorChanged(ScoreCanvas.java:406)
    at com.gstncaruso.tabpro.core.editing.Editor.notifyListeners(Editor.java:1161)
    at com.gstncaruso.tabpro.core.editing.Editor.moveCursor(Editor.java:1156)
    at com.gstncaruso.tabpro.core.editing.Editor.moveToLastMeasure(Editor.java:710)
```

**Suspected cause:** `com.gstncaruso.tabpro.ui.score.ScoreCanvas.java:406` (the
`scrollRectToVisible` call does not guard against a viewport with size 0 — plausible right after
a view-mode switch, while the layout has not yet settled on the real size). One minor caveat: in
the harness, `moveToLastMeasure()` was called directly from the test thread instead of through a
real dispatched key (which would go through the EDT) — the finding may be amplified by that, but
the unguarded `scrollRectToVisible` against a zero size is real and worth shielding anyway.

**Aggravating factor, same mechanism documented in `docs/audit-real-use.md` (note about
`withDialog`/EDT):** after this exception, the whole Maven fork hung (it did not finish on its
own; it had to be killed with an external `kill` after ~20 minutes: "*The forked VM terminated
without properly saying goodbye*", exit 143). A second run, in a fresh JVM and **excluding the
case that had already failed** (only the other 4 files: `Demo v5.gp5`, `tabpro-features.gp5`,
`Voices.gp5`, `Repeat.gp5`), **also hung** and was killed with `timeout 300` + an external `kill`.
Since neither run wrote a Surefire report for this class (`tabpro-tests/target/surefire-reports/`
only has the one for `CorpusAuditTest`), it is not known whether the hang was the same file/step
or a different one: `assertTimeoutPreemptively` is no use here — if the block is a real AWT/Swing
wait (or lower down, an Xlib one) and not just a slow thread, no JUnit `Timeout` mechanism can cut
it off from inside the same JVM.
**Only 1 of the 5 files chosen for the real window could be audited**; the other four remain
pending a run with an external per-process timeout (shell `timeout`) around each file separately,
not around the whole class.

**Estimated size of the fix:** contained — a guard in `ScoreCanvas.editorChanged` (or at the
point where it builds the rectangle for `scrollRectToVisible`) that skips the scroll request if
the viewport is still 0×0. The hang itself (the aggravating factor) is more expensive to
diagnose: it needs to be reproduced with a thread dump (`jstack`) at the exact moment, not with
`assertTimeoutPreemptively`.

**Closed in `fix/las-notificaciones-del-editor-llegan-por-el-edt`:**
`ScoreCanvas.editorChanged` now skips `scrollRectToVisible` if the cursor rectangle or
`getVisibleRect()` are empty (viewport still 0×0), with a test (`ScoreCanvasTest`, tested with the
mock that reproduced the `IllegalArgumentException` above: without the guard, the real
`JViewport` ended up with a nonsensical scroll position instead of throwing in a headless test —
the `IllegalArgumentException` itself only shows up with a real window, because `RepaintManager`
only attempts the double-buffering buffer when the component is *showing*). As defense in depth,
`EdtEditorListener` was added (`tabpro-ui`): an adapter that delivers immediately if already on
the EDT, or defers with `invokeLater` if not — every Swing component could hook into the `Editor`
through it, but only `ScoreCanvas` was wired up in this change, because
`TrackPanel`/`StatusBar`/`TrackSelector`/etc. cache state inside their `refresh()`/
`editorChanged()` (a *push* pattern) and their existing tests mutate the `Editor` and assert in
the same statement, from the test thread — never the EDT —: deferring that delivery would break
them. Extending the adapter to those components is a bigger change (rewriting those tests to pump
the EDT) that remains pending, outside the contained scope of this fix.

The smoke test with a real window (`ViewSwitchThenCursorMoveAuditTest`, `tabpro-app`, with a score
of our own that has Coda/Segno) walks the four views and moves the cursor to the last measure
from the test thread, the same as the original harness: it neither throws nor hangs, alone or
running alongside the other 103 tests in the `integration` group in parallel.

**About the hang:** it could not be reproduced with the rebuilt harness (neither in isolation nor
in the full run of the `integration` group, several times). The hypothesis with the most evidence
is the one `docs/audit-real-use.md` already documented: a global `AWTEventListener` (`Toolkit`)
with no `@ResourceLock(AuditSupport.SWING_LOCK)` in a class running in parallel with another that
does open real dialogs left the whole suite queued up ("reproduced once, without the lock"). The
disposable `GuiSmokeAuditTest` may have run without that lock — it was not kept in the repository
to confirm — which would also explain why the second run (with a different set of files) hung the
same way: the collision is with *another* class in the suite, not with the file under test.
</content>
