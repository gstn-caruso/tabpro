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
| 1b. Auditoría de las tablas de atajos que quedaron fuera del recorte | 🔜 en curso |
| 2. Versionado automático con semantic-release | 🔜 en curso |
| 3. Implementación de los huecos, un PR por hueco | 🔜 en curso |

Punto de partida: `main` = `4702129`, versión 0.8.0.

## Los huecos de esta sesión, en orden

| Hueco | Tamaño | Estado |
|---|---|---|
| Exportar imagen en BMP (+ restricción a modo Página) | chico | 🔜 en curso |
| Banco de sonidos SoundFont en Gervill | mediano | 🔜 en curso |
| Exportar a WAVE | grande | 🔜 en curso |
| Exportar al formato de Guitar Pro | grande | ⏳ pendiente |
| Importar PowerTab | grande | ⏳ pendiente |
| Importar TablEdit (sin spec pública: el más incierto) | grande | ⏳ pendiente |
| Import MIDI: escuchar las pistas antes de importar | chico | ⏳ pendiente |
| Import MIDI: precisión de posición y duración | mediano | ⏳ pendiente |
| Import MIDI: casilla "2 canales por pista" | mediano | ⏳ bloqueado por `Ch2` de la otra sesión |
| Import ASCII: intervalos por negra del ritmo `<variable>` | chico | ⏳ pendiente |
| El lector de GP descarta las direcciones musicales (`skipDirections`) | chico | ⏳ pendiente |
| `MidiScoreExporter` escribe un solo tempo, no el mapa | chico | ⏳ pendiente |
| `File > Open` partido en dos comandos: que abra también los `.gp*` | chico | ⏳ pendiente |
