# Real-use audit — tabpro against the Guitar Pro 5.2 manual

The previous audits (`audit-manual.md`) were static: manual against code, and they took the
manual as covered. This audit exercises the **real user path** on a real `MainFrame`,
instantiated with `setVisible(true)` (Swing needs to register the menu accelerators in its
`KeyboardManager`, which `pack()` alone does not achieve): the real `JMenuItem` and its `Action`,
the real `KeyEvent` dispatched onto the real `ScoreCanvas` (never invoking the `Action` by hand),
the real button on a toolbar, and the real controls of a real modal dialog (detected via
`WINDOW_OPENED`, without `Robot`). The harness lives in
`tabpro-app/src/test/java/com/gstncaruso/tabpro/app/audit/`, tagged `@Tag("integration")`.

Audited code: branch `refactor/impresion-con-costura-para-el-printer-job` on top of `main`,
version 0.39.1. Harness: 15 classes, 98 tests, all green together (`mvn -B -pl tabpro-tests -am
test -Dtests.headless=false -Dtests.excluded.groups=ninguno -Dgroups=integration`).
`mvn -B verify` with defaults: **BUILD SUCCESS, 2665 tests, ~6 s** (identical before and after).

---

## Coverage by chapter

| Chapter (manual line) | OK | LIES | MISSING | NOT VERIFIABLE |
|---|---|---|---|---|
| Write a Score (481) | 12 | 3 | 0 | 0 |
| Add Symbols (961) | 3 | 0 | 0 | 0 |
| Keyboard Shortcuts (Reference, pp. 79-81) | 12 | 3 | 0 | 0 |
| Play the Score (2087) | 3 | 0 | 0 | 0 |
| Insert Parameter Changes (1340) | 1 | 1 | 0 | 0 |
| Work with a Score (1723) | 2 | 0 | 0 | 0 |
| Configure the Display (1886) | 4 | 0 | 0 | 0 |
| Cut, Copy and Paste (1478) | 2 | 0 | 0 | 0 |
| Add Lyrics (1377) / Add Markers (1448) | 2 | 0 | 0 | 0 |
| Wizards (1587) | 1 | 0 | 0 | 0 |
| Percussion (1670) | 2 | 0 | 0 | 0 |
| Configure the Sound (1945) | 1 | 0 | 1 | 0 |
| Tools for the Guitarist (2665) | 2 | 0 | 0 | 0 |
| Print a Score (2207) | 4 | 0 | 0 | 0 |
| Import / Export a Score (2293 / 2506) | 17 | 0 | 0 | 0 |
| **Total** | **68** | **7** | **1** | **0** |

(The "Keyboard Shortcuts" row does not separately count the exhaustive sweep test
`theOnlyFiveKeysThatScrollPaneAndSplitPaneAlreadyOccupiedAreTheDocumentedOnes`, which does not
verify one specific item from the manual but rather that **there is no sixth collision** of the
same kind between the ~150 shortcuts in the catalog and the window's real
`JScrollPane`/`JSplitPane`.)

---

## Findings (only what did not score OK)

### 1. Ctrl+Home and Ctrl+End go silent with the score focused — LIES · small
**Manual:** Keyboard Shortcuts, Navigation — "[Ctrl] Home" = first measure, "[Ctrl] End" =
last measure.
**Path:** keyboard shortcut, score focused (the normal situation while editing).
**Expected:** move the cursor to the first/last measure.
**Observed:** nothing happens — the cursor does not move. The same command **does** work through
the menu (Compás > Primer compás / Último compás — Measure > First measure / Last measure).
**Evidence:** `WriteAScoreAuditTest.ctrlHomeQuedaMudoAunqueElMenuPrimerCompasFunciona`,
`WriteAScoreAuditTest.ctrlFinQuedaMudoAunqueElMenuUltimoCompasFunciona`, and the sweep
`KeyboardShortcutsAuditTest.theOnlyFiveKeysThatScrollPaneAndSplitPaneAlreadyOccupiedAreTheDocumentedOnes`.
**Suspected cause:** `tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/actions/AcceleratorGuard.java:44-46`
(the `block` method). The `JScrollPane` that wraps the score ships out of the box with a
`"scrollHome"` / `"scrollEnd"` action bound on its `WHEN_ANCESTOR_OF_FOCUSED_COMPONENT`, and
`AcceleratorGuard.letCommandsWin` replaces that action with one that **does nothing**
(`inputMap.put(accelerator, name)` + `NO_HACE_NADA`), instead of removing the key from the map.
Swing finds that empty action on the `JScrollPane`, considers it handled and **never gets to
look at** the `WHEN_IN_FOCUSED_WINDOW` where the menu's real accelerator lives. The existing unit
test (`AcceleratorGuardTest`) only checks that the isolated `JScrollPane` stops responding to
`"scrollHome"`; it never checks that the real shortcut keeps working afterward.

### 2. F6 and F8 go silent with the score focused — LIES · small
**Manual:** Keyboard Shortcuts — "F6" = Track properties, "F8" = Page setup.
**Path:** keyboard shortcut, score focused.
**Expected:** open the corresponding real dialog.
**Observed:** no dialog opens. Through the menu, both work.
**Evidence:** `KeyboardShortcutsAuditTest.f6NoAbreLasPropiedadesDeLaPistaConLaPartituraEnfocada`,
`KeyboardShortcutsAuditTest.f8NoAbreConfigurarPaginaConLaPartituraEnfocada`.
**Suspected cause:** the same mechanism as finding 1, but with the `JSplitPane` that separates the
score from the mixer (built-in `"toggleFocus"` / `"startResize"`),
`AcceleratorGuard.java:44-46`.
**Size:** small (the same fix that closes finding 1 closes all five at once).

### 3. Ctrl+Tab goes silent with the score focused — LIES · small
**Manual:** Keyboard Shortcuts — "[Ctrl] Tab" = next marker.
**Path:** keyboard shortcut, score focused.
**Expected:** move the cursor to the next marker.
**Observed:** it does not move. Through the menu (Marcador > Marcador siguiente — Marker > Next
marker) it does work.
**Evidence:** `KeyboardShortcutsAuditTest.ctrlTabQuedaMudoAunqueElMenuMarcadorSiguienteFunciona`.
**Suspected cause:** the same mechanism, the `JSplitPane`'s built-in `"focusOutForward"`,
`AcceleratorGuard.java:44-46`. **Size:** small (same fix as 1 and 2).

### 4. F10 does not open "Cambio de parámetros" with the score focused — LIES · small-medium
**Manual:** Insert Parameter Changes (line 1340) — "F10" opens Note > Mixing table
(Nota > Mesa de mezcla).
**Path:** keyboard shortcut, score focused.
**Expected:** open the real parameter-change dialog.
**Observed:** no dialog opens, and the model does not change. Instead, the real `JMenuBar` is
activated for arrow-key navigation (its `SelectionModel` moves from `-1` to `0`, confirmed, and
in a separate diagnostic the first menu's dropdown was seen opening, a
`javax.swing.Popup$HeavyWeightWindow`). Through the menu (Nota > Mesa de mezcla) it works
perfectly.
**Evidence:**
`InsertParameterChangesAuditTest.f10ConLaPartituraEnfocadaActivaElMenuEnVezDeAbrirElCambioDeParametros`
(the menu path, with the real dialog and its real checkboxes/spinners, is in
`elMenuCambioDeParametrosAbreElDialogoRealYElVolumenElegidoLlegaAlModelo`).
**Suspected cause:** this is not a tabpro wiring bug but a clash with a Swing/FlatLaf convention
(F10 activates the `JMenuBar`, as on Windows/Motif), in `Commands.java`
(`note.mixTableChange` uses `withAccelerator("F10")`). The fix is not as direct as the previous
ones: either the accelerator changes (which would break parity with the Guitar Pro 5 manual) or a
custom `KeyEventDispatcher`/`KeyEventPostProcessor` gets installed with higher priority than the
L&F's. **Size:** small-medium.

### 5. Tab does not toggle tablature/staff with the score focused — LIES · small
**Manual:** line 780 (quoted in `Editor.toggleNotation()`) — Tab toggles editing between
tablature and staff without moving the cursor.
**Path:** raw key (it has no command in the catalog; `KeyboardEditing` resolves it directly on
`ScoreCanvas`).
**Expected:** `cursor().notation()` switches from `TABLATURE` to `STANDARD` (or vice versa).
**Observed:** it does not switch. The binding exists (`KeyboardEditing.install` leaves
`"pressed TAB"` on `ScoreCanvas`'s `WHEN_FOCUSED`, confirmed), but it never runs.
**Evidence:** `WriteAScoreAuditTest.tabCrudoNoCambiaDeNotacionPorQuedarseConElFocoAntes`.
**Suspected cause:** `ScoreCanvas` (constructor,
`tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/score/ScoreCanvas.java`) never calls
`setFocusTraversalKeysEnabled(false)`: Tab is, out of the box, a focus-navigation key for any
`JComponent`, and that AWT machinery keeps the key before `KeyboardEditing`
(`tabpro-ui/.../tab/KeyboardEditing.java:64-77`, the `install` method) ever sees it.
**Size:** small.

### 6. The metronome volume dialog has no way to open — MISSING · small
**Manual:** Configure the Sound (line 1945) — metronome settings (on/off, volume).
**Path:** none. Verified by code inspection, not with a dynamic test (it is a missing feature,
not a lie: there is no control to press).
**Expected:** some menu, button or shortcut that opens `MetronomeDialog`.
**Observed:** `Ports.Dialogs.metronomeSettings()` is declared, **implemented** in
`MainFrame.Windows.metronomeSettings()` (it builds a real `MetronomeSettings` from the
`Transport` and calls `MetronomeDialog.ask`), but **no** `define(...)` in
`tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/actions/Commands.java` and no item in
`MenuBar.java` ever calls it. Confirmed with `grep -rn "metronomeSettings"` over the whole tree:
the only two occurrences are the interface and the implementation.
**Suspected cause:** a `define("sound.metronomeSettings", …, dialogs::metronomeSettings)` is
missing in `Commands.java` (near `sound.metronome`, line ~364) and its entry in `MenuBar.java`.
**Size:** small.

**Print a Score and Import / Export a Score add no new findings.** The two chapters that were
still uncovered (`PrintAuditTest`, `ImportExportAuditTest`) scored **OK** on the cases exercised:
Archivo > Imprimir (File > Print) and Configurar página (Page setup) open tabpro's real dialogs
with their real controls, and the chosen settings are applied; Open/Save/Save as/Open recent and
the six foreign formats the manual names (MIDI, ASCII, MusicXML, PowerTab, TablEdit, Guitar Pro)
plus WAVE/Image/PDF write or read a real file that the matching reader or writer recognizes.

**The real `PrinterJob`, which until version 0.39.1 had no seam, can now be verified end to
end.** `ScorePrinting` received the `PrinterJob` by calling `PrinterJob.getPrinterJob()` directly
(`print` and `configurePrinterPage`), so no test could hand `MainFrame` a fake `PrinterJob`, and
really pressing "Imprimir" or "Configurar…" would have opened a **native** operating-system
window — not a Swing `JDialog` — with a real risk of leaving the suite hanging. Now
`ScorePrinting` receives a `Printing` through its constructor
(`tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/print/Printing.java`): the small interface it
needs from `PrinterJob` (`setJobName`, `setPrintable`, `printDialog`, `print`, `defaultPage`,
`pageDialog`), with `SystemPrinting` as the real production implementation
(`MainFrame`/`App` build it) and `RecordingPrinting` as the fake one in tests, without changing
the app's behavior. With that seam in place:

- `ScorePrintingTest` (`tabpro-ui/src/test/.../print/ScorePrintingTest.java`) proves with the
  fake that the `Printable` reaching the `PrinterJob` is the one that paints the real score, that
  canceling the print dialog never reaches `printing.print()`, and that the `PageFormat` chosen
  under Configurar (Configure) is kept for the next print job (previously it was discarded
  unused).
- `PrintAuditTest` injects a fake `Printing` into the test `MainFrame` (the same pattern as
  `ScoreFiles`/`Player`/`Devices`) and now really presses "Imprimir" (Print) — there is no longer
  a need to close with "Cancelar" (Cancel) —: it proves that the page range chosen in tabpro's
  real dialog reaches the `Printable` that the (fake) `PrinterJob` receives exactly as chosen, and
  that this `Printable` still paints the real score.

---

## The harness

`tabpro-app/src/test/java/com/gstncaruso/tabpro/app/audit/`:

- `AuditSupport.java`: factory for a real `MainFrame` (with injectable fake
  `ScoreFiles`/`Player`/`Devices`/`Printing` — the last one, `RecordingPrinting`, is the fake
  `PrinterJob` that `PrintAuditTest` uses —, plus a `newFrame(Editor, ScoreFiles, ScoreExchange)`
  for tests that need a real `ScoreFiles`/`ScoreExchange` — Open/Save/Import/Export), traversal of
  the real component tree (`findComponent`, `findComponents`, `findMenuItem` — with the variant
  that searches inside one specific `JMenu`, for when the same label exists in both Importar and
  Exportar —, `findButton`, `findCheckBox`, `findRadioButton`, `tabContent`), a `repoFile` for the
  real fixtures of another module (`tabpro-format/src/test/resources/...`, resolved from
  `tabpro-tests`'s real working directory), real `KeyEvent` dispatch (`pressKey`, `typeChar`) and
  handling of real modal dialogs without `Robot` (`withDialog`, `dispatchKeyAndDetectDialog`): a
  global `AWTEventListener` grabs the `JDialog`'s `WINDOW_OPENED` — which Swing delivers inside
  the same nested loop that blocks `setVisible(true)` — and its real controls are touched right
  there. When a command opens more than one dialog in a chain (MIDI import, ASCII import/export,
  WAVE export), the same `onOpen` from `withDialog` is invoked once per real window that appears,
  in the order Swing opens them. `withDialog` also guarantees that an `onOpen` that throws, or
  that forgets to close the dialog, never leaves the suite hanging waiting on a `setVisible(true)`
  that will not return: it catches any real error from the callback, closes the dialog and
  rethrows it from the test thread (`AuditSupportWithDialogTest`, `@Tag("integration")`, covers
  all three cases).
- `TabEditMinimalFixture.java`: a minimal TEF3 file built by hand, with the same binary layout the
  real reader understands (`TabEditByteReader`), for the TablEdit import — which has no real
  sample in the repository (neither does `tabpro-format`, which builds its own the same way, by
  hand, in a test in another module, package-private and not reusable from here).
- One test file per chapter, all `@Tag("integration")` and `@ResourceLock(AuditSupport.SWING_LOCK)`.
- Two infrastructure changes in the root `pom.xml` (with the same default value as before, so
  `mvn -B verify` does not change):
  - `tests.headless` (default `true`) parametrizes surefire's `argLine`
    `-Djava.awt.headless`: menu accelerators resolve via `WHEN_IN_FOCUSED_WINDOW`, and Swing only
    registers them in its `KeyboardManager` when the window is truly *showing* —
    `pack()` without `setVisible(true)` is not enough.
  - `java.util.prefs.userRoot` and `java.io.tmpdir` isolated in the same `argLine`: the real
    `MainFrame` reads/writes the user's real `Preferences` and looks for its recovery file in the
    real `tmpdir`; without isolating them, a real recovery file triggers a real dialog on every
    test (this was reproduced once).
  - `@ResourceLock` with a key shared across the 15 classes: Swing has a single EDT per virtual
    machine and the suite runs classes in parallel — without the lock, two classes that open
    modal dialogs at the same time step on each other's global `AWTEventListener` (`Toolkit` does
    not tell which test each window belongs to) and the suite hangs (this was reproduced once,
    without the lock).

Running just the harness: `mvn -B -pl tabpro-tests -am test -Dtests.headless=false
-Dtests.excluded.groups=ninguno -Dgroups=integration`.
</content>
