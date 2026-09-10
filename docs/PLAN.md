# Plan — el clon completo del manual de Guitar Pro 5

Documento vivo. Se actualiza al cerrar cada etapa para poder retomar sin contexto previo.

## Objetivo

tabpro tiene que hacer **todo** lo que describe `assets/manual-guitar-pro-5.pdf`.
Es un clon libre: donde Guitar Pro usa algo propietario, tabpro pone la
alternativa libre más cercana.

Decisiones tomadas con el usuario (2026-09-06):

- **Alcance:** todo lo que falte contra el manual, no sólo los cuatro huecos que
  declaraba el README de v0.8.0.
- **Sonido:** el RSE se reemplaza por un **SoundFont libre cargado en Gervill**,
  el sintetizador del JDK. Mejora toda la reproducción, no sólo el export a WAVE.
  No se commitea ningún `.sf2` al repo (pesan más de 100 MB): el programa
  descubre los del sistema o el usuario elige uno, la elección se persiste, y si
  no hay ninguno se degrada al banco interno del JDK. El `.deb` recomienda
  `fluid-soundfont-gm`.
- **Versionado:** deja de ser manual. Lo maneja **semantic-release** en el push a
  `main`, según el tipo del commit de squash: `feat:` → minor, `fix:`/`perf:` →
  patch, breaking → major, `docs:`/`chore:`/`ci:`/`test:` → sin bump. De ahí sale
  el tag y el release del `.deb`, automáticamente. **Nadie toca la versión en los
  poms ni en el README a mano.**
- **Higiene:** el árbol arranca limpio; cada cambio entra por su branch + PR con
  CI verde.

## Cómo se trabaja

- El agente principal orquesta y no implementa: reparte a subagentes con worktree
  propio (`Agent(isolation: "worktree")`), y abre él los PRs.
- Modelos: mecánico/read-only → `haiku`; loops TDD/refactor → `sonnet`;
  diseño difícil → `opus`. Nunca `fable` fuera de planning.
- TDD obligatorio: test que falla → mínimo código → refactor. Un PR por hueco.
- Se sube en cada verde (los agentes se caen por rate limit; el trabajo no se pierde).

Para regenerar el texto del manual:

```sh
pdftotext -layout assets/manual-guitar-pro-5.pdf /tmp/manual.txt
```

Secciones (líneas de ese `.txt`): Understanding Notation 385, Main Screen 458,
Write a Score 481, Add Symbols 961, Insert Parameter Changes 1340, Add Lyrics 1377,
Add Markers 1448, Cut/Copy/Paste 1478, Wizards 1587, Percussion 1670,
Work with a Score 1723, Configure the Display 1886, Configure the Sound 1945,
Play the Score 2087, Print a Score 2207, Import a Score 2293, Export a Score 2506,
Tools for the Guitarist 2665, Keyboard Shortcuts 3154 **hasta el final del archivo**
(recortarlo antes deja afuera las tablas Effects, Navigation, Sound y Misc.).

## Dos sesiones trabajando a la vez

Hay **otra sesión de Claude** sobre el mismo repo (`tabpro-mvp-editor-playback`),
que cerró los PRs #30 a #42. El reparto acordado:

| Sesión | Se ocupa de |
|---|---|
| Esta (`docs/`, formatos, sonido) | Todo el capítulo Importar/Exportar, el motor de sonido con SoundFont, y los huecos del lector de Guitar Pro |
| La otra | Notación, efectos, interfaz, reproducción, herramientas del guitarrista y atajos |

Reglas de convivencia: cada una trabaja en worktrees aislados, nunca en el
checkout compartido; `docs/` lo maneja esta sesión; y quien vaya a tocar
`MidiSetupDialog.java` avisa antes, porque las dos mitades caen ahí.

## Estado

| Etapa | Estado |
|---|---|
| 0. Limpieza del árbol (PR #41 y #42 mergeados, worktrees y branches podadas) | ✅ hecho |
| 1. Auditoría del manual contra el código | ✅ hecho — ver [auditoria-manual.md](auditoria-manual.md) |
| 1b. Auditoría de las tablas de atajos que quedaron fuera del recorte | ✅ re-auditado 2026-09-06 — ver abajo |
| 2. Versionado automático con semantic-release | ✅ hecho — publicó la v0.9.0 sola |
| 3. Implementación de los huecos, un PR por hueco | 🔜 en curso |
| 4. Verificación independiente de los formatos binarios | 🔜 en curso — ver abajo |

Punto de partida: `main` = `4702129`, versión 0.8.0. Al cerrar la etapa 2, `main`
quedó en **v0.9.0**, publicada automáticamente por el pipeline.

### Lo que enseñó poner en marcha el pipeline

Dos cosas que conviene no volver a aprender:

1. **`v0.8.0` no existía.** El último tag del repo era `v0.6.1`, aunque los poms
   dijeran 0.8.0 y el README mandara a bajar un `.deb` de esa versión. Antes de
   activar semantic-release hubo que taggear `v0.8.0`, o habría calculado una
   versión **anterior** a la que el programa ya decía tener.
2. **Un dry-run sin credenciales no prueba lo que parece.** semantic-release se
   frena en `verifyConditions` del plugin de GitHub, que corre *antes* de
   `generateNotes`, así que el dry-run verificaba que la configuración cargara,
   no que las notas se pudieran generar. El primer release real falló por un
   preset incompatible que el dry-run nunca llegó a ejercitar. Para probar de
   verdad hay que correrlo sacando los plugins que piden credenciales.

### Re-auditoría de atajos (2026-09-06)

El "37 de 41" era de antes de una docena de PRs y nadie lo había vuelto a
medir. Se rehizo entero contra `Commands.java`, cotejando las 76 filas del
capítulo Reference del manual (páginas 79 a 81) una por una — no si "algún"
comando tenía la tecla, si el comando **correcto** la tenía. Test que sostiene
esto: `ManualKeyboardShortcutsTest` (72 filas comparables 1:1 contra el
catálogo).

- **72 de 72 coinciden.** Ni una sola diferencia contra el manual hoy.
- **4 no pasan por el catálogo pero están:** Home/End (primer/último beat del
  compás) y el `*` del puntillo los resuelve `KeyboardEditing` como tecla
  cruda del lienzo (con su propio test); Page Up/Page Down los resuelve Swing
  solo, scrolleando el `JScrollPane` de la partitura.
- **Atajos que tiene tabpro y el manual no lista:** zoom (`Ctrl +`/`Ctrl -`/
  `Ctrl 0`), diapasón (`Ctrl 3`) y teclado (`Ctrl 4`). No pisan ningún atajo
  del manual, se dejan.
- **Feature faltante, no atajo:** `Enter` como "agregar nota en notación
  estándar" no existe — ya estaba anotado en
  [auditoria-manual.md](auditoria-manual.md) ("Enter no agrega una nota en
  notación estándar — AUSENTE · grande"). tabpro solo escribe por dígitos de
  traste.
- **Bug real encontrado y arreglado — no en el catálogo, en la plomería:**
  `JScrollPane` y `JSplitPane` traen atajos de fábrica (scroll, navegar el
  split) que Swing revisa *antes* que el acelerador de un menú. Con el foco en
  la partitura -la situación normal al editar- se comían `Ctrl+Home`
  (nav.firstBar), `Ctrl+Fin` (nav.lastBar), `F6` (track.properties), `F8`
  (file.pageSetup) y `Ctrl+Tab` (marker.next): el catálogo declaraba la tecla
  correcta y colgaba de su menú, pero apretarla no hacía nada. Arreglado por
  `AcceleratorGuard` (`AcceleratorGuardTest`); ver el PR de la branch
  `fix/los-atajos-que-el-manual-manda`.
- **Sin colisiones.** `CommandsTest.noTwoCommandsShareTheSameShortcut` ya
  cubría esto y sigue en verde; se sumó
  `MenuBarTest.todoComandoConAceleradorCuelgaDeAlgunMenu` para que un atajo
  declarado y nunca colgado de un menú (la otra forma de quedar muerto) tampoco
  pase desapercibido.

## Los huecos de esta sesión

Todos cerrados salvo el último.

| Hueco | Estado |
|---|---|
| Exportar imagen en BMP (+ restricción a modo Página) | ✅ #45 |
| La exportación de imagen falla ruidosamente si `ImageIO` no escribe | ✅ #49 |
| El lector de GP descartaba las direcciones musicales | ✅ #50 |
| Exportar a WAVE | ✅ #51 |
| `MidiScoreExporter` escribía un solo tempo | ✅ #52 |
| El lector declara el orden de los casilleros de direcciones | ✅ #54 |
| Diálogos de importación (escuchar pistas, precisión, espaciado ASCII) | ✅ #57 |
| El exportador de sonido se muda a `tabpro-midi` | ✅ #58 |
| Importar TablEdit | ✅ #62 |
| La importación de TablEdit vuelve a llegar al importador | ✅ #64 |
| **Cuatro bugs del lector de Guitar Pro** | ✅ #69 |
| El puerto de intercambio sin `default` que tiran | ✅ #70 |
| El `.mid` exportado suena como la partitura | ✅ #60 |
| Exportar al formato de Guitar Pro | ✅ #74 |
| El cursor de edición como línea roja | ✅ #76 |
| Importar PowerTab | ✅ #77 |
| Banco de sonidos SoundFont (+ F2) | ✅ #71 |
| Import MIDI: casilla "2 canales por pista" | ✅ hecho, esperando el arreglo de `effectChannelNextTo` |
| El lector de GP descarta el byte de wah | 🔜 en curso |

## Lo más importante que se aprendió

**El oráculo estaba adentro del sistema que queríamos verificar.** El mismo error
apareció cinco veces en un día, en cinco disfraces:

1. **El lector de Guitar Pro leía mal los archivos reales.** El escritor nuevo de
   `.gp4` pasaba todos sus round-trips contra nuestro propio lector. Contra
   **PyGuitarPro** y `.gp4` auténticos: **siete de siete archivos generados no
   abrían**, y de seis bugs, **cuatro eran del lector que ya estaba en
   producción**. Tres de dieciséis archivos auténticos ni siquiera abrían en
   tabpro. Pasaba porque lector y escritor comparten las mismas suposiciones — y
   los fixtures del repo los generábamos nosotros con ese mismo lector.
2. **El dry-run de semantic-release** verificaba que la configuración cargara, no
   que las notas se pudieran generar: sin token se frena antes de ese paso.
3. **El CI sin placa de sonido es el usuario real.** Un puerto MIDI que no abría
   se llevaba puesta la reproducción entera. Y exportar a WAVE reventaba con una
   excepción sin manejar en cualquier máquina sin audio — el render pedía una
   línea que un render *offline* no necesita.
4. **Probar el banco de sonidos en una máquina que lo tiene instalado.** Saltear
   un camino con `Assumptions` no es probarlo.
5. **Los tests probaban la pieza, no el camino del usuario.** `importTabEdit`
   estaba implementado, testeado y era **inalcanzable**: nadie había escrito la
   línea que lo delega, y el usuario elegía su archivo para recibir "no
   disponible".

### Las reglas que quedan

- **Un round-trip contra nuestro propio lector prueba consistencia interna, no
  compatibilidad.** La única verificación que significa algo para un formato
  binario es contra un archivo auténtico o contra otra implementación.
- **La diferencia de entorno no dice de qué lado está el error.** Dice que hay
  una suposición sobre la máquina metida en algún lado. La pregunta que los
  separa: *¿qué querría que pasara en la máquina del usuario?*
- **Un `default` que tira convierte un error de compilación en uno de runtime**, y
  hace que "no lo soporto" y "me lo olvidé" se vean iguales en el código.
- **Cuando dos features distintas se tuercen en el mismo punto, el punto está mal
  puesto.**
- **Un test que no puede fallar es basura**; uno que tapa un agujero que el
  diseño podría cerrar es una curita.

### Cuando el bug ya salió del programa

Dos bugs llegaron a los archivos de la gente, y **piden cosas distintas**. La
pregunta no es "¿el bug llegó a los archivos?" sino **"¿el dato correcto todavía
es derivable de lo que quedó guardado?"**

- **Los bends destruyeron el dato.** Un bend leído a la mitad es indistinguible
  de uno legítimo de esa profundidad. No hay migración posible: va nota en el
  release pidiendo reimportar el original.
- **Los canales no perdieron nada.** El canal real era una función determinística
  del orden de las pistas, así que el programa lo recalcula al abrir y el usuario
  no se entera. No va en la nota del release.

---

## Etapa: se ve y se usa como Guitar Pro 5 (arrancó el 2026-09-10)

Objetivo declarado por Gastón: **un programa lo más parecido posible a Guitar
Pro 5.2** (abandonware), con el manual como guía de producto. Tres frentes: lo
que el manual describe y todavía no funciona *al usarlo*, el aspecto visual
(barras e íconos) y la accesibilidad, que hoy es cero.

### Decisiones (2026-09-10)

- **Estética:** disposición, tamaños y semántica de barras e íconos se miden de
  las capturas del manual (`pdfimages`, ver la nota de tipografía); se mantiene
  FlatLaf Darcula. No se copia el tema claro de Windows.
- **Íconos:** las acciones genéricas (archivo, edición, zoom, transporte, vista)
  salen de **Tabler Icons** (MIT, SVG) dibujadas con `jsvg` 2.1.0 directo
  (la librería que FlatLaf usa por debajo; `flatlaf-extras` se descartó porque
  arrastra FlatLaf a `tabpro-ui`, y FlatLaf vive sólo en `tabpro-app`). Los
  símbolos musicales salen de **Bravura**, que ya está en el repo. Los efectos
  sin glifo SMuFL (P.M., let ring, tapping…) van como texto abreviado, como en
  GP5. Se commitean sólo los SVG que se usan, con su licencia al lado.
- **Accesibilidad, los cuatro frentes:** teclado completo (mnemónicos, orden de
  tabulación, foco visible, ningún control sólo alcanzable con el mouse); lector
  de pantalla (nombre y descripción accesible en cada control); contraste y
  tamaño (WCAG AA sobre el tema oscuro, tooltip en todo ícono, escala de la UI);
  y una sección **Accesibilidad** en Preferencias (tamaño de fuente, alto
  contraste, sin animaciones). Cada preferencia nace con su lector y su test:
  la regla de la etapa anterior sigue vigente.
- **Cómo se encuentra lo que falla:** una auditoría de **uso real** — cada
  acción del manual ejercitada por el camino del usuario (menú, atajo, botón,
  diálogo) verificando el efecto observable — y no otra lectura estática del
  código. Informe en `docs/auditoria-uso-real.md`, harness bajo el tag
  `integracion`.
- **Flujo:** autónomo (DIY) y en loop. El principal planifica, briefea, abre el
  PR, espera el CI y mergea. El agente `worker` (`~/.claude/agents/worker.md`:
  sonnet, worktree propio, TDD + TCR, push en cada verde, sin PR) escribe el
  código. Una feature branch por cambio, un PR por tipo de cambio.

### Frentes y orden

| # | Frente | Cortes en PR |
|---|---|---|
| A | Auditoría de uso real | el informe y el harness; después un `fix/` por hallazgo, los MIENTE primero |
| B | Íconos | B1 puerto `IconSet` + `flatlaf-extras` + primeros SVG · B2 barras genéricas a Tabler · B3 figuras, claves y efectos a Bravura · B4 barras agrupadas y ordenadas como la "Main Screen" del manual · B5 color por tema, estado deshabilitado y HiDPI |
| C | Accesibilidad | C1 test que recorre el árbol de componentes y exige tooltip y nombre accesible en cada control sin texto · C2 mnemónicos en menús y diálogos con test de no-colisión · C3 teclado en los componentes custom (perillas, diapasón, piano, grilla) y foco visible · C4 test de contraste WCAG AA sobre la paleta · C5 sección Accesibilidad en Preferencias |
| D | Lo visual que salga de la auditoría | barra de estado, mesa de mezcla, vista global: medidos contra las capturas |

Los inventarios que alimentan B y C (íconos actuales contra barras de GP5;
componentes sin tooltip, nombre accesible ni teclado; paleta) se generan con
agentes de un solo pase y no se commitean: lo que vale de ellos entra en el
PR que lo usa.

### Cómo retomar sin contexto

El loop es: elegir el siguiente corte de esta tabla (o el próximo hallazgo de la
última auditoría en `docs/`) → briefear un `worker` (`~/.claude/agents/worker.md`)
con objetivo, branch, decisiones cerradas, criterio de terminado y trailer →
mirar el PNG que el worker deja en el scratchpad → abrir el PR con qué y por qué
→ `gh pr checks` verde → `gh pr merge --squash` → borrar la branch remota sólo si
el PR figura MERGED → actualizar esta tabla. Cuando una auditoría se agota, se
busca un oráculo nuevo del lado de afuera (el manual, sus capturas, archivos
reales); las tres primeras fueron uso real, visual de la ventana y de la
partitura; la cuarta, el corpus; la quinta, en curso, los diálogos.

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
| 2 · core, format, app (+ `integracion` → `integration` tag), ui/dialogs, ui/score group, rest of ui | `test/<scope>-english-names` | — | in progress |
| 3 · internal messages | — | — | pending |
| 4 · i18n slices 4.1–4.9 (see design above) | — | — | pending |
| 5 · docs in English | — | — | pending |
