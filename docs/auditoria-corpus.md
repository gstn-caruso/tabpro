# Auditoría de corpus — tabpro contra archivos reales

Las auditorías anteriores (`auditoria-manual.md`, `auditoria-uso-real.md`) verificaron el código
contra el manual y contra el camino real del usuario, pero con muy pocos archivos ajenos de
verdad: `tabpro-format` trae 10 fixtures propios de Guitar Pro, 17 de PowerTab y 5 de MusicXML.
Desde la última vez que se probó contra un corpus externo (PyGuitarPro, 60/61 archivos), cambió
mucho el pintado de la partitura (tipografía Bravura, glifos, tempo, marcadores, selección),
las barras, la mesa de mezcla y la vista global. Esta auditoría vuelve a bajar el corpus de tests
de **PyGuitarPro** (LGPL, por eso nunca se commitea: vive en el scratchpad de la sesión) y lo
suma a los fixtures propios, para encontrar qué se rompe con archivos reales: excepciones,
cuelgues, páginas que no renderizan, exportaciones que no reabren.

Código auditado: rama `main`, versión 0.49.1. Corpus: 49 Guitar Pro (39 de PyGuitarPro + 10
propios, `.gp3`/`.gp4`/`.gp5`), 17 PowerTab (propios) y 5 MusicXML (propios) — 71 archivos.

## Método

1. **Paso automático, sin ventana** (`CorpusAuditTest`, descartable, borrado antes de este commit):
   por cada archivo, en un `assertTimeoutPreemptively` de 60 s —
   - lo abre por el `ScoreExchange` real (`CombinedExchange(NotationExchange, SoundExchange)`,
     el mismo que usa `Archivo > Importar`): `importGuitarPro`/`importPowerTab`/`importMusicXml`
     según la carpeta de origen;
   - renderiza todas sus páginas en modo Página (`ScoreSheets.renderPages`) y una vez en modo
     Pergamino (`ScoreSheets.render(..., ViewMode.PARCHMENT, ...)`), fuera de pantalla;
   - lo exporta a `.gp4`, MIDI y MusicXML a un directorio temporal y reabre cada export con el
     lector correspondiente;
   - lo guarda como `.tabpro` (`JsonScoreFiles`) y lo recarga, comparando el `Score` con `equals`.
   - Corrida: `mvn -B -pl tabpro-tests -am test -Dtests.excluded.groups=ninguno
     -Dgroups=integracion -Dtest=CorpusAuditTest` — **71/71 casos ejecutados en 2,1 s**
     (sin contar el arranque de Maven/JVM).

2. **Paso con ventana real** (`GuiSmokeAuditTest`, descartable, borrado antes de este commit):
   `Theme.install()` + `AuditSupport.newFrame` con un `RecordingPlayer` (no frena solo) y un
   `EventQueue` propio empujado sobre la cola real de AWT (para atrapar excepciones que escapan
   del pintado, no solo las que dispara el propio test), sobre cinco archivos variados
   (multipista + muchos compases, percusión + segunda voz + repetición con finales alternativos,
   dos voces, repeticiones, direcciones): recorre las cuatro vistas del menú Ver, mueve el cursor
   al último compás y reproduce dos segundos. **Sólo se pudo correr 1 de los 5 casos** — ver
   Hallazgo 2.

## Tabla — paso automático (71/71 archivos)

Todas las columnas booleanas son sobre el mismo archivo ya abierto; `-1`/`false` en un archivo
que no abrió significa que los pasos siguientes ni se intentaron. `tabproIgual` compara el
`Score` recargado con `equals()`. `ms` es el tiempo del pipeline completo por archivo (paso 1).

| archivo | formato | abre | pistas | compases | renderiza | gp4 | midi | musicxml | tabpro | tabpro= | ms |
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

Resumen: **49/49 Guitar Pro** y **5/5 MusicXML** pasan los cinco pasos enteros. **14/17 PowerTab**
pasan; los otros 3 son el Hallazgo 1. Ninguno de los 68 archivos que abrieron encontró un problema
al renderizar, exportar/reabrir GP4, MIDI o MusicXML, ni en la ida y vuelta a `.tabpro`.

## Hallazgos

### Hallazgo 1 — Tres `.ptb` no abren (limitación ya conocida, no es un bug nuevo)

`guitar_ins.ptb`, `merge_multibar_rests.ptb` y `positions.ptb` (fixtures propios de
`tabpro-format/src/test/resources/powertab/`) tiran `ScoreFileException` al abrir:

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

No es un hallazgo nuevo: `PowerTabFileTest` (`tabpro-format`) ya tiene
`aMultibarRestIsReportedInsteadOfGuessed` y `aGuitarReassignmentIsReportedInsteadOfGuessed`
que verifican exactamente esto — el lector reporta en vez de adivinar, a propósito. Se
lista igual porque es lo que le pasa a un usuario real que intenta abrir estos tres archivos:
sale un cartel de error, no un cuelgue ni una lectura corrupta. No requiere fixture nuevo.

### Hallazgo 2 — `IllegalArgumentException` al mover el cursor tras cambiar de vista, y la corrida completa se cuelga

**Archivo:** `Directions.gp5` (PyGuitarPro; 1 pista, 19 compases, con símbolos Coda/Segno).
**Paso:** `GuiSmokeAuditTest` — con la ventana real visible, tras recorrer Pergamino → Pantalla
vertical → Pantalla horizontal → **Modo página** (menú Ver), `editor.moveToLastMeasure()` para
ir al último compás dispara, **de forma sincrónica sobre el mismo hilo que la llamó** (no el
EDT): `Editor.moveCursor` → `Editor.notifyListeners` → `ScoreCanvas.editorChanged` (línea 406)
→ `JViewport.scrollRectToVisible` → intenta crear un buffer de doble buffering de **0×0 píxeles**
y explota:

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

**Sospecha:** `com.gstncaruso.tabpro.ui.score.ScoreCanvas.java:406` (el `scrollRectToVisible` no
se defiende de un viewport con tamaño 0 — plausible justo después de un cambio de modo de vista,
mientras el layout todavía no asentó el tamaño real). Un caso menor: en el harness,
`moveToLastMeasure()` se llamó directo desde el hilo del test en vez de por una tecla real
despachada (que sí pasaría por el EDT) — el hallazgo puede estar amplificado por eso, pero el
`scrollRectToVisible` sin guarda contra tamaño 0 es real y vale la pena blindarlo igual.

**Agravante, mismo mecanismo documentado en `docs/auditoria-uso-real.md` (nota sobre
`withDialog`/EDT):** después de esta excepción, el fork entero de Maven quedó colgado (no
terminó solo, hubo que matarlo con `kill` externo a los ~20 minutos: "*The forked VM terminated
without properly saying goodbye*", exit 143). Una segunda corrida, en una JVM nueva y **sin
incluir el caso que ya había fallado** (sólo los otros 4 archivos: `Demo v5.gp5`,
`tabpro-features.gp5`, `Voices.gp5`, `Repeat.gp5`), **también se colgó** y se mató con
`timeout 300` + `kill` externo. Como ninguna corrida escribió el reporte de Surefire para esta
clase (`tabpro-tests/target/surefire-reports/` sólo tiene el de `CorpusAuditTest`), no se sabe
si el cuelgue fue el mismo archivo/paso u otro: el `assertTimeoutPreemptively` no sirve acá —
si el bloqueo es una espera real de AWT/Swing (o más abajo, de Xlib) y no un simple hilo lento,
ninguna clase de `Timeout` de JUnit lo puede cortar desde adentro de la misma JVM.
**Sólo se pudo auditar 1 de los 5 archivos elegidos para la ventana real**; los otros cuatro
quedan pendientes de una corrida con timeout externo por proceso (`timeout` de shell) alrededor
de cada archivo por separado, no de la clase entera.

**Tamaño estimado del fix:** acotado — un guard en `ScoreCanvas.editorChanged` (o en el punto
donde arma el rectángulo para `scrollRectToVisible`) que no pida scroll si el viewport todavía
mide 0×0. El cuelgue en sí (agravante) es más caro de diagnosticar: hace falta reproducirlo con
un thread dump (`jstack`) en el momento exacto, no con `assertTimeoutPreemptively`.
