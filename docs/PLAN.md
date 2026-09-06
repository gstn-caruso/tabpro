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
