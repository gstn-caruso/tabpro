# Audit — the Guitar Pro 5 manual against tabpro 0.8.0

Five agents read the manual (`assets/manual-guitar-pro-5.pdf`) block by
block and checked every item against the code, with `file:line` as proof.
This document lists **only the gaps**. The work plan lives in [PLAN.md](PLAN.md).

State of the audited code: `main` = `4702129`, version 0.8.0.

---

## Import and export

Coverage declared by the auditor: 8 of 17 items present.

### Export to the Guitar Pro format — MISSING · large
`File > Export > Guitar Pro 4 Format`. There is no `exportGuitarPro` in any
module; `ScoreExchange.java:83` only declares `importGuitarPro(Path)`, and the
Export menu (`MenuBar.java:45`) only offers MIDI/ASCII/MusicXML/Image/PDF.

The existing reader (`tabpro-format/.../guitarpro/`, 2287 lines) is the mirror
to follow: it needs a `GuitarProByteWriter` (little-endian: int, short, boolean,
color, key signature, fixed-length and length-prefixed strings) and the writers
per section (header, channels, measure attributes, tracks, beats, notes, chords,
bends), orchestrated by a `GuitarProFile.write(Score, Path)` with the same order
as `read()`. Since the target is GP4 only, the `GuitarProVersion` abstraction
is not needed: the fixed GP4 layout (`generation=4, minor=6`) is enough.

### PowerTab import — MISSING · large
`File > Import > PowerTab`. Zero occurrences of "PowerTab" in the repo. Binary
`.ptb` format, documented by the open source project `powertabeditor`. New
package `tabpro-format/.../powertab/` with the same layered pattern as
`guitarpro/`.

### TablEdit import — MISSING · large
`File > Import > TablEdit`. Zero occurrences in the repo. Proprietary binary
`.tef` format with **no known public specification** — it is the most uncertain
of the four gaps: it would have to be reverse-engineered from sample files.

### WAVE export — MISSING · large
`File > Export > Wave`. `AudioSystem` is only used in `MicrophonePitch.java` for
the tuner's capture; there is no `AudioSystem.write(...)` anywhere.

The infrastructure is already there: `MidiScoreExporter.toSequence(Score)`
converts the score to a `Sequence`, and `MidiPlayer` knows how to open the
synthesizer. What is missing is offline rendering against `AudioSynthesizer`
(Gervill, the JDK's synth) and dumping the `AudioInputStream` with
`AudioSystem.write(..., WAVE, path)`. New class in `tabpro-midi` + dialog in
`tabpro-ui/dialogs`.

### BMP export — PARTIAL · small
The manual asks for BMP and notes that it is only enabled in Page mode.
`exportImage()` works but `ScorePrinting.formatOf` (`:73-76`) only returns `jpg`
or `png`, and the filter in `MainFrame.java:430` is `"Image (*.png, *.jpg)"`.
The `ViewMode` is not checked before exporting either. `ImageIO` already ships
the BMP plugin.

### MIDI import — "Use 2 channels per track" checkbox — MISSING · medium
The manual: two MIDI channels per track, useful for bends and slides.
`MidiImportPanel.java:24` only has the `transpose` checkbox. The real gap is in
the domain: `Channel` (`tabpro-core/.../model/Channel.java:4-6`) is a record
with a single `number` field. Telling detail: the Guitar Pro reader **already
parses** the second channel (`GuitarProTrackReader.java:30`,
`effectChannelIndex`) and discards it, because the model has nowhere to store
it.

### MIDI import — preview the tracks before importing — MISSING · small/medium
`MidiImportDialog.java:35-120` has no playback button at all. The pattern to
copy is in `ScoreBrowser.java:326`, which receives `transport::preview`.

### MIDI import — position and duration precision — MISSING · medium
The manual lets you choose the precision used to pick position and duration.
`MidiScoreImporter.java:192` always uses `DurationTicks.nearestTo(...)`, with no
parameter. Model to follow: the `rhythmChoice` combo in `AsciiImportPanel`.

### ASCII import — spacing for the `<variable>` rhythm — PARTIAL · small
The `<variable>` mode exists (`RhythmStrategy.FromSpacing`) but the second list
that fixes how many intervals sit between two quarter notes is missing.
`AsciiTabImporter.java:204` computes only from the cell width.

---

## Symbols, parameter changes, lyrics, markers, copy/paste, wizards and percussion

Coverage declared by the auditor: 41 of 47 items present. The block most
faithful to the manual: lyric syllable syntax, the six slide types, the bend
curve editor and the percussion wizard are all complete, and retrieving the
parameter changes when starting playback mid-score
(`SoundAutomation.java:35-50`) implements the manual's "Tip" to the letter.

### "Show 'Dynamic' Notes" [F11] — MISSING · small
The manual asks for the note to be painted with a gradient according to its
intensity. `grep "F11"` over `tabpro-ui` returns nothing; the View menu
(`MenuBar.java:174-177`) does not have it. _(Claimed by the other session.)_

### The tremolo bar symbol is not drawn — PARTIAL · small
Model, editor, dialog and **sound** are all there
(`TrackRenderer.java:240-241` builds the real pitch bend curve), but
`grep "tremoloBar"` over the whole `ui/score/` package returns **zero**
results: `paintBend` is only invoked for the note bend. _(Claimed by the
other session.)_

### Fade In is not drawn — PARTIAL · small
Model, editor and sound are all there (`MidiSequences.java:162`,
`writeFadeIn`); the "F" label in `TabSymbolPainter.labelsFor(Beat)` is
missing.

### The grace note transition is not used — PARTIAL · medium
`GraceTransition` (SLIDE/BEND/HAMMER/NONE) is edited, serialized to `.tabpro`
and read from the GP format… and **never consumed**: neither
`TrackRenderer.scheduleGrace` (`:198-202`) builds the curve, nor does
`TabNotationPainter.paintGraceNote` (`:104-115`) draw a different line. It is
data that travels through the whole system without any effect.

### A bend point's vibrato does not sound — PARTIAL · small
It is edited with a right click across three levels and drawn in the editor
(`BendGridPanel.java:82-83`), but `PitchTrajectory.of(Bend, ...)` (`:39-44`)
only looks at `point.semitones()`, never `point.vibrato()`.

### Wah-wah: no symbol and not read from GP files — PARTIAL · small
Model, editor and menu exist; the symbol is missing from the score and the wah
byte is not read in `GuitarProBeatReader`, so a real `.gp5` loses the data on
import. Not sounding is correct: the manual says it only affects RSE.
_(Claimed by the other session.)_

### Copy and paste between two sessions — MISSING · small or large
`Editor.java:61` uses its own in-memory `Clipboard`, not
`Toolkit.getSystemClipboard()`. Wiring it to the system clipboard is small;
truly supporting several windows is an architecture change.

---

## Working with the score and printing

Coverage declared by the auditor: 27 of 50 items present. This is the block
with the most gaps. Best covered: the speed trainer, parameter changes, the
full page setup with Refresh/Save as default, the three channel detection
modes and the global view.

### Repositioning the audio during playback — PARTIAL · medium
The manual: clicking on the score during playback resumes from there without
stopping, and the same with Ctrl+Tab/Shift+Tab between markers. Today the
editing cursor moves (`ScoreCanvas.java:58-65`) but the audio stays where it
was: `Player.java:6-19` only exposes `play/stop/playNote`, with no seek.

### The MIDI engine ignores each track's port and channel — PARTIAL · medium
The model (`Channel.port`, `Channel.number`) and the mixer display them, but
`MidiSequences.java:64-68,111-115` (`channelFor`) assigns channels by
sequential order skipping channel 9, never reading what the track actually
says. `TrackTimeline` does not even have a `port` field.

### A single output device instead of four ports — PARTIAL · medium
The manual: four simultaneous MIDI ports, each with its own device.
`MidiDeviceSetup.java:18-41` has a single `output` field, and `MidiSetupDialog`
a single combo. _(The other session claimed MIDI Setup.)_

### File > Search the web — MISSING · medium/large
Neither the menu nor any service; zero matches across the five modules.

### The metronome does not sound on its own — PARTIAL · small
`Player.play(timeline, clicks, listener)` (`:11-13`) always needs a
`Timeline`; the manual says the metronome can be used on its own.

### Step by step: the buttons do not change function during playback — PARTIAL · small/medium
The manual: during playback they turn into previous/next measure.
`Transport.java:132-150` aborts with `if (player.isPlaying()) return;`.

### No track toolbar — MISSING · small
`ToolBars.java` defines `documentRow/structureRow/notationRow`; there is no
track selector outside the mixer.

### View > Swap view (score ↔ mixer) — MISSING · small
`MainFrame.java:710-718` only shows or hides the mixer inside a `JSplitPane`.

### Preferences: force multitrack on a horizontal screen — MISSING · small
`Preferences.java:10-15` only stores the default figure, count-in,
auto-scroll and the bass note in the chord name.

### Recent files are saved but not shown — PARTIAL · small
`Preferences.java:15-56` implements `MAX_RECENT_FILES=8`, `recentFiles()` and
`remember(Path)`, and `ScoreDocument.java:66,73` feeds them… but
`recentFiles()` has not a single consumer in the UI.

### Open is split into two commands — PARTIAL · small
`file.open` (Ctrl+O) only filters `.tabpro` (`MainFrame.java:288-290`); for a
`.gp5` you have to go through the separate import command. The manual has a
single Open.

### "Limit Pitch Variation" — MISSING · small
The checkbox that forbids variations of more than a whole tone does not exist
in any module.

### MIDI Setup: no test-sound button and no editable sensitivity — small
There is no per-device preview, and `MidiCapture.DEFAULT_SENSITIVITY_MILLIS = 60`
(`:20,42`) is fixed in the code. _(The other session claimed MIDI Setup.)_

### The current tempo does not appear in the title during playback — MISSING · small
`ScoreDocument.windowTitle()` (`:45-48`) does not include it and `MainFrame`
only updates the title from the `Editor`, not from the `Transport`.

### Print: missing the "Configure" button for the paper format — PARTIAL · small
Scale and fit-to-page are there; delegating to `PrinterJob.pageDialog()` is
missing (only `printDialog()` is used, which is something else).

### Minor
A single toggle for the whole toolbar instead of one per bar; File > Browse
without an option for how many measures to sound before jumping to the next
file; relative tempo with no one-click button to disable it; clicking on the
mixer does not jump to the measure's first beat; and no `PAGE_UP` /
`PAGE_DOWN` to navigate (`KeyboardEditing.java:32-39`).

---

## Notation and writing

Coverage declared by the auditor: 44 of 58 items present. Best covered: the
automatic measure advance when moving the cursor, note relocation when
changing the tuning, the fourteen musical directions, MIDI capture and the
automatic clef based on the tuning.

### Irregular groups beyond the triplet — PARTIAL · small
`Tuplet.AVAILABLE` (`:14`) already lists 1,3,5,6,7,9,10,11,12,13 and
`Editor.setTuplet` (`:170`) is generic, but the UI only wires `note.triplet`
(`Commands.java:171`), which toggles 1↔3. The engine is there; the door is
missing.

### 12-string and 5-string banjo are checkboxes with no effect — PARTIAL · medium
They are edited, persisted and travel through the GP format, but no fretboard
class, painter or fret calculation reads them. Compare with `capo`, which is
actually used in `Track.java:90`.

### Line break with the wrong scope — PARTIAL · medium
The manual: it affects only the active track or the multitrack view, so each
track can have its own measure layout. Today `Editor.setLineBreak`
(`:391-393`) goes through `withAttributesInEveryTrackAt(...)` and
`Score.attributesOf` (`:55-58`) always reads from `track(0)`.

### Ctrl+drag for whole measures — PARTIAL · small
`Selection.wholeMeasures` and `Editor.clippingOf` (`:761`) already distinguish
the case, but `ScoreCanvas.java:69,248-256` always passes `false` and there is
not a single `isControlDown()` anywhere in the UI.

### File > New does not open the Info window — PARTIAL · small
Both pieces exist, they are just not chained together
(`MainFrame.java:294-301`).

### Preferences: fixed autosave and undo-disable with no effect — PARTIAL · small
`Preferences.java:59-71` defines `autosaveEvery()` and `undoEnabled()`; the F12
panel exposes neither, and `undoEnabled()` **is not checked anywhere**.

### Score info with no "Default Properties" tab — MISSING · medium
`ScoreInfoDialog.java:21-23` only builds "General" and "Lyrics".

### No manual override for beaming or stem direction — MISSING · medium
`StemDirection.pointsUp(...)` always computes from the average of the
pitches; the Note menu has no entry for it.

### Fixed toolbars with no visibility submenu — MISSING · medium
`ToolBars.java:127-132` forces `setFloatable(false)`; View has a single
toggle.

### No right-click context menu on the tab — MISSING · small
Zero `JPopupMenu` anywhere in the interface.

### Out of scope by decision
The GP3-like and GP4-like skins (`Theme.java` documents the decision: our own
light and dark theme, without the Windows XP look) and the instrument's
MIDI/RSE selector, which disappears because RSE does not exist.

---

## Guitarist tools and shortcuts

Coverage declared by the auditor: 69 of 78 items present. The chord window,
the scales window, the fretboard, the keyboard, the tuner and the metronome
are all closely aligned with the manual.

### Enter does not add a note in standard notation — MISSING · large
The manual puts it in the Edition table. In tabpro `Commands.java:308` binds
`ENTER` to "next note". This implies a staff-based input mode that does not
exist: writing today is 100% by fret digits.

### No "Scales" button on the fretboard and the keyboard — MISSING · small
The manual puts it at the top right of both. The only door is the Tools menu.
The inline selector also only offers 5 scales from `ScaleType`, not the 47
from `ScaleLibrary`.

### The fretboard and the keyboard are not floating bars — MISSING · medium
The manual says they are toolbars dockable at the top or bottom, or floating.
`MainFrame.java:170` puts them in a fixed panel.

### The scales window does not show the semitones between notes — PARTIAL · small
It shows the name, degree and interval from the tonic, but not the pattern of
distances between consecutive notes.

### Chord zone A does not indicate "Custom" mode — PARTIAL · small
The name is cleared and the model knows about it
(`ChordEditorModel:275-282`), but `ChordDialog` never reads `isCustom()`.

### Combos showing raw enum names — PARTIAL · small
`COMPLEX`, `ANY`, `FORCE`, `FORBID` with no `label()` and no renderer.

### `*` as an alternative key for the dot — PARTIAL · small
`Commands.java:169` only binds `PERIOD`.

_The Effects, Navigation, Sound and Misc. tables were left out of this report
because of a manual-clipping error; they are audited separately._

---
</content>
