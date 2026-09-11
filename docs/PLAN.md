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

### Estado

| Ítem | Branch | PR | Estado |
|---|---|---|---|
| Plan de la etapa y actualizaciones | `docs/plan-etapa-visual`, `docs/estado-etapa-visual` | #112, #120 | mergeado |
| A · auditoría de uso real, 15 de 15 capítulos (47 OK / 7 MIENTE / 1 AUSENTE; Print e Import/Export todo OK) | `docs/auditoria-uso-real`, `docs/auditoria-uso-real-print-e-import-export` | #114, #129 | mergeado |
| A · el CI corre el harness bajo Xvfb | `ci/harness-con-display` | #117 | mergeado |
| A · fix: la configuración del metrónomo se abre desde Sonido (hallazgo 6) | `fix/configuracion-del-metronomo-alcanzable` | #119 | mergeado |
| A · fix: los siete atajos que Swing interceptaba (hallazgos 1–5) | `fix/atajos-que-swing-interceptaba` | #121 | mergeado |
| B1 · íconos genéricos desde Tabler (`SvgIcon` + `jsvg`) | `feat/iconos-tabler` | #113 | mergeado |
| B3 · símbolos musicales desde Bravura (`GlyphIcon`) | `feat/iconos-bravura` | #118 | mergeado |
| B4 · barras con el orden y los grupos de GP5, efectos abajo de la partitura | `feat/barras-como-gp5` | #127 | mergeado |
| B4 · dinámicas ppp…fff y banco de sonidos como botones | `feat/dinamicas-y-banco-de-sonidos-en-las-barras` | #132 | mergeado |
| B4 · las barras fijan sus colores desde la paleta, con o sin tema | `fix/barras-superiores-apagadas` | #133 | mergeado |
| B4 · selector de pista por número (fila 1) | `feat/selector-de-pista-por-numero` | — | en curso |
| C1 · nombre accesible y tooltip en todo control (`AccessibilityWalker`) | `feat/nombres-accesibles` | #115 | mergeado |
| C1 · siete diálogos separables para el recorredor | `refactor/dialogos-separables-para-el-recorredor` | #128 | mergeado |
| C2 · mnemónicos Alt+letra en menús y formularios | `feat/mnemonicos` | #124 | mergeado |
| C3 · teclado y foco visible en perilla, diapasón, teclado y grilla | `feat/teclado-en-los-componentes-custom` | #122 | mergeado |
| C3 · marcadores, percusión, bends y afinador por teclado; Ctrl+F6 cede el foco | `feat/teclado-en-los-custom-menores-y-salida-de-foco` | #125 | mergeado |
| C3 · el test de Ctrl+F6 inyecta la travesía de foco | `test/ctrl-f6-sin-el-focus-manager-global` | #130 | mergeado |
| C4 · contraste WCAG AA en las dos paletas, con tests | `fix/contraste-wcag` | #116 | mergeado |
| C4 · contraste en la mesa de mezcla y los cuatro mástiles | `fix/contraste-en-la-mesa-y-los-mastiles` | #126 | mergeado |
| C5 · Preferencias > Accesibilidad (letra, alto contraste, sin animaciones) | `feat/preferencias-de-accesibilidad` | #123 | mergeado |
| D · README y capturas con el tema real | `docs/readme-y-capturas-de-la-etapa-visual` | #131 | mergeado |
| B4 · selector de pista por número (fila 1) | `feat/selector-de-pista-por-numero` | #136 | mergeado |
| B4 · combo de zoom editable con el porcentaje | `feat/combo-de-zoom-como-gp5` | #140 | mergeado |
| B4 · editar el marcador del cursor desde menú y barra | `feat/editar-el-marcador-del-cursor` | #144 | mergeado |
| C4 · la cejilla en la paleta de cada mástil | `fix/la-cejilla-cumple-el-contraste-en-cada-mastil` | #135 | mergeado |
| A · impresión verificable hasta el `PrinterJob` (costura `Printing`) | `refactor/impresion-con-costura-para-el-printer-job` | #137 | mergeado |
| D · auditoría visual zona por zona contra la captura de GP5 (diez diferencias medidas) | — | — | hecha, no se commitea |
| D · diapasón con veta y trastes metálicos, teclado con punto y bisel | `feat/diapason-y-teclado-como-gp5` | #138 | mergeado |
| D · barra de estado con los seis paneles hundidos de GP5 | `feat/barra-de-estado-como-gp5` | #139 | mergeado |
| D · mesa de mezcla con deslizadores, números planos y columnas de GP5 | `feat/mesa-de-mezcla-como-gp5` | #141 | mergeado |
| D · vista global con regla de compases, marcadores en rojo y cabecera 1,3× | `feat/vista-global-como-gp5` | #142 | mergeado |
| D · bandas de título con ✕ en diapasón y teclado | `feat/paneles-de-diapason-y-teclado-con-titulo` | #143 | mergeado |
| D · README y capturas de la segunda tanda, sobre el `main` final | `docs/readme-y-capturas-de-la-segunda-tanda` | #152 | mergeado |
| D · el combo de zoom conserva su ancho | `fix/el-combo-de-zoom-no-se-estira` | #146 | mergeado |
| E · auditoría de la partitura contra los ejemplos de notación del manual (diez ítems medidos) | — | — | hecha, no se commitea |
| E · nombres de cuerda apagados por defecto, como GP5 | `feat/nombres-de-cuerda-opcionales-por-pista` | #147 | mergeado |
| E · tempo inicial escrito, tempo y números de compás en rojo | `feat/tempo-inicial-y-numeros-de-compas-como-gp5` | #148 | mergeado |
| E · brecha pentagrama–tab medida (36 px) | `feat/brecha-pentagrama-tab-como-gp5` | #149 | mergeado |
| E · selección amarilla y cuadrado de color del marcador | `feat/seleccion-amarilla-y-marcador-con-color-como-gp5` | #150 | mergeado |
| E · el cuadrado del marcador no pisa el nombre de pista | `fix/el-cuadrado-del-marcador-no-pisa-el-nombre-de-pista` | #151 | mergeado |
| E · dinámicas escritas bajo la nota | — | — | **no aplica**: el manual dice que GP5 no las muestra en la partitura; el modo F11 ya existe |
| B4 · botón de digitación de mano derecha | `feat/boton-de-digitacion-de-mano-derecha` | #154 | mergeado |
| F · auditoría de robustez con el corpus real (71 archivos: 68 abren, 68 pasan render, export y reapertura) | `docs/auditoria-corpus` | #155 | mergeado |
| F · fix: la partitura recibe las notificaciones del `Editor` en el EDT y no scrollea con viewport 0×0 | `fix/las-notificaciones-del-editor-llegan-por-el-edt` | #156 | mergeado |
| F · todos los componentes Swing escuchan al `Editor` por el adaptador del EDT | `refactor/todos-los-componentes-escuchan-al-editor-por-el-edt` | #158 | mergeado |
| G · auditoría visual de los diálogos contra las capturas del manual (quince ítems) | — | — | hecha, no se commitea |
| G · «Documento centrado» al imprimir | `feat/documento-centrado-al-imprimir` | #159 | mergeado |
| G · letra en cinco pestañas con área multilínea | `feat/letra-multilinea-como-gp5` | #160 | mergeado |
| G · combos y listas con etiquetas en castellano (`Labels`, oráculo en el recorredor) | `fix/los-combos-muestran-etiquetas-legibles` | #161 | mergeado |
| G · secciones de formulario como cajas de grupo con título | `feat/cajas-de-grupo-con-titulo-en-los-formularios` | #162 | mergeado |
| G · lista de marcadores como tabla de gestión | `feat/lista-de-marcadores-con-tabla-como-gp5` | #163 | mergeado |
| G · propiedades de pista en dos columnas, «Forzar canales 11 a 16» y estilos de diagramas | `feat/propiedades-de-pista-como-gp5` | #164 | mergeado |
| G · asistente de percusión en grilla de cuatro columnas | `feat/asistente-de-percusion-en-grilla-como-gp5` | #165 | mergeado |
| F · el harness falla limpio en vez de colgar la suite | `test/el-harness-falla-limpio-en-vez-de-colgarse` | #166 | mergeado |
| G · el oráculo de combos no exime renderers propios; Orientación y Papel con etiquetas | `fix/el-oraculo-de-combos-no-exime-renderers-propios` | #168 | mergeado |
| G · constructor de acordes: inversión separada del bajo, posiciones y cejilla con radios | `feat/constructor-de-acordes-como-gp5` | #169 | mergeado |
| G · importar MIDI con escucha previa, marcar todas y cuantización por radios con efecto real | `feat/importar-midi-como-gp5` | #170 | mergeado |
| G · herramienta de escalas con listas, diagrama de grados y escuchar | `feat/herramienta-de-escalas-como-gp5` | #171 | mergeado |
| F · `ScoreDocumentTest` sin las Preferences reales; tests de Preferences serializados | `test/metronomo-y-documento-sin-flakiness` | #172 | mergeado |
| G · separar «Tipo» de acorde en extensión, alteraciones y «add» (toca nombrado, diagramas y formatos GP) | — | — | anotado, pieza grande |
| G · botones de ayuda «?» y «Aplicar» en los diálogos | — | — | descartado: no hay ayuda que mostrar |
| H · auditoría visual fresca de toda la app tras los 61 PRs (diez hallazgos, más quince de la sub-auditoría de ventana) | — | — | hecha, no se commitea |
| H · barras de unión inclinadas, con «Forzar barras horizontales» por pista | `feat/barras-de-union-inclinadas-como-gp5` | #174 | mergeado |
| H · la vista previa ASCII se verifica con el diálogo real (era artefacto del fixture) | `fix/la-vista-previa-ascii-muestra-la-tablatura` | #175 | mergeado |
| H · mover el cursor limpia la selección, Shift extiende, Ctrl+A se pinta | `fix/la-seleccion-se-limpia-al-mover-el-cursor` | #176 | mergeado |
| H · la mesa muestra puerto y canal con dos dígitos y el instrumento entero | `fix/la-mesa-muestra-los-canales-y-el-instrumento-enteros` | #177 | mergeado |
| H · textos de efecto sin pisar el traste, marcador de 8×12, staccato opuesto a la plica | `fix/textos-de-efecto-marcador-y-staccato-como-gp5` | #178 | mergeado |
| H · diapasón y teclado cerrados por defecto con estado recordado; Escalas con Do mayor | `fix/valores-iniciales-como-gp5` | #179 | mergeado |
| H · tildes y eñes en 107 literales de 47 archivos, con guardián | `fix/tildes-en-los-textos-de-la-interfaz` | #180 | mergeado |
| F · la grilla de percusión fija su tamaño de celda (flaky bajo la suite paralela) | `test/la-grilla-de-percusion-sin-flakiness` | #181 | mergeado |
| H · ningún diálogo supera la pantalla; Configurar página y MIDI enteros; MIDI sin `JOptionPane` dependiente del locale | `fix/configurar-pagina-y-midi-entran-en-pantalla` | #182 | mergeado |
| H · palanca con sus seis tipos y códigos GP5 exactos; opciones de let ring/palm mute/dinámica con título y foco propios | `fix/palanca-con-sus-tipos-y-opciones-por-pestana` | #183 | mergeado |
| F · guardián: todo test que mute el look and feel lleva `@Isolated` | `test/los-tests-que-instalan-un-tema-corren-aislados` | #184 | mergeado |
| F · red de humo permanente: cada fixture del repo se abre, renderiza, exporta y reabre en cada CI | `test/humo-permanente-sobre-los-fixtures` | #186 | mergeado |
| I · auditoría de rendimiento con partituras de 100/300/600 compases (cinco hotspots medidos) | — | — | hecha, no se commitea |
| I · la partitura pinta sólo hojas y sistemas a la vista; layout memoizado (modo Página: 28 s → 10 ms) | `perf/la-partitura-pinta-solo-lo-visible` | #187 | mergeado |
| I · mover el cursor repinta sólo su zona; el `Editor` avisa qué cambió (407 ms → 19 ms por flecha) | `perf/mover-el-cursor-repinta-solo-lo-que-cambio` | #188 | mergeado |
| I · cada hoja itera sólo sus compases; acordes bajo el título cacheados | `perf/el-export-pinta-solo-los-compases-de-cada-hoja` | #189 | mergeado |
| I · el PDF y la imagen codifican cada hoja en bloque (el 90 % del tiempo de export) | `perf/el-pdf-y-la-imagen-codifican-cada-hoja-en-bloque` | — | en curso |
| G · «Forzar barras horizontales» (pide barras inclinadas, pieza grande de render) | — | — | anotado |

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
