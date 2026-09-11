# Developing tabpro

How tabpro is put together, how its tests run, and how a change gets from a
branch to a release. For what tabpro does and how to install it, see the
[README](../README.md).

## Stack

- Java 25, multi-module Maven.
- Swing with FlatLaf, with tabpro's own light and dark themes.
- Gson for the native `.tabpro` format.
- `javax.sound.midi` for playback and MIDI capture, and Gervill for
  SoundFont banks.
- [Tabler Icons](https://tabler.io/icons) for pictograms and the
  [Bravura](https://github.com/steinbergmedia/bravura) font for music symbols.

## Modules

Dependencies point one way, toward `tabpro-core`. The interface reaches file
formats and MIDI only through three ports defined in core: `ScoreFiles`,
`ScoreExchange` and `Player`. `tabpro-format` and `tabpro-midi` do not know
about each other: each one implements half of `ScoreExchange`, notation in
one and sound in the other, and `tabpro-app` composes the two halves.

| Module | Responsibility |
|---|---|
| `tabpro-core` | The immutable score model, the editing session with undo and redo, notation, harmony, the wizards and playback. |
| `tabpro-format` | The native format, reading and writing foreign notation (MIDI, ASCII and MusicXML import), and the `.gp3`/`.gp4`/`.gp5`/`.gtp` reader. |
| `tabpro-midi` | Playback, MIDI capture and rendering sound to files: `.mid` and `.wav` export. |
| `tabpro-ui` | The interface: score, fretboard, keyboard, percussion, mix table, global view, status bar, menus, toolbars and dialogs. |
| `tabpro-app` | `main`, the theme, the wiring (including composing the exchange) and packaging. |
| `tabpro-tests` | No code of its own: it gathers the suite and runs it. |

## Tests

Tests live next to the code they test, but they do not run there. Surefire
starts one JVM per module, and each JVM pays startup and class loading
again, which dominates a short suite: five modules would mean five warm-ups
for tests that, once the classes are loaded, run in under half a second.
`tabpro-tests` gathers the `test-classes` of the five modules and runs them
once, in a single JVM with the JIT limited to C1.

Because of that, `mvn test -pl <module>` runs no tests. Run the suite from the
repository root:

```sh
mvn verify
```

CI runs two more passes: the suite under a Spanish locale, since the
interface ships in English and Spanish, and the integration tests, which need
a display, under Xvfb:

```sh
mvn test -Dtests.locale="-Duser.language=es -Duser.country=ES"
xvfb-run -a mvn test -Dtests.headless=false -Dgroups=integration \
  -Dtests.excluded.groups= -Dsurefire.failIfNoSpecifiedTests=false
```

## Guitar Pro compatibility

The Guitar Pro reader and writer are checked against
[PyGuitarPro](https://github.com/Perlence/PyGuitarPro) and the 61 authentic
files in its test suite (ten `.gp3`, sixteen `.gp4` and thirty-five `.gp5`),
not against files tabpro wrote itself. A reader and a writer that share the
same wrong assumption agree with each other, so the oracle has to come from
outside.

Sixty of the sixty-one files open, and the one that does not cannot be opened
by PyGuitarPro either. Before that check, twenty-four opened, and only one of
the thirty-five `.gp5` files did. The corpus is LGPL, so it is never
committed; [audit-corpus.md](audit-corpus.md) records a later audit against
it.

## Workflow

- Every behavior change starts with a failing test.
- Each change goes on its own branch and opens a pull request against `main`.
  CI gates the merge.
- Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/).
- Every push to `main` runs [semantic-release](https://semantic-release.gitbook.io/)
  once CI is green. The commit types decide the version (`feat` bumps the
  minor, `fix` and `perf` the patch; `docs`, `chore`, `refactor`, `test`, `ci`
  and `style` release nothing). It then updates the poms, the version in the
  README and `CHANGELOG.md`, tags the release, builds the `.deb` and publishes
  it on GitHub.

Plans and audits live next to this file: [PLAN.md](PLAN.md),
[audit-manual.md](audit-manual.md), [audit-real-use.md](audit-real-use.md)
and [audit-corpus.md](audit-corpus.md).
