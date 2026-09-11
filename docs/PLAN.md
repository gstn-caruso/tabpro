# Plan — the complete Guitar Pro 5 manual clone

Living document. Updated when each stage closes, so it can be picked up again without prior context.

## Goal

tabpro has to do **everything** described in `assets/manual-guitar-pro-5.pdf`.
It is a free clone: wherever Guitar Pro uses something proprietary, tabpro
provides the closest free alternative.

Decisions made with the user (2026-09-06):

- **Scope:** everything missing against the manual, not just the four gaps
  the v0.8.0 README declared.
- **Sound:** RSE is replaced with a **free SoundFont loaded into Gervill**,
  the JDK's synthesizer. It improves all playback, not just the WAVE export.
  No `.sf2` file is committed to the repo (they weigh over 100 MB): the
  program discovers the ones on the system, or the user picks one, the
  choice persists, and if there is none it falls back to the JDK's internal
  bank. The `.deb` recommends `fluid-soundfont-gm`.
- **Versioning:** stops being manual. **semantic-release** handles it on push
  to `main`, based on the squash commit type: `feat:` → minor, `fix:`/`perf:`
  → patch, breaking → major, `docs:`/`chore:`/`ci:`/`test:` → no bump. That
  produces the tag and the `.deb` release, automatically. **Nobody touches
  the version in the poms or the README by hand.**
- **Hygiene:** the tree starts clean; every change goes through its own
  branch + PR with green CI.

## How We Work

- The main agent orchestrates and does not implement: it hands work off to
  subagents with their own worktree (`Agent(isolation: "worktree")`), and it
  opens the PRs itself.
- Models: mechanical/read-only → `haiku`; TDD/refactor loops → `sonnet`; hard
  design → `opus`. Never `fable` outside planning.
- TDD mandatory: failing test → minimal code → refactor. One PR per gap.
- Push on every green (agents drop out from rate limits; work is not lost).

To regenerate the manual's text:

```sh
pdftotext -layout assets/manual-guitar-pro-5.pdf /tmp/manual.txt
```

Sections (line numbers in that `.txt`): Understanding Notation 385, Main Screen 458,
Write a Score 481, Add Symbols 961, Insert Parameter Changes 1340, Add Lyrics 1377,
Add Markers 1448, Cut/Copy/Paste 1478, Wizards 1587, Percussion 1670,
Work with a Score 1723, Configure the Display 1886, Configure the Sound 1945,
Play the Score 2087, Print a Score 2207, Import a Score 2293, Export a Score 2506,
Tools for the Guitarist 2665, Keyboard Shortcuts 3154 **through the end of the file**
(trimming it earlier leaves out the Effects, Navigation, Sound and Misc tables).

## Two Sessions Working at Once

There is **another Claude session** on the same repo (`tabpro-mvp-editor-playback`),
which closed PRs #30 to #42. The agreed split:

| Session | Handles |
|---|---|
| This one (`docs/`, formats, sound) | The whole Import/Export chapter, the SoundFont sound engine, and the Guitar Pro reader's gaps |
| The other | Notation, effects, interface, playback, guitarist's tools and shortcuts |

Coexistence rules: each one works in isolated worktrees, never in the shared
checkout; this session handles `docs/`; and whoever is about to touch
`MidiSetupDialog.java` gives a heads-up first, because both halves land
there.

## Status

| Stage | Status |
|---|---|
| 0. Tree cleanup (PR #41 and #42 merged, worktrees and branches pruned) | ✅ done |
| 1. Manual audit against the code | ✅ done — see [audit-manual.md](audit-manual.md) |
| 1b. Audit of the shortcut tables left out of the trim | ✅ re-audited 2026-09-06 — see below |
| 2. Automatic versioning with semantic-release | ✅ done — it published v0.9.0 on its own |
| 3. Implementing the gaps, one PR per gap | 🔜 in progress |
| 4. Independent verification of the binary formats | 🔜 in progress — see below |

Starting point: `main` = `4702129`, version 0.8.0. When stage 2 closed, `main`
was at **v0.9.0**, published automatically by the pipeline.

### What Bringing Up the Pipeline Taught Us

Two things worth not learning twice:

1. **`v0.8.0` did not exist.** The repo's last tag was `v0.6.1`, even though
   the poms said 0.8.0 and the README pointed at downloading a `.deb` of that
   version. Before turning on semantic-release, `v0.8.0` had to be tagged,
   or it would have computed a version **older** than the one the program
   already claimed to have.
2. **A dry-run without credentials does not prove what it looks like.**
   semantic-release stops at the GitHub plugin's `verifyConditions`, which
   runs *before* `generateNotes`, so the dry-run only verified that the
   configuration loaded, not that the notes could be generated. The first
   real release failed on an incompatible preset the dry-run never got to
   exercise. To really test it, you have to run it with the
   credential-requiring plugins removed.

### Shortcut Re-audit (2026-09-06)

The "37 of 41" predated a dozen PRs and nobody had measured it again. It was
redone in full against `Commands.java`, checking the manual's Reference
chapter's 76 rows (pages 79 to 81) one by one — not whether "some" command
had the key, but whether the **correct** command had it. Test that backs
this up: `ManualKeyboardShortcutsTest` (72 rows comparable 1:1 against the
catalog).

- **72 of 72 match.** Not a single difference against the manual today.
- **4 do not go through the catalog but are there:** Home/End (first/last
  beat of the bar) and the dotted-value `*` are resolved by `KeyboardEditing`
  as a raw canvas key (with its own test); Page Up/Page Down are resolved by
  Swing alone, scrolling the score's `JScrollPane`.
- **Shortcuts tabpro has that the manual does not list:** zoom (`Ctrl +`/
  `Ctrl -`/`Ctrl 0`), fretboard (`Ctrl 3`) and keyboard (`Ctrl 4`). They do
  not collide with any manual shortcut, so they stay.
- **Missing feature, not a shortcut:** `Enter` as "add a note in standard
  notation" does not exist — already noted in
  [audit-manual.md](audit-manual.md) ("Enter does not add a note in standard
  notation — MISSING · large"). tabpro only writes via fret digits.
- **Real bug found and fixed — not in the catalog, in the plumbing:**
  `JScrollPane` and `JSplitPane` come with factory shortcuts (scroll,
  navigate the split) that Swing checks *before* a menu accelerator. With
  focus on the score — the normal situation while editing — they swallowed
  `Ctrl+Home` (nav.firstBar), `Ctrl+End` (nav.lastBar), `F6`
  (track.properties), `F8` (file.pageSetup) and `Ctrl+Tab` (marker.next):
  the catalog declared the right key and it hung off its menu, but pressing
  it did nothing. Fixed by `AcceleratorGuard` (`AcceleratorGuardTest`); see
  the PR on branch `fix/los-atajos-que-el-manual-manda`.
- **No collisions.** `CommandsTest.noTwoCommandsShareTheSameShortcut` already
  covered this and stays green; `MenuBarTest.todoComandoConAceleradorCuelgaDeAlgunMenu`
  was added so that a declared shortcut that never hung off any menu (the
  other way to end up dead) would not go unnoticed either.

## This Session's Gaps

All closed except the last one.

| Gap | Status |
|---|---|
| Export image as BMP (+ restrict to Page mode) | ✅ #45 |
| Image export fails loudly if `ImageIO` does not write | ✅ #49 |
| The GP reader dropped musical directions | ✅ #50 |
| Export to WAVE | ✅ #51 |
| `MidiScoreExporter` wrote a single tempo | ✅ #52 |
| The reader declares the order of the direction slots | ✅ #54 |
| Import dialogs (listen to tracks, precision, ASCII spacing) | ✅ #57 |
| The sound exporter moves to `tabpro-midi` | ✅ #58 |
| Import TablEdit | ✅ #62 |
| The TablEdit import reaches the importer again | ✅ #64 |
| **Four bugs in the Guitar Pro reader** | ✅ #69 |
| The exchange port with a throwing `default` | ✅ #70 |
| The exported `.mid` sounds like the score | ✅ #60 |
| Export to the Guitar Pro format | ✅ #74 |
| The editing cursor as a red line | ✅ #76 |
| Import PowerTab | ✅ #77 |
| SoundFont sound bank (+ F2) | ✅ #71 |
| MIDI import: "2 channels per track" checkbox | ✅ done, waiting on the `effectChannelNextTo` fix |
| The GP reader drops the wah byte | 🔜 in progress |

## The Most Important Lesson Learned

**The oracle was inside the system we wanted to verify.** The same error
showed up five times in one day, in five disguises:

1. **The Guitar Pro reader misread real files.** The new `.gp4` writer
   passed every round-trip against our own reader. Against **PyGuitarPro**
   and authentic `.gp4` files: **seven out of seven generated files would
   not open**, and of six bugs, **four were in the reader already in
   production**. Three of sixteen authentic files did not even open in
   tabpro. It happened because reader and writer share the same assumptions
   — and we generated the repo's fixtures ourselves with that same reader.
2. **semantic-release's dry-run** verified that the configuration loaded,
   not that the notes could be generated: without a token it stops before
   that step.
3. **CI with no sound card is the real user.** A MIDI port that failed to
   open took the entire playback down with it. And exporting to WAVE blew
   up with an unhandled exception on any machine without audio — the render
   asked for a line that an *offline* render does not need.
4. **Test the sound bank on a machine that actually has one installed.**
   Skipping a path with `Assumptions` is not testing it.
5. **The tests tested the piece, not the user's path.** `importTabEdit` was
   implemented, tested and **unreachable**: nobody had written the line
   that delegates to it, and the user picked their file only to get "not
   available".

### The Rules That Remain

- **A round-trip against our own reader proves internal consistency, not
  compatibility.** The only verification that means anything for a binary
  format is against an authentic file or another implementation.
- **An environment difference does not say which side the bug is on.** It
  says there is an assumption about the machine buried somewhere. The
  question that tells them apart: *what would I want to happen on the
  user's machine?*
- **A `default` that throws turns a compile-time error into a runtime one**,
  and makes "I don't support this" and "I forgot about this" look the same
  in the code.
- **When two different features twist at the same point, the point is in
  the wrong place.**
- **A test that cannot fail is garbage**; one that patches a hole the
  design could close instead is a band-aid.

### When the Bug Already Shipped

Two bugs reached people's files, and **they call for different things**.
The question is not "did the bug reach the files?" but **"is the correct
data still derivable from what was saved?"**

- **The bends destroyed the data.** A bend read at half depth is
  indistinguishable from a legitimate one of that depth. No migration is
  possible: a release note goes out asking users to re-import the original.
- **The channels lost nothing.** The real channel was a deterministic
  function of track order, so the program recomputes it on open and the
  user never notices. It does not go in the release note.

---

## Stage: Looks and Behaves Like Guitar Pro 5 (started 2026-09-10)

Goal stated by Gastón: **a program as close as possible to Guitar Pro 5.2**
(abandonware), with the manual as the product guide. Three fronts: what the
manual describes that still does not work *when you use it*, the visual
look (toolbars and icons), and accessibility, which today is zero.

### Decisions (2026-09-10)

- **Look:** the layout, sizes and semantics of toolbars and icons are
  measured from the manual's screenshots (`pdfimages`, see the typography
  note); FlatLaf Darcula stays. Windows' light theme is not copied.
- **Icons:** generic actions (file, edit, zoom, transport, view) come from
  **Tabler Icons** (MIT, SVG) drawn directly with `jsvg` 2.1.0 (the library
  FlatLaf uses underneath; `flatlaf-extras` was dropped because it drags
  FlatLaf into `tabpro-ui`, and FlatLaf only lives in `tabpro-app`). Musical
  symbols come from **Bravura**, already in the repo. Effects with no SMuFL
  glyph (P.M., let ring, tapping…) go as abbreviated text, like in GP5.
  Only the SVGs actually used get committed, with their license alongside.
- **Accessibility, the four fronts:** full keyboard support (mnemonics, tab
  order, visible focus, no control reachable only with the mouse); screen
  reader (accessible name and description on every control); contrast and
  size (WCAG AA over the dark theme, a tooltip on every icon, UI scale);
  and an **Accessibility** section in Preferences (font size, high
  contrast, no animations). Every preference is born with its reader and
  its test: the previous stage's rule still holds.
- **How failures get found:** a **real-use** audit — every manual action
  exercised through the user's path (menu, shortcut, button, dialog),
  checking the observable effect — instead of another static reading of
  the code. Report in `docs/audit-real-use.md`, harness under the
  `integracion` tag.
- **Flow:** autonomous (DIY) and looping. The main agent plans, briefs,
  opens the PR, waits for CI and merges. The `worker` agent
  (`~/.claude/agents/worker.md`: sonnet, its own worktree, TDD + TCR, push
  on every green, no PR) writes the code. One feature branch per change,
  one PR per change type.

### Fronts and Order

| # | Front | PR Slices |
|---|---|---|
| A | Real-use audit | the report and the harness; then one `fix/` per finding, the LIES first |
| B | Icons | B1 `IconSet` port + `flatlaf-extras` + first SVGs · B2 generic toolbars to Tabler · B3 note values, clefs and effects to Bravura · B4 toolbars grouped and ordered like the manual's "Main Screen" · B5 color per theme, disabled state and HiDPI |
| C | Accessibility | C1 a test that walks the component tree and requires a tooltip and accessible name on every textless control · C2 mnemonics in menus and dialogs with a no-collision test · C3 keyboard on the custom components (knobs, fretboard, piano, grid) and visible focus · C4 WCAG AA contrast test on the palette · C5 Accessibility section in Preferences |
| D | Whatever the audit surfaces visually | status bar, mix table, global view: measured against the screenshots |

The inventories that feed B and C (current icons against GP5's toolbars;
components with no tooltip, accessible name or keyboard; the palette) are
generated with single-pass agents and are not committed: whatever from
them is worth keeping goes into the PR that uses it.

### How to Resume Without Context

The loop is: pick the next slice from this table (or the next finding from
the latest audit in `docs/`) → brief a `worker`
(`~/.claude/agents/worker.md`) with the goal, branch, closed decisions,
completion criteria and trailer → look at the PNG the worker leaves in the
scratchpad → open the PR stating what and why → `gh pr checks` green →
`gh pr merge --squash` → delete the remote branch only if the PR shows
MERGED → update this table. When an audit runs dry, a new outside oracle
is sought (the manual, its screenshots, real files); the first three were
real use, the window's look and the score's look; the fourth, the corpus;
the fifth, in progress, the dialogs.

### Status

| Item | Branch | PR | State |
|---|---|---|---|
| Stage plan and updates | `docs/plan-etapa-visual`, `docs/estado-etapa-visual` | #112, #120 | merged |
| A · real-use audit, 15 of 15 chapters (47 OK / 7 LIES / 1 MISSING; Print and Import/Export all OK) | `docs/auditoria-uso-real`, `docs/auditoria-uso-real-print-e-import-export` | #114, #129 | merged |
| A · CI runs the harness under Xvfb | `ci/harness-con-display` | #117 | merged |
| A · fix: metronome settings open from Sound (finding 6) | `fix/configuracion-del-metronomo-alcanzable` | #119 | merged |
| A · fix: the seven shortcuts Swing was intercepting (findings 1–5) | `fix/atajos-que-swing-interceptaba` | #121 | merged |
| B1 · generic icons from Tabler (`SvgIcon` + `jsvg`) | `feat/iconos-tabler` | #113 | merged |
| B3 · musical symbols from Bravura (`GlyphIcon`) | `feat/iconos-bravura` | #118 | merged |
| B4 · toolbars with GP5's order and groups, effects below the score | `feat/barras-como-gp5` | #127 | merged |
| B4 · ppp…fff dynamics and sound bank as buttons | `feat/dinamicas-y-banco-de-sonidos-en-las-barras` | #132 | merged |
| B4 · toolbars set their colors from the palette, with or without a theme | `fix/barras-superiores-apagadas` | #133 | merged |
| B4 · track selector by number (row 1) | `feat/selector-de-pista-por-numero` | — | in progress |
| C1 · accessible name and tooltip on every control (`AccessibilityWalker`) | `feat/nombres-accesibles` | #115 | merged |
| C1 · seven dialogs made separable for the walker | `refactor/dialogos-separables-para-el-recorredor` | #128 | merged |
| C2 · Alt+letter mnemonics in menus and dialogs | `feat/mnemonicos` | #124 | merged |
| C3 · keyboard and visible focus on the knob, fretboard, keyboard and grid | `feat/teclado-en-los-componentes-custom` | #122 | merged |
| C3 · markers, percussion, bends and tuner by keyboard; Ctrl+F6 hands off focus | `feat/teclado-en-los-custom-menores-y-salida-de-foco` | #125 | merged |
| C3 · the Ctrl+F6 test injects the focus traversal | `test/ctrl-f6-sin-el-focus-manager-global` | #130 | merged |
| C4 · WCAG AA contrast in both palettes, with tests | `fix/contraste-wcag` | #116 | merged |
| C4 · contrast in the mix table and the four fretboards | `fix/contraste-en-la-mesa-y-los-mastiles` | #126 | merged |
| C5 · Preferences > Accessibility (font, high contrast, no animations) | `feat/preferencias-de-accesibilidad` | #123 | merged |
| D · README and screenshots with the real theme | `docs/readme-y-capturas-de-la-etapa-visual` | #131 | merged |
| B4 · track selector by number (row 1) | `feat/selector-de-pista-por-numero` | #136 | merged |
| B4 · editable zoom combo with the percentage | `feat/combo-de-zoom-como-gp5` | #140 | merged |
| B4 · edit the cursor's marker from menu and toolbar | `feat/editar-el-marcador-del-cursor` | #144 | merged |
| C4 · the barre in every fretboard's palette | `fix/la-cejilla-cumple-el-contraste-en-cada-mastil` | #135 | merged |
| A · printing verifiable up to the `PrinterJob` (`Printing` seam) | `refactor/impresion-con-costura-para-el-printer-job` | #137 | merged |
| D · zone-by-zone visual audit against the GP5 screenshot (ten differences measured) | — | — | done, not committed |
| D · fretboard with wood grain and metal frets, keyboard with dot and bevel | `feat/diapason-y-teclado-como-gp5` | #138 | merged |
| D · status bar with GP5's six recessed panels | `feat/barra-de-estado-como-gp5` | #139 | merged |
| D · mix table with sliders, plain numbers and GP5's columns | `feat/mesa-de-mezcla-como-gp5` | #141 | merged |
| D · global view with bar ruler, markers in red and a 1.3× header | `feat/vista-global-como-gp5` | #142 | merged |
| D · title bars with ✕ on the fretboard and the keyboard | `feat/paneles-de-diapason-y-teclado-con-titulo` | #143 | merged |
| D · README and screenshots for the second batch, on the final `main` | `docs/readme-y-capturas-de-la-segunda-tanda` | #152 | merged |
| D · the zoom combo keeps its width | `fix/el-combo-de-zoom-no-se-estira` | #146 | merged |
| E · score audit against the manual's notation examples (ten items measured) | — | — | done, not committed |
| E · string names off by default, like GP5 | `feat/nombres-de-cuerda-opcionales-por-pista` | #147 | merged |
| E · initial tempo written out, tempo and bar numbers in red | `feat/tempo-inicial-y-numeros-de-compas-como-gp5` | #148 | merged |
| E · staff–tab gap measured (36 px) | `feat/brecha-pentagrama-tab-como-gp5` | #149 | merged |
| E · yellow selection and colored marker square | `feat/seleccion-amarilla-y-marcador-con-color-como-gp5` | #150 | merged |
| E · the marker square no longer overlaps the track name | `fix/el-cuadrado-del-marcador-no-pisa-el-nombre-de-pista` | #151 | merged |
| E · dynamics written under the note | — | — | **not applicable**: the manual says GP5 does not show them on the score; F11 mode already exists |
| B4 · right-hand fingering button | `feat/boton-de-digitacion-de-mano-derecha` | #154 | merged |
| F · robustness audit with the real corpus (71 files: 68 open, 68 pass render, export and reopen) | `docs/auditoria-corpus` | #155 | merged |
| F · fix: the score receives `Editor` notifications on the EDT and no longer scrolls with a 0×0 viewport | `fix/las-notificaciones-del-editor-llegan-por-el-edt` | #156 | merged |
| F · every Swing component listens to the `Editor` through the EDT adapter | `refactor/todos-los-componentes-escuchan-al-editor-por-el-edt` | #158 | merged |
| G · visual audit of the dialogs against the manual's screenshots (fifteen items) | — | — | done, not committed |
| G · «Documento centrado» (Center on Page) when printing | `feat/documento-centrado-al-imprimir` | #159 | merged |
| G · lyrics in five tabs with a multi-line area | `feat/letra-multilinea-como-gp5` | #160 | merged |
| G · combos and lists show Spanish labels (`Labels`, oracle in the walker) | `fix/los-combos-muestran-etiquetas-legibles` | #161 | merged |
| G · form sections as titled group boxes | `feat/cajas-de-grupo-con-titulo-en-los-formularios` | #162 | merged |
| G · marker list as a management table | `feat/lista-de-marcadores-con-tabla-como-gp5` | #163 | merged |
| G · track properties in two columns, «Forzar canales 11 a 16» (Force channels 11 to 16) and diagram styles | `feat/propiedades-de-pista-como-gp5` | #164 | merged |
| G · percussion assistant in a four-column grid | `feat/asistente-de-percusion-en-grilla-como-gp5` | #165 | merged |
| F · the harness fails clean instead of hanging the suite | `test/el-harness-falla-limpio-en-vez-de-colgarse` | #166 | merged |
| G · the combo oracle no longer exempts custom renderers; Orientation and Paper get labels | `fix/el-oraculo-de-combos-no-exime-renderers-propios` | #168 | merged |
| G · chord builder: inversion split from the bass, positions and barre with radio buttons | `feat/constructor-de-acordes-como-gp5` | #169 | merged |
| G · import MIDI with preview, select-all and quantization radio buttons with a real effect | `feat/importar-midi-como-gp5` | #170 | merged |
| G · scale tool with lists, a degree diagram and listen | `feat/herramienta-de-escalas-como-gp5` | #171 | merged |
| F · `ScoreDocumentTest` without the real Preferences; Preferences tests serialized | `test/metronomo-y-documento-sin-flakiness` | #172 | merged |
| G · split chord «Tipo» (Type) into extension, alterations and «add» (touches naming, diagrams and GP formats) | — | — | noted, large piece |
| G · «?» help and «Aplicar» (Apply) buttons in dialogs | — | — | dropped: no help to show |
| H · fresh visual audit of the whole app after 61 PRs (ten findings, plus fifteen from the window sub-audit) | — | — | done, not committed |
| H · slanted beams, with «Forzar barras horizontales» (Force horizontal beams) per track | `feat/barras-de-union-inclinadas-como-gp5` | #174 | merged |
| H · the ASCII preview is verified with the real dialog (it was a fixture artifact) | `fix/la-vista-previa-ascii-muestra-la-tablatura` | #175 | merged |
| H · moving the cursor clears the selection, Shift extends it, Ctrl+A paints it | `fix/la-seleccion-se-limpia-al-mover-el-cursor` | #176 | merged |
| H · the table shows port and channel with two digits and the full instrument name | `fix/la-mesa-muestra-los-canales-y-el-instrumento-enteros` | #177 | merged |
| H · effect texts no longer overlap the fret, 8×12 marker, staccato opposite the stem | `fix/textos-de-efecto-marcador-y-staccato-como-gp5` | #178 | merged |
| H · fretboard and keyboard closed by default with remembered state; Scales defaults to C major | `fix/valores-iniciales-como-gp5` | #179 | merged |
| H · accents and ñ restored in 107 literals across 47 files, with a guardian | `fix/tildes-en-los-textos-de-la-interfaz` | #180 | merged |
| F · the percussion grid fixes its cell size (flaky under the parallel suite) | `test/la-grilla-de-percusion-sin-flakiness` | #181 | merged |
| H · no dialog exceeds the screen; Page Setup and MIDI shown in full; MIDI drops the locale-dependent `JOptionPane` | `fix/configurar-pagina-y-midi-entran-en-pantalla` | #182 | merged |
| H · tremolo bar with its six types and exact GP5 codes; let ring/palm mute/dynamics options get their own title and focus | `fix/palanca-con-sus-tipos-y-opciones-por-pestana` | #183 | merged |
| F · guardian: every test that mutates the look and feel carries `@Isolated` | `test/los-tests-que-instalan-un-tema-corren-aislados` | #184 | merged |
| F · permanent smoke net: every repo fixture opens, renders, exports and reopens on every CI run | `test/humo-permanente-sobre-los-fixtures` | #186 | merged |
| I · performance audit with 100/300/600-bar scores (five hotspots measured) | — | — | done, not committed |
| I · the score paints only visible sheets and systems; layout memoized (Page mode: 28 s → 10 ms) | `perf/la-partitura-pinta-solo-lo-visible` | #187 | merged |
| I · moving the cursor repaints only its zone; the `Editor` reports what changed (407 ms → 19 ms per arrow key) | `perf/mover-el-cursor-repinta-solo-lo-que-cambio` | #188 | merged |
| I · each sheet iterates only its own bars; chords under the title get cached | `perf/el-export-pinta-solo-los-compases-de-cada-hoja` | #189 | merged |
| I · the PDF and the image encode each sheet in one block (90% of export time) | `perf/el-pdf-y-la-imagen-codifican-cada-hoja-en-bloque` | — | in progress |
| G · «Forzar barras horizontales» (requires slanted beams, a large rendering piece) | — | — | noted |

Lo que queda anotado para después: digitación de mano derecha como botón
aparte (el diálogo único ya cubre las dos manos), tres íconos de la captura de
GP5 que no se distinguen, `doubleBar` y `tuplet` en Java2D por ser sub-píxel en
Bravura, los valores predefinidos del combo de zoom (el manual no los lista), y
la fuente del dígito de traste, que la resolución del manual no permite afirmar.

**Estado (2026-09-11):** 77 PRs de la etapa (#112–#189) en `main`, CI verde,
~5750 tests contando los parametrizados. Siete auditorías con oráculo externo:
uso real, ventana, partitura, corpus real, diálogos, pasada fresca y
rendimiento (en cierre). Tres auditorías hechas, todas con oráculo externo: uso
real de los 15 capítulos del manual (harness que corre en el CI bajo Xvfb),
visual zona por zona y de la partitura contra las capturas del manual, medidas
en píxeles. Lo que las tres encontraron está cerrado o anotado arriba.

**Lo que enseñó la sexta tanda:** una pasada fresca después de una tanda grande
encuentra regresiones que ninguna auditoría anterior podía ver (la mesa nueva
escondía los dígitos, las cajas de grupo enterraron botones); el `JOptionPane`
saca sus botones del locale de la JVM y el runner no tiene español; y la suite
en una sola JVM en paralelo castiga a cualquier componente que mida contra
`UIManager` en su layout.

**Lo que enseñó la quinta tanda:** un worker que termina sin cambios pierde su
worktree, y retomado por mensaje trabaja en el checkout principal: se lanza uno
nuevo. Un worker se negó con razón a implementar las dinámicas escritas porque
el manual dice lo contrario: el oráculo manda sobre el brief. Dos PRs se lastimaron por comandos encadenados sin condición: uno se mergeó con
el CI rojo y a otro se le borró la branch remota con el CI rojo (GitHub cierra
el PR). El merge y el borrado se gatean con el exit code de `gh pr checks` y
con el estado MERGED. Y dos ramas que borran o renombran un color compartido
(`KNOB_BODY`) chocan semánticamente aunque git no vea conflicto: el CI del PR
es el que lo dice, y por eso se mira antes de mergear.

**Lo que enseñó la primera tanda:** el `AcceleratorGuard` de la etapa anterior era él
mismo una interfaz que mentía: ponía una acción vacía en vez de sacarle la tecla
al `JScrollPane`, y su test verificaba que el scroll ya no la atendiera, no que
el atajo funcionara después. La auditoría de uso real lo encontró porque despacha
la tecla de verdad. `jsvg` resuelve `currentColor` desde `Graphics2D.getColor()`
al renderizar, no desde el componente. Un conmutable que no lee el estado real al
construirse miente igual que una preferencia sin lector. Y una captura del
`MainFrame` sin `Theme.install()` muestra Metal, no la app: el harness construye
la ventana sin tema y hay que instalarlo como hace `App.main()`.

## Stage: English codebase and i18n (started 2026-09-10)

This section, and everything written after it, is in English. The older
sections get translated in phase 5.

### Goal

Delete every comment that is not an external contract. Move the comments that
remain, identifiers, test names, internal messages, docs and commits to English.
Ship the user interface in both Spanish and English.

### Decisions (2026-09-10)

- **Comments that stay** document a contract with something outside our code:
  file formats and protocols (GP3/4/5, PowerTab, TabEdit, MIDI, MusicXML, PDF,
  BMP, WAVE, SMuFL), plus JDK, Swing, FlatLaf, Gervill, OS or CI behavior that
  forces a line that would otherwise look odd. Those comments get translated to
  English. Javadoc on our own types goes, even on public ones.
- **Lost intent:** if a deleted comment said something the name did not, and a
  rename is obvious, the rename goes in the same structural commit. No Extract
  Method.
- **In English:** the comments that remain, identifiers and test method names,
  exception and log messages that never reach the user, docs (README, this
  plan, audits, LEEME), and every commit and PR description from now on. The
  CHANGELOG already released stays as it is.
- **Never renamed:** persisted `java.util.prefs` keys, files written to disk
  (the recovery file), `.tabpro` JSON fields and resource paths. The JUnit tag
  `integracion` does get renamed in phase 2, together with the poms and CI.
- **i18n:** the base bundle is English, and `es_*` system locales get Spanish.
  Any other system language falls back to English. Preferences offers
  Language (Automatic / Español / English), applied on restart.
- **Flow unchanged:** workers write the code in their own worktrees, one PR per
  module and change type, and a PR merges only when `gh pr checks` is green.

### Phases and order

1. **Comments** (`refactor`, `build`, `ci`; no release). Seven parallel scopes
   that share no files: core, format, midi, app, ui/dialogs,
   ui/score+instruments+harmony+tracks+page+print, and the rest of ui. Build and
   CI files are an eighth scope. Obvious renames whose references cross a scope
   are collected and applied in one follow-up PR.
2. **Identifiers and test names in English** (`test`, `refactor`; no release),
   per module, after phase 1 merges. The `integracion` tag is renamed here.
3. **Internal messages in English** (`refactor`): exception and log text that
   never reaches the user. Text that does reach the user moves to the bundles in
   phase 4.
4. **i18n.** First the infrastructure, with the locale pinned to Spanish. Then
   extraction by area, writing both bundles. Last, the switch: locale
   resolution, Preferences > Language, and English as the base. Only that last
   PR is a `feat`, so it is the only one that releases and no version ships a
   half-translated UI.
5. **Docs in English** (`docs`): README (keeping the version mentions that
   `scripts/prepare-release.sh` rewrites), this plan, the audits and the LEEME
   files.

### Phase 4 design (from the UI text inventory, 2026-09-10)

What the inventory found:
- `ui/actions/Commands` defines every command with a stable id (`file.new`)
  next to its Spanish label; `MenuBar` and `ToolBars` read from it.
- `ui/dialogs/style/Labels` is the single place that turns a domain type into
  Spanish text.
- Spanish display text lives inside the domain too: `ScaleLibrary` and
  `TuningLibrary` use the Spanish name as the only identifier, `PercussionKit`
  maps GM numbers to Spanish names, `Wah.label()` is painted on the score, and
  `ScoreInfo`, `ScoreDocument` and `StatusInfo` each carry "Sin título".
  `ChordType`, `Dynamic`, `HarmonicType`, `OctaveMark` and the GM `Instruments`
  are already language neutral.
- Some text becomes score data: `TrackDto.tuningName` is persisted in `.tabpro`
  files, six importers name tracks "Pista N", and the `PageElement` header and
  footer defaults are stored once inserted.
- `DialogShell` hardcodes Aceptar/Cancelar/Cerrar. About 11 places call
  `JOptionPane` directly and about 7 use `JFileChooser`; both follow the JVM
  locale today. The JDK ships Spanish Swing resources (`basic_es`).
- Some exception messages reach the user: five catch sites in `MainFrame`, plus
  the ASCII and MIDI import/export dialogs, some of them concatenated
  (`"No se pudo imprimir: " + e.getMessage()`).
- `MnemonicAssigner` derives mnemonics from the label text, and `MenuBarTest`
  checks the real menu bar for collisions. Accelerators are key codes, so they
  do not depend on the language.
- `AccentedLiteralsTest` scans Java literals, and the 26 audit tests find
  components by their Spanish text.

Decisions:
- **Bundles by area** in tabpro-ui under `com/gstncaruso/tabpro/ui/i18n/`: one
  English base file and one `_es` file per area, in UTF-8. The infrastructure PR
  creates every area file empty, so extraction PRs only append to their own
  files and can run in parallel without conflicts. Keys are dotted and named by
  intent. Placeholders use `MessageFormat`.
- **`Texts`** is the only reader of the bundles. It loads them with
  `ResourceBundle.Control.getNoFallbackControl(FORMAT_PROPERTIES)`, so a
  Spanish JVM default can never leak into English. The process language is set
  once at startup. Until the switch PR it stays pinned to Spanish, which makes
  every extraction PR a `refactor` with no visible change and lets the existing
  tests keep asserting Spanish.
- **The domain loses its display text:** scales, tunings and percussion get a
  language-neutral id, and `Labels` maps ids to keys. `.tabpro` files that
  store a Spanish `tuningName` keep opening with the same tuning.
- **Error dialogs** map the failure to a key in the UI. The exception's own
  message stays internal, and phase 3 leaves user-facing messages alone until
  this phase replaces them.
- **Switch PR (`feat`):** a `Language` (Automatic / Español / English) stored
  under a new key, resolved as `es*` → Spanish and anything else → English.
  Preferences gets the option with a note that it applies after a restart.
  `App.main` installs the language and calls `Locale.setDefault` before
  `Theme.install()`, so `JOptionPane` and `JFileChooser` follow it. The PR also
  adds a key-parity test between the two bundles, an English mnemonic-collision
  test, `AccentedLiteralsTest` rescoped to the `_es` files, and a guardian that
  no Spanish UI literal remains in main code.

Slices, in order (4.2 to 4.8 can run two at a time once 4.1 has merged):

| Slice | Content |
|---|---|
| 4.1 | `Texts`, the empty area bundles, `DialogShell` buttons |
| 4.2 | `Commands`, `MenuBar`, `ToolBars` |
| 4.3 | `Labels`, plus display text out of core (scales, tunings, percussion, wah, untitled) |
| 4.4 | dialogs, first half |
| 4.5 | dialogs, second half |
| 4.6 | status bar, score painters, page, print, instruments, harmony, tracks, browser, sound |
| 4.7 | `MainFrame`, the `JOptionPane` call sites, error dialogs |
| 4.8 | default track names in importers, page element defaults |
| 4.9 | the switch (`feat`) |

Phase 1 result: comment lines in Java went from 6775 to 1088, all in English and all documenting an external contract.

What phases 2 and 3 found that the inventory missed:
- The domain carries Spanish display labels in 21 core files, not just the four
  listed above. They include `bars/{KeySignature,DirectionJump,LineBreak,TripletFeel}`,
  `effects/{BeamBreak,StemOverride,GraceTransition,Ornament,SoundParameter,BendType,
  PickstrokeDirection,SlideType,StrokeDirection}`, `DiagramPlacement`, `VoicePart`,
  `ScoreInfo.heading()/credits()` and the `"Personalizada"` tuning.
- User-visible text also lives outside ui:
  - the sound bank `status()` of `SoundFontBank`/`SoundFontSynthesizer`, shown by MIDI Setup,
  - the export warnings of `GuitarProExporter`,
  - the `ScoreFileException` messages of every reader and writer in core, format and midi,
    including `ScoreExchange.notSupported`.
- Default names that become score data: `Score.blank()`'s `"Guitarra"`, the
  importers' `"Pista N"` and the Guitar Pro chord reader's `"Acorde"`.

Refined slices, one bundle area each (the area files exist from 4.1). Work runs in
three waves; slices in the same wave run in parallel because they share no files.

| Slice | Area | Content | Wave |
|---|---|---|---|
| 4.2 | `menus` | `Commands`, `MenuBar`, `ToolBars` | A |
| 4.3 | `domain` | enum labels in core `bars`/`effects`, `VoicePart`, `DiagramPlacement`, wah; core keeps only the constants, `Labels` maps them | A |
| 4.5 | `edit_dialogs` | note, beat, effects, bar, symbol, lyrics and marker dialogs | A |
| 4.7 | `views` | status bar, score painters, page, print, instruments, harmony, tracks, browser, percussion, sound panels, sound bank status | A |
| 4.4 | `library` | tunings, scales and percussion get ids (`.tabpro` files with a Spanish `tuningName` keep opening), `"Personalizada"`, `ScoreInfo` heading, `"Sin título"` | B, after 4.3 (both touch `Labels`) |
| 4.6 | `score_dialogs` | track, score info, page setup, preferences, MIDI, ASCII, wizards, metronome, tuner dialogs | B |
| 4.8 | `window` | `MainFrame`, the `JOptionPane` call sites, error dialogs; `ScoreFileException` carries a structured reason that the UI maps to a key; `GuitarProExporter` warnings | B |
| 4.9 | `defaults` | default track and chord names and `PageElement` defaults, handed to importers and `Score.blank()` from the app | C |
| 4.10 | — | the switch (`feat`): `Language` preference, resolution, `Locale.setDefault`, English mnemonic collisions, `AccentedLiteralsTest` on the `_es` files, guardian against Spanish UI literals in Java | C |

Waves as run: A = 4.2 + 4.3; B = 4.4 + 4.5; C = 4.7 + 4.8; D = 4.6 + 4.9 (4.6 needs 4.8's `ErrorTexts`, 4.9 touches importers that 4.8 edits); E = 4.10. Slice 4.10 must install the language before any UI class loads, because `ScoreDocument.UNTITLED` is resolved at class load.

### How to resume without context

Read this section and the table below, then take the first row that is not
merged. If the row is not started, brief a `worker` with its goal, branch,
scope, the decisions above and the trailer. If the worker finished, open the PR
with what changed and why, chain `gh pr checks N && gh pr merge N --squash`,
delete the remote branch only once the PR shows MERGED, and update this table.
Phases 2 to 5 start only after the previous phase has fully merged, because
their changes touch the same lines.

### State

| Item | Branch | PR | State |
|---|---|---|---|
| Stage plan | `docs/plan-english-codebase-and-i18n`, `docs/plan-i18n-design` | #192 | merged |
| 1 · core comments | `refactor/core-comments` | #200 | merged |
| 1 · format comments (two passes) | `refactor/format-comments` | #202 | merged |
| 1 · midi comments | `refactor/midi-comments` | #195 | merged |
| 1 · app comments | `refactor/app-comments` | #196 | merged |
| 1 · ui/dialogs comments | `refactor/ui-dialogs-comments` | #197 | merged |
| 1 · ui/score and neighbors comments | `refactor/ui-score-comments` | #198 | merged |
| 1 · rest of ui comments | `refactor/ui-rest-comments` | #199 | merged |
| 1 · build and CI files in English | `ci/build-files-in-english` | #193 | merged |
| 1 · cross-scope renames | — | — | not needed: no worker found one |
| 2 · midi identifiers and test names | `test/midi-english-names` | #201 | merged |
| 2 · core identifiers and test names | `test/core-english-names` | #205 | merged |
| 2 · app identifiers, `integracion` → `integration` tag | `test/app-english-names` | #206 | merged |
| 2 · ui/dialogs, rest of ui, ui/score group | `test/ui-*-english-names` | #204, #207, #208 | merged |
| 2 · format identifiers and test names | `test/format-english-names` | #210 | merged |
| 3 · midi, core, rest of ui, ui/dialogs, app, ui/score group | `refactor/*-internal-strings-in-english` | #209, #211, #212, #213, #214, #215 | merged |
| 3 · format, with `StrokeDto.rasgueado` → `strummed` keeping its JSON key | `refactor/format-internal-strings-in-english` | #216 | merged |
| 4.1 · `Texts`, area bundles, `DialogShell` buttons (a locale-dependent test fixed with `@Isolated`) | `refactor/i18n-infrastructure` | #218 | merged |
| CI · `tests.locale` property, suite runs under es_ES too | `ci/test-suite-under-both-locales` | #219 | merged |
| 4.2 · `menus`: 238 keys (commands, menu bar, toolbars) | `refactor/i18n-menus` | #220 | merged |
| 4.3 · `domain`: 18 core and 8 ui enums lose `label()`, 154 keys | `refactor/i18n-domain-labels` | #221 | merged |
| 4.5 · `edit_dialogs`: effects, note, measure, markers, paste, instrument, help, 77 keys | `refactor/i18n-edit-dialogs` | #223 | merged |
| 4.4 · `library`: `TuningName` (library/user/custom), scale and GM ids, 146 keys; `.tabpro` keeps storing the Spanish tuning name | `refactor/i18n-library-names` | #224 | merged |
| 4.7 · `views` | `refactor/i18n-views` | — | in progress |
| 4.8 · `window`, structured `ScoreFileException` problems | `refactor/i18n-window-and-errors` | — | in progress |
| 4.6, 4.9, 4.10 | — | — | pending (wave D after 4.7 and 4.8, then the switch) |
| test · deterministic multi-port seek in `MidiPlayerTest` (flaked in the es_ES run) | `test/midi-deterministic-multi-port-seek` | — | in progress |
| 5 · audits and fixture READMEs in English, renamed `docs/audit-*.md` | `docs/audits-in-english` | #222 | merged |
| 5 · README and this plan in English | — | — | pending (after 4.10) |
