# PowerTab fixtures

The 18 `.ptb` files (and the accompanying `.pt2`) are **third-party
content**, unlike the Guitar Pro fixtures: they are real files taken as-is
from the [powertabeditor](https://github.com/powertab/powertabeditor)
project's own repository (GPLv3 license), from the
`test/formats/powertab_old/data/` directory, where they are part of its own
test suite (`test/formats/powertab_old/test_powertabold.cpp`). Each one
exercises a single section of the format:

- `song_header.ptb` — the song's data (title, artist, author, etc.).
- `guitars.ptb` — guitars and tunings, across the file's two "scores".
- `barlines.ptb` — barline type, key signature and time signature.
- `staves.ptb` — staves, clef and string count.
- `positions.ptb` — figure, rest, double dot and compressed multibar rest.
- `notes.ptb` — string, fret and note effects (natural harmonic, tapped,
  trill, tie, ghost note, octave).
- `alternate_endings.ptb` — alternate endings (1st, 2nd ending) and D.C./D.S.
- `tempo_markers.ptb` — a standard tempo marker.
- `guitar_ins.ptb` — reassigning guitars to staves mid-piece (the format's
  "guitar in").
- `chordtext.ptb`, `chord_diagrams.ptb` — chord name and diagram.
- `directions.ptb` — navigation symbols (Coda, Segno).
- `floating_text.ptb` — loose text over the score.
- `bends.ptb`, `tremolo_bars.ptb`, `volume_swells.ptb` — bend, tremolo bar
  and volume swells.
- `merge_multibar_rests.ptb` (+ `merge_multibar_rests_correct.pt2`) —
  another case of compressed multibar rest.

## What tabpro reads from each one

tabpro's reader (`tabpro-format/.../powertab/`) does not cover everything
the original powertabeditor suite tests: structure (measures, tracks,
tuning), notes and their durations, and a reasonable set of per-note effects
are supported; what is not gets a clear exception instead of a guess — see
`PowerTabFile`'s javadoc. In particular:

- `positions.ptb` and `merge_multibar_rests.ptb` **must fail** to import:
  they use a compressed multibar rest, which is not supported.
- `guitar_ins.ptb` **must fail**: it reassigns a staff to another guitar
  mid-piece.
- `chord_diagrams.ptb`, `chordtext.ptb`, `floating_text.ptb`,
  `directions.ptb`, `bends.ptb`, `tremolo_bars.ptb` and `volume_swells.ptb`
  import without error, but those specific sections are discarded on
  purpose (they do not change a note's pitch, duration or string: they are
  annotation and decoration).

## License

These files remain under the source project's license (GPLv3), not under
tabpro's MIT license — and **that is on purpose, not an oversight**: the
decision to leave them as they are, instead of replacing them with
synthetic ones, was made and documented here so it is clear the next time
someone reads it. They are test data, not code distributed as part of the
program: the same reasoning by which any test suite versions third-party
fixtures to test a reader for a foreign format. The reader code itself
(`tabpro-format/.../powertab/`) belongs to tabpro, MIT like the rest of the
project; see its javadoc for the provenance of the specification it does
use.
</content>
