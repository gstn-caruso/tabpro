# Plan — el clon completo del manual de Guitar Pro 5

Documento vivo. Se actualiza al cerrar cada etapa para poder retomar sin contexto previo.

## Objetivo

tabpro tiene que hacer **todo** lo que describe `assets/manual-guitar-pro-5.pdf`.
Es un clon libre: donde Guitar Pro usa algo propietario (el RSE), tabpro pone la
alternativa libre más cercana.

Decisiones tomadas con el usuario (2026-09-06):

- **Alcance:** todo lo que falte contra el manual, no sólo los cuatro huecos que
  declaraba el README.
- **Sonido:** el motor RSE queda afuera. La exportación a WAVE se hace
  renderizando con el sintetizador por defecto de `javax.sound.midi` — sin banco
  SoundFont propio.
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
Tools for the Guitarist 2665, Keyboard Shortcuts 3154.

## Estado

| Etapa | Estado |
|---|---|
| 0. Limpieza del árbol (PR #41 y #42 mergeados, worktrees y branches podadas) | ✅ hecho |
| 1. Auditoría del manual contra el código (5 agentes en paralelo) | 🔜 en curso |
| 2. Implementación de los huecos, un PR por hueco | ⏳ pendiente |
| 3. Release | ⏳ pendiente |

Punto de partida: `main` = `4702129`, versión 0.8.0.

## Huecos conocidos antes de auditar

Los declaraba el README de v0.8.0:

1. Exportación a WAVE — **en alcance** (sinte por defecto).
2. Exportación al formato de Guitar Pro (cap. *Guitar Pro 4 Export*) — en alcance.
3. Importación de PowerTab — en alcance.
4. Importación de TablEdit — en alcance.
5. RSE (motor de sonido realista) — **fuera de alcance**, es propietario.

## Huecos encontrados por la auditoría

_(la etapa 1 completa esta sección)_
