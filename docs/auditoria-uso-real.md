# Auditoría de uso real — tabpro contra el manual de Guitar Pro 5.2

Las auditorías anteriores (`auditoria-manual.md`) fueron estáticas: manual contra código,
dieron el manual por cubierto. Esta auditoría ejercita el **camino real del usuario** sobre una
`MainFrame` real, instanciada con `setVisible(true)` (hace falta que Swing registre los
aceleradores de menú en su `KeyboardManager`, cosa que `pack()` solo no logra): el `JMenuItem`
real y su `Action`, el `KeyEvent` real despachado sobre el `ScoreCanvas` real (nunca invocando el
`Action` a mano), el botón real de una barra, y los controles reales de un diálogo modal real
(detectado por `WINDOW_OPENED`, sin `Robot`). El harness vive en
`tabpro-app/src/test/java/com/gstncaruso/tabpro/app/audit/`, tagueado `@Tag("integracion")`.

Código auditado: rama `docs/auditoria-uso-real` sobre `main` = `64f9ac5`, versión 0.31.0.
Harness: 13 clases, 54 tests, todos verdes juntos (`mvn -B -pl tabpro-tests -am test
-Dtests.headless=false -Dtests.excluded.groups=ninguno -Dgroups=integracion`).
`mvn -B verify` por defecto: **BUILD SUCCESS, 2240 tests, ~3 s** (idéntico antes y después).

---

## Cobertura por capítulo

| Capítulo (línea del manual) | OK | MIENTE | AUSENTE | NO VERIFICABLE |
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
| Print a Score (2207) | 0 | 0 | 0 | no cubierto |
| Import / Export a Score (2293 / 2506) | 0 | 0 | 0 | no cubierto |
| **Total** | **47** | **7** | **1** | **2 capítulos** |

(La fila "Keyboard Shortcuts" no cuenta aparte el test de barrido exhaustivo
`lasUnicasCincoTeclasQueElScrollPaneYElSplitPaneYaOcupabanSonLasDocumentadas`, que no verifica un
ítem puntual del manual sino que **no hay una sexta colisión** del mismo tipo entre los ~150
atajos del catálogo y el `JScrollPane`/`JSplitPane` reales de la ventana.)

---

## Hallazgos (sólo lo que no dio OK)

### 1. Ctrl+Home y Ctrl+Fin quedan mudos con la partitura enfocada — MIENTE · chico
**Manual:** Keyboard Shortcuts, Navigation — "[Ctrl] Home" = primer compás, "[Ctrl] End" =
último compás.
**Camino:** atajo de teclado, partitura enfocada (situación normal al editar).
**Esperado:** mover el cursor al primer/último compás.
**Observado:** no pasa nada — el cursor no se mueve. El mismo comando **sí** funciona por el
menú (Compás > Primer compás / Último compás).
**Evidencia:** `WriteAScoreAuditTest.ctrlHomeQuedaMudoAunqueElMenuPrimerCompasFunciona`,
`WriteAScoreAuditTest.ctrlFinQuedaMudoAunqueElMenuUltimoCompasFunciona`, y el barrido
`KeyboardShortcutsAuditTest.lasUnicasCincoTeclasQueElScrollPaneYElSplitPaneYaOcupabanSonLasDocumentadas`.
**Sospecha:** `tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/actions/AcceleratorGuard.java:44-46`
(método `block`). El `JScrollPane` que envuelve la partitura trae de fábrica un `"scrollHome"` /
`"scrollEnd"` en su `WHEN_ANCESTOR_OF_FOCUSED_COMPONENT`, y `AcceleratorGuard.letCommandsWin`
reemplaza esa acción por una que **no hace nada** (`inputMap.put(accelerator, name)` +
`NO_HACE_NADA`), en vez de sacarle la tecla al mapa. Swing encuentra esa acción vacía en el
`JScrollPane`, la da por atendida y **nunca llega a mirar** el `WHEN_IN_FOCUSED_WINDOW` donde
vive el acelerador real del menú. El test unitario existente
(`AcceleratorGuardTest`) sólo comprueba que el `JScrollPane` aislado deja de decir
`"scrollHome"`; nunca comprueba que el atajo real siga funcionando después.

### 2. F6 y F8 quedan mudos con la partitura enfocada — MIENTE · chico
**Manual:** Keyboard Shortcuts — "F6" = Propiedades de la pista, "F8" = Configurar página.
**Camino:** atajo de teclado, partitura enfocada.
**Esperado:** abrir el diálogo real correspondiente.
**Observado:** no se abre ningún diálogo. Por menú, los dos funcionan.
**Evidencia:** `KeyboardShortcutsAuditTest.f6NoAbreLasPropiedadesDeLaPistaConLaPartituraEnfocada`,
`KeyboardShortcutsAuditTest.f8NoAbreConfigurarPaginaConLaPartituraEnfocada`.
**Sospecha:** mismo mecanismo que el hallazgo 1, pero con el `JSplitPane` que separa la
partitura de la mesa de mezcla (`"toggleFocus"` / `"startResize"` de fábrica),
`AcceleratorGuard.java:44-46`.
**Tamaño:** chico (mismo fix que el hallazgo 1 arregla los cinco de una).

### 3. Ctrl+Tab queda mudo con la partitura enfocada — MIENTE · chico
**Manual:** Keyboard Shortcuts — "[Ctrl] Tab" = marcador siguiente.
**Camino:** atajo de teclado, partitura enfocada.
**Esperado:** mover el cursor al próximo marcador.
**Observado:** no se mueve. Por menú (Marcador > Marcador siguiente) sí funciona.
**Evidencia:** `KeyboardShortcutsAuditTest.ctrlTabQuedaMudoAunqueElMenuMarcadorSiguienteFunciona`.
**Sospecha:** mismo mecanismo, `"focusOutForward"` de fábrica del `JSplitPane`,
`AcceleratorGuard.java:44-46`. **Tamaño:** chico (mismo fix que 1 y 2).

### 4. F10 no abre "Cambio de parámetros" con la partitura enfocada — MIENTE · chico-mediano
**Manual:** Insert Parameter Changes (línea 1340) — "F10" abre Nota > Mesa de mezcla.
**Camino:** atajo de teclado, partitura enfocada.
**Esperado:** abrir el diálogo real de cambio de parámetros.
**Observado:** no se abre ningún diálogo, y el modelo no cambia. En cambio, el `JMenuBar` real
se activa para navegación con flechas (su `SelectionModel` pasa de `-1` a `0`, confirmado, y en
un diagnóstico aparte se vio abrirse el desplegable del primer menú, un
`javax.swing.Popup$HeavyWeightWindow`). Por menú (Nota > Mesa de mezcla) funciona perfecto.
**Evidencia:**
`InsertParameterChangesAuditTest.f10ConLaPartituraEnfocadaActivaElMenuEnVezDeAbrirElCambioDeParametros`
(el camino del menú, con el diálogo real y sus casillas/spinners reales, está en
`elMenuCambioDeParametrosAbreElDialogoRealYElVolumenElegidoLlegaAlModelo`).
**Sospecha:** no es un bug de wiring de tabpro sino un choque con una convención de
Swing/FlatLaf (F10 activa el `JMenuBar`, como en Windows/Motif), en
`Commands.java` (`note.mixTableChange` usa `withAccelerator("F10")`). El arreglo no es tan
directo como los anteriores: o se cambia el acelerador (rompería la paridad con el manual de
Guitar Pro 5) o se instala un `KeyEventDispatcher`/`KeyEventPostProcessor` propio con más
prioridad que el de la L&F. **Tamaño:** chico-mediano.

### 5. Tab no alterna tablatura/pentagrama con la partitura enfocada — MIENTE · chico
**Manual:** línea 780 (citada en `Editor.toggleNotation()`) — Tab alterna la edición entre
tablatura y pentagrama sin mover el cursor.
**Camino:** tecla cruda (no tiene comando en el catálogo; la resuelve `KeyboardEditing`
directamente sobre `ScoreCanvas`).
**Esperado:** `cursor().notation()` cambia de `TABLATURE` a `STANDARD` (o viceversa).
**Observado:** no cambia. El binding existe (`KeyboardEditing.install` deja
`"pressed TAB"` en el `WHEN_FOCUSED` de `ScoreCanvas`, comprobado), pero nunca se ejecuta.
**Evidencia:** `WriteAScoreAuditTest.tabCrudoNoCambiaDeNotacionPorQuedarseConElFocoAntes`.
**Sospecha:** `ScoreCanvas` (constructor,
`tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/score/ScoreCanvas.java`) nunca llama a
`setFocusTraversalKeysEnabled(false)`: Tab es, de fábrica, una tecla de navegación de foco para
cualquier `JComponent`, y esa maquinaria de AWT se queda con la tecla antes de que
`KeyboardEditing` (`tabpro-ui/.../tab/KeyboardEditing.java:64-77`, método `install`) la vea.
**Tamaño:** chico.

### 6. El diálogo de volumen del metrónomo no tiene forma de abrirse — AUSENTE · chico
**Manual:** Configure the Sound (línea 1945) — configuración del metrónomo (activo/volumen).
**Camino:** ninguno. Verificado por inspección de código, no con un test dinámico (es una
ausencia, no una mentira: no hay control que apretar).
**Esperado:** algún menú, botón o atajo que abra `MetronomeDialog`.
**Observado:** `Ports.Dialogs.metronomeSettings()` está declarado, **implementado** en
`MainFrame.Windows.metronomeSettings()` (arma un `MetronomeSettings` real desde el `Transport` y
llama a `MetronomeDialog.ask`), pero **ningún** `define(...)` de
`tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/actions/Commands.java` ni ningún ítem de
`MenuBar.java` lo invoca. Se confirmó con `grep -rn "metronomeSettings"` sobre todo el árbol:
las únicas dos apariciones son la interfaz y la implementación.
**Sospecha:** falta un `define("sound.metronomeSettings", …, dialogs::metronomeSettings)` en
`Commands.java` (cerca de `sound.metronome`, línea ~364) y su entrada en `MenuBar.java`.
**Tamaño:** chico.

---

## NO VERIFICABLE

### Print a Score (línea 2207)
No se llegó a cubrir por límite de tiempo dentro del orden de prioridad pedido (es el
anteúltimo capítulo). Nota a favor: `ScorePrintingTest` (con `@Tag("integracion")` ya existente,
`tabpro-ui/src/test/.../print/ScorePrintingTest.java`) compara el render en memoria contra un
BMP exportado a disco, lo que ya cubre el **renderizado**; lo que falta específicamente es el
camino de usuario (Archivo > Imprimir, Ctrl+P) sobre el `PrinterJob`/diálogo de impresión real de
Swing, que además depende de que la máquina tenga algún servicio de impresión instalado
(`PrinterJob.getPrinterJob().getPrintService()`), algo que no se verificó que exista en este
entorno.

### Import / Export a Score (líneas 2293 / 2506)
Tampoco se llegó a cubrir por límite de tiempo (último capítulo del orden pedido). Estos
comandos (`document::importXxx` / `document::exportXxx`) abren un `JFileChooser` real antes de
llegar al lector/escritor — un camino de usuario genuino que esta auditoría no ejerció — pero
cada formato (`.tabpro`/JSON, MIDI, ASCII, MusicXML, PowerTab, TablEdit, Guitar Pro) ya tiene
suites unitarias extensas contra el propio lector/escritor (`tabpro-format/src/test/...`), y
`CombinedExchangeTest`/`ClippingJsonTest` (en `tabpro-app`) cubren la orquestación. Ejercitar el
`JFileChooser` real de forma segura (sin tocar archivos del usuario ni depender de un gestor de
ventanas para sus diálogos nativos) hubiera necesitado más tiempo del que quedaba.

---

## El harness

`tabpro-app/src/test/java/com/gstncaruso/tabpro/app/audit/`:

- `AuditSupport.java`: fábrica de `MainFrame` real (con `ScoreFiles`/`Player`/`Devices` falsos
  inyectables), recorrido del árbol de componentes real (`findComponent`, `findComponents`,
  `findMenuItem`, `findButton`, `findCheckBox`, `findRadioButton`, `tabContent`), despacho de
  `KeyEvent` real (`pressKey`, `typeChar`) y el manejo de diálogos modales reales sin `Robot`
  (`withDialog`, `dispatchKeyAndDetectDialog`): un `AWTEventListener` global agarra el
  `WINDOW_OPENED` del `JDialog` -que Swing entrega dentro del mismo bucle anidado que bloquea a
  `setVisible(true)`- y ahí mismo se tocan sus controles reales.
- Un archivo de test por capítulo, todos `@Tag("integracion")` y
  `@ResourceLock(AuditSupport.SWING_LOCK)`.
- Dos cambios de infraestructura en el `pom.xml` raíz (con el mismo valor por defecto que antes,
  así que `mvn -B verify` no cambia):
  - `tests.headless` (default `true`) parametriza `-Djava.awt.headless` del `argLine` de
    surefire: los aceleradores de menú se resuelven con `WHEN_IN_FOCUSED_WINDOW`, y Swing sólo
    los registra en su `KeyboardManager` cuando la ventana está *showing* de verdad —
    `pack()` sin `setVisible(true)` no alcanza.
  - `java.util.prefs.userRoot` y `java.io.tmpdir` aislados en el mismo `argLine`: `MainFrame`
    real lee/escribe las `Preferences` reales del usuario y busca su archivo de recuperación en
    el `tmpdir` real; sin aislarlos, un archivo de recuperación real dispara un diálogo real en
    cada test (se reprodujo una vez).
  - `@ResourceLock` con una clave compartida en las 13 clases: Swing tiene un solo EDT por
    máquina virtual y la suite corre las clases en paralelo — sin el lock, dos clases que abren
    diálogos modales al mismo tiempo se pisan el `AWTEventListener` global (`Toolkit` no
    distingue de qué test es cada ventana) y la suite queda colgada (se reprodujo una vez, sin
    el lock).

Correr sólo el harness: `mvn -B -pl tabpro-tests -am test -Dtests.headless=false
-Dtests.excluded.groups=ninguno -Dgroups=integracion`.
