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
</content>
