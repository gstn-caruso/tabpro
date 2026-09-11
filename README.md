# tabpro

A free guitar tablature and score editor, in Java 25 with Swing. You write
the music, see it as it looks in a published songbook, and hear it play.

![The tabpro window](docs/screenshots/tabpro.png)

It takes after Guitar Pro 5.2, borrowing its toolbar layout — with the
effects bar right under the score, like the original — and its keyboard
shortcuts. Dark theme, with Tabler Icons pictograms and musical symbols from
the Bravura font.

## What It Does

**You write in the tablature and the staff at the same time.** Whatever you
enter in one shows up in the other. Write with the keyboard — digits are the
fret, arrows move the cursor —, by clicking the fretboard or the keyboard
above, or by playing a connected MIDI instrument.

**A score holds as many tracks as you want**, each with its own tuning,
string count, capo and instrument. You can view them all together or one at
a time — jumping between them with the numbered selector on the toolbar —
and mute the ones in your way from the mix table. If you change a track's
string count, its notes transpose to the new tuning instead of getting lost:
a banjo line becomes a guitar one.

**The score looks the way it prints.** There is Page mode with its sheet,
margins, header and footer; Parchment mode with no page breaks; and two
screen modes that use all the available space. Zoom ranges from 30% to 200%,
in an editable toolbar combo with the manual's preset values.

![The score as it prints](docs/screenshots/sheet.png)

**It plays.** MIDI playback that follows the real order of the bars —
repeats, alternate endings and jumps like *D.C. al Coda* — and the effects:
bends and the tremolo bar with their curve, slides, hammer-ons/pull-offs,
trills, tremolo picking, harmonics, delayed strums, grace notes, fade in and
swing. With metronome, count-in, loop with a speed trainer, relative tempo
from x0.25 to x2 and step-by-step mode. While it plays, a thin vertical line
runs across the system marking where it is; clicking any bar jumps there
without stopping the sound, the step buttons switch to moving bar by bar,
and the window title shows the actual tempo at that moment.

**With real samples, if you want.** `F2` turns on the sound bank: tabpro
looks for the SoundFont files (`.sf2`, `.dls`) installed on the machine and
plays them with Gervill, the synthesizer that ships with Java. Without a
bank it sounds like the system synthesizer, as always. And it exports the
score to WAVE, rendered without opening any audio line: you can export it on
a machine with no sound card.

Parameter changes inserted mid-score actually play: lowering every track's
volume toward the end, switching instrument at the chorus, or speeding up
the tempo, with a transition measured in beats.

## Notation

Clef, key signature, dotted note values, stems, beams, rests, accidentals,
ties, tuplets and **two voices** per track.

On top of that, the guitarist's symbols: palm mute, let ring, tapping, slap
and pop, natural and artificial harmonics, vibrato and wide vibrato, trill,
tremolo picking, strums, pickstroke direction, ghost and dead notes, accents,
staccato, hammer-ons/pull-offs, all six slide types, bends with an editable
curve, grace notes, fingering for both hands, free text and chord diagrams.

And bar structure: repeats with their count, alternate endings, double bar,
musical directions (Coda, Segno, Fine and the fourteen jumps), markers —
edited from the bar under the cursor, without opening the full list —,
octave marks (8va, 8vb, 15ma and 15mb, which change where the note is
written without touching how it sounds), and forced or prevented line breaks
to lay out the page.

## The Fretboard and the Keyboard

![The fretboard and the keyboard](docs/screenshots/fretboard-and-keyboard.png)

They mark the beat's notes and write on click. They adjust themselves to the
active track's tuning, capo and string count. They can show just the beat,
the bar, the next beat, the last chord diagram, or the chosen scale — and
for scale notes you can show the name, the interval or the degree. Four
fretboard types, left- or right-handed, and the note under the mouse. The
fretboard has a wood grain finish and metal frets; the keyboard has beveled
keys and marks the note with a dot. Each one closes with the ✕ on its own
title bar.

You can also color note heads by dynamic intensity, to read the dynamics at
a glance, choose which toolbars you see and which you do not, and swap the
score and the mix table's positions.

## The Mix Table and the Global View

![The mix table and the global view](docs/screenshots/mix-table.png)

Port, the track's two channels — its own and its effects channel, so a bend
does not shift the tuning of clean notes —, General MIDI instrument — or a
drum kit, for percussion tracks —, volume and pan with a slider and a number
box, chorus, reverb, phaser and tremolo as a plain number, and mute and solo
next to each track's number. Everything editable while it plays. Next to it,
the global view: one colored row per track, the bar ruler, markers in red,
and a small silver square on every bar with no notes, to jump anywhere with
a click.

## The Tools

**The chord window** generates diagrams for any tuning, names them, suggests
a fingering and lets you correct it by hand — and remembers the one you
picked for the next time that shape shows up. You can move the base fret,
force or forbid the barre, omit chord notes, and save the ones you use most
in your own library.

**The scale window** ships its own library, shows how each scale is built,
and finds the one a range of bars uses, sorted by how many notes fall
outside it.

Plus the **tuner** — by ear, string by string, and the digital one with a
microphone —, the **percussion assistant** and the wizards in the Tools
menu: let ring, palm mute and dynamics per string over a range of bars,
arranging bars, filling in with rests, automatic fingering, transposing and
checking bar durations.

## Accessibility

**Everything works without a mouse.** Menus and dialogs are navigated by
mnemonic (`Alt` + the underlined letter); the mix table's sliders, the
fretboard, the keyboard, the global view grid, markers, the percussion
assistant, bends and the tuner are all operated by keyboard and show focus.
From the score, `Ctrl+F6` hands focus to the rest of the window.

**Every control has an accessible name and description**, on top of its
tooltip, so a screen reader announces it even without visible text. The
status bar at the bottom separates page, position, bar status and duration,
track, and title and author into six recessed panels, each with its own
name.

**Both palettes, light and dark, meet WCAG AA contrast.**
`Preferences > Accessibility` adds an adjustable font size, high contrast,
and the option to turn off animations.

**The interface ships in Spanish and English.** `Preferences > Language`
offers Automatic, Español and English; Automatic follows the system
language, and a change applies on restart.

## Files

Every new score starts with the tempo, time signature, key signature and
header data you left as default properties.

It saves to `.tabpro`, a readable, versioned JSON that keeps everything:
effects, ties, tuplets, both voices, each bar's attributes, track
properties, header data and lyrics.

It opens `.gp3`, `.gp4`, `.gp5` and `.gtp` files, and exports to the Guitar
Pro 4 format (warning beforehand about what is lost, if the score uses
something that format does not support).

The reader and writer are verified against PyGuitarPro and against the
**sixty-one authentic files** in its test suite — ten `.gp3`, sixteen `.gp4`
and thirty-five `.gp5` — not against files tabpro itself wrote: a reader and
a writer that share the same wrong assumption agree with each other, so the
oracle has to come from outside.

**Sixty of the sixty-one open**, and the one that does not is one
PyGuitarPro cannot open either. Before that check, twenty-four used to open:
of the thirty-five `.gp5` files, only **one** opened.

> If you imported Guitar Pro scores with a version earlier than 0.17.1,
> their bends ended up at half depth and some grace notes with the wrong
> transition. There is no way to fix them from the already-imported file:
> re-import the original.

It imports `.tef` files from TablEdit and `.ptb` files from PowerTab.

It imports and exports MIDI, ASCII tablature and MusicXML, each with its own
window: the MIDI one lists the file's tracks and lets you import them one at
a time or merge several onto an existing track; the ASCII one lets you paste
and fix the tablature before importing it onto the active track.

It also exports the score as an image and as a PDF, and prints it letting
you choose the page range and the scale.

The score browser (`Ctrl+B`) walks a folder and lets you listen to each file
without opening it: you choose how many bars play and playback moves on to
the next one by itself.

## Installation

**Debian / Ubuntu:** download the `.deb` from the latest
[release](https://github.com/gstn-caruso/tabpro/releases) and install it:

```sh
sudo apt install ./tabpro_0.60.0_all.deb
tabpro
```

It shows up in the applications menu and gets associated with `.tabpro`,
`.gp3`, `.gp4`, `.gp5` and `.gtp` files: opening any of them from the
desktop opens tabpro with that score. It needs a JRE 25 with a graphical
environment (`openjdk-25-jre`); the *headless* variant is not enough.

**Any system with Java 25:**

```sh
java -jar tabpro-app-0.60.0.jar [file]
```

## Keyboard Shortcuts

Digits write the fret, arrows move the cursor, `+` shortens the note value
and `-` lengthens it, `R` inserts a rest, `L` ties, `/` makes a triplet, `H`
a hammer-on/pull-off, `S` a slide, `B` a bend, `V` vibrato, `P` palm mute,
`I` let ring, `X` a dead note, `O` a ghost note, `G` a grace note, `T` text,
`A` a chord, `F` fade in, `Space` plays.

`F2` turns the sound bank on and off, `F5` opens the score information, `F6`
the track properties, `F7` the instrument, `F8` the page setup, `F9` the
loop, `F10` the mix table, `F11` the dynamic notes, `F12` the preferences.
`Ctrl+Tab` and `Shift+Tab` jump between markers. `F1` opens the full list.

The twelve menus — File, Edit, Bar, Track, Note, Effects, Markers, Tools,
Sound, View, Options and Help — carry everything else.

## What Is Still Missing

Exporting to `.gp5` — tabpro exports to `.gp4`, which is what the manual
asks for; `.gp5` files are read-only for now.

## How It's Built

Java 25, multi-module Maven, Swing with FlatLaf (its own light and dark
theme), Gson for the native format, and `javax.sound.midi` for playback and
capture.

Dependencies point one way, toward `tabpro-core`; the interface talks to the
format and to MIDI only through the `ScoreFiles`, `ScoreExchange` and
`Player` ports, defined in core. `tabpro-format` and `tabpro-midi` do not
know about each other: `ScoreExchange` is implemented by both, in halves —
notation by one, sound by the other — and `tabpro-app` composes them.

- `tabpro-core` — the score model (immutable), the editing session with
  undo and redo, notation, harmony, the wizards and playback.
- `tabpro-format` — the native format, the foreign notation that gets read
  and written (importing MIDI, ASCII and MusicXML) and the
  `.gp3/.gp4/.gp5/.gtp` reader.
- `tabpro-midi` — playback, capture and sound to file: exporting `.mid` and
  `.wav`.
- `tabpro-ui` — the interface: score, fretboard, keyboard, percussion, mix
  table, global view, status bar, menus, toolbars and the dialog windows.
- `tabpro-app` — `main`, the theme, the wiring — including composing the
  exchange — and packaging.

Tests live next to the code they test, but they do not run there. Surefire
spins up one JVM per module, and each one pays the startup and
class-loading cost again, which dominates a short suite: five modules are
five warm-ups for tests that, once the classes are loaded, run in under half
a second. `tabpro-tests` gathers the `test-classes` of all five and runs
them once, in a single JVM and with the JIT pinned to C1.

- `tabpro-tests` — has no code of its own; it just gathers the suite and
  runs it.

Since the five modules' tests run there, `mvn test -pl <module>` runs none
of them: the whole suite runs from the root.

```sh
mvn verify
```

It produces `tabpro-app/target/tabpro-app-<version>.jar` (executable) and
`tabpro-app/target/tabpro_<version>_all.deb`.

Every behavior change starts with a failing test. Each change goes on its
own branch, opens a PR against `main`, and CI (`mvn -B verify`, headless)
gates the merge. A `vX.Y.Z` tag on `main` triggers the release, which builds
the `.deb` and publishes it.

## License

[MIT](LICENSE)
