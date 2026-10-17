# Guitar Pro fixtures

## `tabpro-synthetic.*`

All three files carry the same musical content written in the three format
generations: one guitar track in standard tuning, two 4/4 measures of quarter
notes, with an ascending C major scale on the fifth string.

## `tabpro-features.*`

The same three formats, with four measures and three tracks that exercise
everything the reader has to understand: dynamics, bend, tie, slide, natural
harmonic, a second voice (only in gp5, the only generation where the format
has one), a time signature change to 3/4, a repeat with alternate endings, a
chord diagram, a four-string bass track and a percussion track on channel 10.

## `tabpro-effects2.*`

Three measures with the effects the reader branches on by version: a pair of
tied notes that forms a real tie, vibrato, trill, tremolo bar and the
artificial, tapped, pinch and semitone harmonics. GP3 does not support the
trill, the tremolo bar or any harmonic other than natural or artificial: they
get lost on save there, and that is not a reader bug.

## `tabpro-features-v5.00.gp5`

The same content as `tabpro-features.gp5` but saved as v5.00, which stores
tracks differently: the flags byte comes before all of them instead of just
the first one, and the RSE instrument takes one byte less.

## Origin

This is **original content**, generated on purpose to test tabpro's reader.
It is not material from Guitar Pro or Arobas Music, so it can be versioned
and redistributed with the rest of the project under the MIT license.

## `gp5-demos.csv`

This file contains only paths, note fingerprints, counts and initial tempos for the 40
GP5.2 demo scores; it contains no scores. PyGuitarPro 0.11 generated these
independent expectations. The optional `GuitarProDemoCorpusTest` reads the
originals from the `tabpro.gp5.demos` system property and checks track/bar/note
counts, tempo and the note-position fingerprint.

The note fingerprint is SHA-256 of UTF-8 lines in track, measure, voice and
beat order, with notes sorted by string. Each line is
`track:measure:voice:beat:string:fret:tie` followed by LF. The four positions
are zero-based; the string number is one-based and tie is `0` or `1`.
No-note beats contribute no lines. This covers 16,493 notes; it does not
check durations, effect curves, rendering or sound.
</content>
