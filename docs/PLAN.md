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
| 1b. Auditoría de las tablas de atajos que quedaron fuera del recorte | ✅ hecho — 37 de 41 atajos correctos |
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

## Los huecos de esta sesión, en orden

| Hueco | Estado |
|---|---|
| Exportar imagen en BMP (+ restricción a modo Página) | ✅ #45 |
| La exportación de imagen falla ruidosamente si `ImageIO` no escribe | ✅ #49 |
| El lector de GP descartaba las direcciones musicales | ✅ #50 |
| Exportar a WAVE | ✅ #51 |
| `MidiScoreExporter` escribía un solo tempo | ✅ #52 |
| El lector declara el orden de los casilleros de direcciones | ✅ #54 |
| Diálogos de importación (escuchar pistas, precisión, espaciado ASCII) | ✅ #57 |
| El exportador de sonido se muda a `tabpro-midi` (`format` ya no depende de `midi`) | ✅ #58 |
| Importar TablEdit | ✅ #62 |
| El `.mid` exportado suena como la partitura | 🔜 rebasándose |
| Banco de sonidos SoundFont (+ F2) | 🔜 rebasándose |
| Los seis bugs del formato Guitar Pro | 🔜 en curso |
| Exportar al formato Guitar Pro | ⏸ retenido hasta que la verificación dé verde |
| Importar PowerTab | 🔜 en curso |
| Import MIDI: casilla "2 canales por pista" | ⏳ pendiente |
| `File > Open` que abra también los `.gp*` | ⏳ pendiente |

## Lo más importante que se aprendió

**El oráculo estaba adentro del sistema que queríamos verificar.** Aparece cuatro
veces el mismo día, en cuatro disfraces:

1. **El lector de Guitar Pro lee mal los archivos reales.** El escritor nuevo de
   `.gp4` pasaba todos sus tests de ida y vuelta contra nuestro propio lector.
   Verificado contra **PyGuitarPro** —otra implementación— y contra `.gp4`
   auténticos: **siete de siete archivos generados con uso normal no abren**, y
   aparecieron seis bugs. Pasaba porque el lector y el escritor comparten las
   mismas suposiciones equivocadas: se entienden entre ellos y con nadie más.
   Peor: los fixtures del repo los generamos nosotros usando ese mismo lector
   como referencia, así que tampoco eran fuente independiente.
   Uno de los seis está confirmado como bug viejo del lector:
   `UNITS_PER_QUARTER_TONE = 50` cuando el formato usa 25, o sea que **todos los
   bends de todos los archivos de Guitar Pro que tabpro abrió alguna vez entraron
   a la mitad de profundidad**. Y como el `.tabpro` no guarda de dónde vino la
   partitura, **no hay migración segura**: duplicar todo arreglaría las
   importadas y rompería las escritas a mano. Va nota en el release.
2. **El dry-run de semantic-release** verificaba que la configuración cargara, no
   que las notas se pudieran generar: sin token se frena antes de ese paso.
3. **El CI sin placa de sonido es el usuario real.** Un puerto MIDI que no abría
   se llevaba puesta la reproducción entera, y no se veía en la máquina de
   desarrollo porque ahí había línea libre.
4. **Probar el banco de sonidos en una máquina que lo tiene instalado** es el
   mismo error, y por eso el camino sin banco se prueba explícitamente.

**Regla que queda para el proyecto:** un round-trip contra nuestro propio lector
prueba consistencia interna, no compatibilidad. La única verificación que
significa algo es contra un archivo auténtico o contra otra implementación.

**Lección de diseño que dejó el caso de los bends:** la procedencia de un dato es
barata de guardar y cara de no tener. Si el `.tabpro` recordara de qué archivo
salió, la migración sería posible.
