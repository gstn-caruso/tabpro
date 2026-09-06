# Auditoría — el manual de Guitar Pro 5 contra tabpro 0.8.0

Cinco agentes leyeron el manual (`assets/manual-guitar-pro-5.pdf`) bloque por
bloque y verificaron cada ítem contra el código, con `archivo:línea` como prueba.
Este documento lista **sólo los huecos**. El plan de trabajo vive en [PLAN.md](PLAN.md).

Estado del código auditado: `main` = `4702129`, versión 0.8.0.

---

## Importar y exportar

Cobertura declarada por el auditor: 8 de 17 ítems presentes.

### Exportación al formato Guitar Pro — AUSENTE · grande
`File > Export > Guitar Pro 4 Format`. No existe `exportGuitarPro` en ningún
módulo; `ScoreExchange.java:83` sólo declara `importGuitarPro(Path)`, y el menú
Exportar (`MenuBar.java:45`) sólo trae MIDI/ASCII/MusicXML/Imagen/PDF.

El lector ya existente (`tabpro-format/.../guitarpro/`, 2287 líneas) es el espejo
a seguir: hace falta `GuitarProByteWriter` (little-endian: int, short, boolean,
color, armadura, strings de largo fijo y prefijadas) y los escritores por sección
(header, canales, atributos de compás, pistas, beats, notas, acordes, bends),
orquestados por un `GuitarProFile.write(Score, Path)` con el mismo orden que
`read()`. Como el destino es sólo GP4, no hace falta la abstracción
`GuitarProVersion`: alcanza el layout fijo de GP4 (`generation=4, minor=6`).

### Importación de PowerTab — AUSENTE · grande
`File > Import > PowerTab`. Cero apariciones de "PowerTab" en el repo. Formato
binario `.ptb`, documentado por el proyecto open source `powertabeditor`. Paquete
nuevo `tabpro-format/.../powertab/` con el mismo patrón de capas que `guitarpro/`.

### Importación de TablEdit — AUSENTE · grande
`File > Import > TablEdit`. Cero apariciones en el repo. Formato binario `.tef`
propietario y **sin especificación pública conocida** — es el más incierto de los
cuatro huecos: habría que reversearlo desde archivos de muestra.

### Exportación a WAVE — AUSENTE · grande
`File > Export > Wave`. `AudioSystem` sólo se usa en `MicrophonePitch.java` para
la captura del afinador; no hay `AudioSystem.write(...)` en ningún lado.

La infraestructura ya está: `MidiScoreExporter.toSequence(Score)` convierte la
partitura a `Sequence`, y `MidiPlayer` sabe abrir el sintetizador. Falta el render
fuera de tiempo real contra `AudioSynthesizer` (Gervill, el sinte del JDK) y
volcar el `AudioInputStream` con `AudioSystem.write(..., WAVE, path)`. Clase nueva
en `tabpro-midi` + diálogo en `tabpro-ui/dialogs`.

### Exportación a BMP — PARCIAL · chico
El manual pide BMP y aclara que sólo se habilita en modo Página. `exportImage()`
funciona pero `ScorePrinting.formatOf` (`:73-76`) sólo devuelve `jpg` o `png`, y
el filtro de `MainFrame.java:430` es `"Imagen (*.png, *.jpg)"`. Tampoco se
consulta el `ViewMode` antes de exportar. `ImageIO` ya trae el plugin BMP.

### Import MIDI — casilla "Use 2 channels per track" — AUSENTE · mediano
El manual: dos canales MIDI por pista, útil para bends y slides.
`MidiImportPanel.java:24` sólo tiene el checkbox `transpose`. El hueco real es de
dominio: `Channel` (`tabpro-core/.../model/Channel.java:4-6`) es un record con un
solo campo `number`. Dato revelador: el lector de Guitar Pro **ya parsea** el
segundo canal (`GuitarProTrackReader.java:30`, `effectChannelIndex`) y lo tira,
porque el modelo no tiene dónde guardarlo.

### Import MIDI — escuchar las pistas antes de importar — AUSENTE · chico/mediano
`MidiImportDialog.java:35-120` no tiene ningún botón de reproducción. El patrón a
copiar está en `ScoreBrowser.java:326`, que recibe `transport::preview`.

### Import MIDI — precisión de posición y duración — AUSENTE · mediano
El manual deja definir con qué precisión se eligen posición y duración.
`MidiScoreImporter.java:192` usa siempre `DurationTicks.nearestTo(...)`, sin
parámetro. Modelo a seguir: el combo `rhythmChoice` de `AsciiImportPanel`.

### Import ASCII — espaciado para el ritmo `<variable>` — PARCIAL · chico
El modo `<variable>` existe (`RhythmStrategy.FromSpacing`) pero falta la segunda
lista que fija cuántos intervalos hay entre dos negras.
`AsciiTabImporter.java:204` calcula sólo con el ancho de celda.

---

## Símbolos, cambios de parámetro, letra, marcadores, copiar/pegar, asistentes y percusión

Cobertura declarada por el auditor: 41 de 47 ítems presentes. El bloque más
fiel al manual: la sintaxis de sílabas de la letra, los seis tipos de slide, el
editor de curva del bend y el asistente de percusión están completos, y el
retrieval de los cambios de parámetro al arrancar a mitad de partitura
(`SoundAutomation.java:35-50`) implementa el "Tip" del manual al pie de la letra.

### "Show 'Dynamic' Notes" [F11] — AUSENTE · chico
El manual pide pintar la nota con un degradé según su intensidad. `grep "F11"`
sobre `tabpro-ui` no da nada; el menú Ver (`MenuBar.java:174-177`) no lo tiene.
_(Reclamado por la otra sesión.)_

### El símbolo de la palanca no se dibuja — PARCIAL · chico
Modelo, editor, diálogo y **sonido** están (`TrackRenderer.java:240-241` arma la
curva de pitch bend real), pero `grep "tremoloBar"` sobre todo el paquete
`ui/score/` da **cero** resultados: `paintBend` sólo se invoca para el bend de
nota. _(Reclamado por la otra sesión.)_

### Fade In no se dibuja — PARCIAL · chico
Modelo, editor y sonido están (`MidiSequences.java:162`, `writeFadeIn`); falta la
etiqueta "F" en `TabSymbolPainter.labelsFor(Beat)`.

### La transición de la nota de adorno no se usa — PARCIAL · mediano
`GraceTransition` (SLIDE/BEND/HAMMER/NONE) se edita, se serializa en `.tabpro` y
se lee del formato GP… y **nunca se consume**: ni `TrackRenderer.scheduleGrace`
(`:198-202`) arma la curva, ni `TabNotationPainter.paintGraceNote` (`:104-115`)
dibuja una línea distinta. Es un dato que viaja entero por el sistema sin efecto.

### El vibrato de un punto del bend no suena — PARCIAL · chico
Se edita con clic derecho en tres niveles y se dibuja en el editor
(`BendGridPanel.java:82-83`), pero `PitchTrajectory.of(Bend, ...)` (`:39-44`) sólo
mira `point.semitones()`, nunca `point.vibrato()`.

### Wah-wah: sin símbolo y sin lectura desde archivos GP — PARCIAL · chico
Modelo, editor y menú existen; falta el símbolo en la partitura y falta leer el
byte de wah en `GuitarProBeatReader`, así que un `.gp5` real pierde el dato al
importarse. Que no suene es correcto: el manual dice que sólo afecta con RSE.
_(Reclamado por la otra sesión.)_

### Copiar y pegar entre dos sesiones — AUSENTE · chico o grande
`Editor.java:61` usa un `Clipboard` propio en memoria, no
`Toolkit.getSystemClipboard()`. Conectarlo al portapapeles del sistema es chico;
soportar varias ventanas de verdad es un cambio de arquitectura.

---

## Trabajar con la partitura e imprimir

Cobertura declarada por el auditor: 27 de 50 ítems presentes. Es el bloque con
más huecos. Lo mejor cubierto: el entrenador de velocidad, los cambios de
parámetro, la configuración de página completa con Refresh/Guardar como
predeterminado, los tres modos de detección de canal y la vista global.

### Reposicionar el audio en marcha — PARCIAL · mediano
El manual: hacer clic en la partitura durante la reproducción reanuda desde ahí
sin frenar, y lo mismo con Ctrl+Tab/Shift+Tab entre marcadores. Hoy el cursor de
edición se mueve (`ScoreCanvas.java:58-65`) pero el audio sigue donde estaba:
`Player.java:6-19` sólo expone `play/stop/playNote`, sin seek.

### El motor MIDI ignora el puerto y el canal de cada pista — PARCIAL · mediano
El modelo (`Channel.port`, `Channel.number`) y la mesa de mezcla los muestran,
pero `MidiSequences.java:64-68,111-115` (`channelFor`) asigna canales por orden
secuencial salteando el 9, sin leer nunca lo que dice la pista. `TrackTimeline`
ni siquiera tiene campo `port`.

### Un solo dispositivo de salida en vez de cuatro puertos — PARCIAL · mediano
El manual: cuatro puertos MIDI simultáneos, cada uno con su dispositivo.
`MidiDeviceSetup.java:18-41` tiene un único campo `output`, y `MidiSetupDialog`
un solo combo. _(La otra sesión reclamó MIDI Setup.)_

### Archivo > Buscar en la web — AUSENTE · mediano/grande
Ni el menú ni ningún servicio; cero coincidencias en los cinco módulos.

### El metrónomo no suena solo — PARCIAL · chico
`Player.play(timeline, clicks, listener)` (`:11-13`) siempre necesita un
`Timeline`; el manual dice que el metrónomo se puede usar solo.

### Paso a paso: los botones no cambian de función durante el play — PARCIAL · chico/mediano
El manual: durante la reproducción pasan a ser compás anterior / siguiente.
`Transport.java:132-150` aborta con `if (player.isPlaying()) return;`.

### Sin barra de herramientas de pista — AUSENTE · chico
`ToolBars.java` define `documentRow/structureRow/notationRow`; no hay selector de
pista fuera de la mesa de mezcla.

### Ver > Intercambiar vista (partitura ↔ mesa de mezcla) — AUSENTE · chico
`MainFrame.java:710-718` sólo muestra u oculta la mesa en un `JSplitPane`.

### Preferencias: forzar multipista en pantalla horizontal — AUSENTE · chico
`Preferences.java:10-15` sólo guarda figura por defecto, cuenta regresiva,
auto-scroll y el bajo en el nombre del acorde.

### Los archivos recientes se guardan pero no se muestran — PARCIAL · chico
`Preferences.java:15-56` implementa `MAX_RECENT_FILES=8`, `recentFiles()` y
`remember(Path)`, y `ScoreDocument.java:66,73` los alimenta… pero `recentFiles()`
no tiene un solo consumidor en la UI.

### Abrir está partido en dos comandos — PARCIAL · chico
`file.open` (Ctrl+O) filtra sólo `.tabpro` (`MainFrame.java:288-290`); para un
`.gp5` hay que ir al comando aparte de importar. El manual tiene un solo Abrir.

### "Limit Pitch Variation" — AUSENTE · chico
La casilla que prohíbe variaciones de más de un tono no existe en ningún módulo.

### MIDI Setup: sin botón de prueba de sonido y sin sensibilidad editable — chico
No hay preview por dispositivo, y `MidiCapture.DEFAULT_SENSITIVITY_MILLIS = 60`
(`:20,42`) está fijo en el código. _(La otra sesión reclamó MIDI Setup.)_

### El tempo actual no aparece en el título durante el play — AUSENTE · chico
`ScoreDocument.windowTitle()` (`:45-48`) no lo incluye y `MainFrame` sólo
actualiza el título desde el `Editor`, no desde el `Transport`.

### Imprimir: falta el botón "Configurar" del formato de papel — PARCIAL · chico
Escala y ajustar a la página están; falta delegar en `PrinterJob.pageDialog()`
(sólo se usa `printDialog()`, que es otra cosa).

### Menores
Un único toggle para toda la barra de herramientas en vez de uno por barra;
Archivo > Explorar sin la opción de cuántos compases sonar antes de saltar al
próximo archivo; tempo relativo sin botón para desactivarlo de un clic; clic en
la mesa de mezcla que no lleva al primer beat del compás; y sin `PAGE_UP` /
`PAGE_DOWN` para navegar (`KeyboardEditing.java:32-39`).

---

_(las otras dos secciones se completan a medida que llegan los informes)_
