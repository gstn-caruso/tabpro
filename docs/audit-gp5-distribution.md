# Local Guitar Pro 5.2 distribution audit

2026-10-17. Sources were supplied locally under
`~/Descargas/guitar pro 5/rea-analysis/extracted/app`. This pass used the
extracted data and scores directly, without executing or decompiling GP5.
No executables, fonts, samples, serial numbers or vendor scores were added
to tabpro.

## Tuning catalog

`data/Tunings.dat` is a text catalog of open-string MIDI pitches, listed
from the last string to the first. Its SHA-256 is
`adde38db908a9c0be1c73ed4af8311d5422ca1021b971bfd8487b26d69b1ccbb`.
Tabpro stores strings in the opposite order: string 1 comes first.

Three existing presets disagreed with that catalog:

| Preset | Previous pitches, string 1 first | Corrected pitches |
|---|---|---|
| Open D5 | 62, 57, 54, 50, 45, 38 | 62, 57, 50, 50, 45, 38 |
| Open F | 65, 60, 57, 53, 45, 41 | 65, 60, 53, 48, 45, 41 |
| Open G6 | 62, 59, 55, 50, 45, 38 | 64, 59, 55, 50, 43, 38 |

The previous Open D5 duplicated Open D major, including its F-sharp.
The previous Open G6 contained A instead of the sixth, E. Open F used a
different voicing; the preset now matches the GP5 voicing.

Nine presets were added: guitar two steps down, Drop D variant, Double
Drop D, Drop E, Open Cmaj7, Open D6 and Open Esus11; bass two steps down;
and lute/vihuela. All have Spanish and English labels and persistent
names in `.tabpro` files. The catalog now has 52 presets.

Existing scores keep their saved pitches, including scores saved with the
previous preset definitions. Regression tests verify this explicitly.
These are curated musical definitions, not a wholesale import of GP5's
catalog: other instrument octave choices and tuning variants were left as
they were in tabpro.

## Authentic demo corpus

All 40 files in `Demo Files` opened successfully before these changes,
including the 133-bar complete RSE demo. The independent PyGuitarPro 0.11
reader then supplied expected track counts, bar counts, initial tempos,
note counts and a fingerprint of every note's track, measure, voice, beat,
string, fret and tie state. All 16,493 notes agree with tabpro.

The optional `GuitarProDemoCorpusTest` preserves that comparison for later
changes. The repository stores only aggregate metadata and SHA-256 hashes;
it requires the original local demo files. A file hash mismatch fails the
test instead of comparing a different score against an old expectation.
`scripts/gp5-demo-oracle.py` regenerates the expectations using PyGuitarPro,
independently of tabpro's importer.

This establishes agreement on the tested fields. Durations, effects,
rendering, audio and GP5 export are outside this comparison.
