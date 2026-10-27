# Project review and sample GP5 validation

Date: **2026-10-27**. Reviewed base: **`75b25a5`**, `main`, tabpro **0.60.0**.
This is a documentation-only review, not a set of implemented fixes or an exhaustive audit.

## Summary

The existing module boundaries, immutable score model and independent Guitar Pro corpus
checks are useful foundations; no architectural rewrite is justified by this review.
The supplied GP5 opens through the real reader and agrees with an independent parser on
all compared notes and the other fields below, after explicit normalization. One metadata
difference remains; rendering and sound were not checked. Prioritize preserving users'
work, then XML input safety and interchange fidelity, before responsiveness improvements.

## Validation evidence

The coordinating review supplied the full-suite and Xvfb results below; they were **not
rerun for this documentation change**. The focused baseline was executed directly for
this change. Times are Maven total times, not test-only timings.

| Run | Tests | Failures / errors / skipped | Result | Time |
|---|---:|---|---|---:|
| Coordinator: full `verify` | 5,210 | 0 / 0 / 1 | BUILD SUCCESS | 11.978 s |
| Coordinator: Xvfb integration | 132 | 0 / 0 / 0 | BUILD SUCCESS | 27.081 s |
| Documentation: focused baseline | 56 | 0 / 0 / 0 | BUILD SUCCESS | 1.026 s |

Commands, from the repository root, in the same order:

```sh
ASDF_JAVA_VERSION='temurin-25.0.4+7.0.LTS' mvn -o -B verify
ASDF_JAVA_VERSION='temurin-25.0.4+7.0.LTS' xvfb-run -a mvn -o -B test -Dtests.headless=false -Dgroups=integration -Dtests.excluded.groups= -Dsurefire.failIfNoSpecifiedTests=false
ASDF_JAVA_VERSION='temurin-25.0.4+7.0.LTS' mvn -o -B test -Dtest=GuitarProFileTest -Dsurefire.failIfNoSpecifiedTests=false
```

The skipped case is the optional GP5 demo corpus, not a failed import. The coordinator
also reported that `java` without an asdf version selection failed locally; the commands
select the installed JDK without changing project configuration. The focused check is
importer regression evidence, not a test of the user-supplied file. Initial
`git diff --check` also passed. These green suites do not reproduce the risks below.

## Supplied GP5: what was actually compared

The local, untracked `docs/sample.gp5` is **76,644 bytes**, with a **GP5.10** header.
SHA-256 before and after the supplied experiment:
`8c3f98be40038769efc6a3e56ae9b1739c9038213ce71d37ee63beaf5a835dbb`.
The documentation pass independently confirmed its size and hash before editing.
The score's redistribution license is unknown: do not commit it, attach it to the PR,
or copy it into the fixture corpus.

The reverse-engineering reviewer supplied a session experiment, **not rerun here**:
JShell invoked the real `GuitarProFile(new TestDefaultNames()).read(Path)` reader;
Python independently used `guitarpro.parse` from **PyGuitarPro 0.11**. Scripts were fed
through stdin (`python3 -B -` for Python), with Gson JSON and comparison results on
stdout, without auxiliary files. This is transient session evidence: no reusable
comparison script or log was retained or versioned, so these entry points are a method
summary, not a complete replayable command.

Both parsers reported **“Green Day - 21 Guns”**, header tempo **160**, **9 tracks** and
**211 measures per track**. Note counts, in file order:

| Track | Notes |
|---|---:|
| Pista 1 | 1,310 |
| Pista 5 | 1,146 |
| Pista 9 | 227 |
| Pista 3 | 494 |
| Percusión | 1,177 |
| Pista 6 | 421 |
| Pista 7 | 421 |
| Pista 8 | 84 |
| Pista 2 | 268 |
| **Total** | **5,548** |

Compared successfully, after the normalization described next:

- String, fret and tie on all **5,548 notes**, including **351 ties**.
- Note value, dots and tuplet fields on **5,820 comparable beats**; nominal duration
  for all **3,798 track/measure/voice pairs**.
- Tuning and capo on all **9 tracks**; dynamics and duration percentage on every note.
- Present effects: **144 legato slides, 13 vibratos, 2 bends, 24 dead notes and
  63 fade-ins**.
- **17 automations**, including values, transitions and scope. In particular, track 9
  contains tempo **140 in measure 1** and **165 in measure 10**. Header tempo 160 is
  therefore not evidence that playback stays at 160.

Explicitly normalized representation differences: zero- versus one-based MIDI channels;
volume **128 → 127** on tracks 6, 7 and 9; pan **128 → 127** on track 7; equivalent bend
scales; and **2,767 PyGuitarPro empty-status beats** represented as note-free entries
in Tabpro. Each such empty beat was the sole entry of its voice, not an interleaved
empty beat. This normalization is sample-specific, not a blanket rule for other files,
and does not establish that boundary clamping is perceptually inaudible.

**Only residual difference in the compared fields:** percussion `fretCount` is **87**
in PyGuitarPro and **36** in Tabpro. The reader clamps this metadata in
[GuitarProFile.java:202–213](../tabpro-format/src/main/java/com/gstncaruso/tabpro/format/guitarpro/GuitarProFile.java#L202-L213).
This is **not observed note loss**: all 1,177 percussion notes match, including **857**
with values above 36, up to **57**. The effect on editing or rendering is unverified.

No visual inspection, listening, playback timing, RSE validation or comparison of
unlisted fields was performed. This supports import fidelity for the compared data,
not a claim of perfect GP5 compatibility. For repeatability, a follow-up could retain
an opt-in semantic comparison harness, identify the oracle version and input hash,
and keep the score outside Git unless redistribution rights are established. The
[existing fixture policy](../tabpro-format/src/test/resources/guitarpro/README.md)
already distinguishes original fixtures from optional external scores.

## Prioritized improvements

All source references below were checked against `75b25a5`. These are **static findings
or inferred risks, not reproduced UI, filesystem or security failures**. Priority
reflects potential impact; the next scenario is proposed work, not a test added here.

### H1 — High: do not discard work after a cancelled or failed save

- **Evidence:** [MainFrame.java:157](../tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/MainFrame.java#L157) uses `DISPOSE_ON_CLOSE`, without a `windowClosing` discard guard. The [confirmation at :680–695](../tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/MainFrame.java#L680-L695) returns true after calling the UI's void `save()`, even when Save As was cancelled or an error was caught (:454–477); `CLOSED_OPTION` also falls through to true. New/Open (:398–427) and Quit (:637–640) consume that result.
- **Observable condition:** a dirty score, followed by window close, or “Yes, save” followed by a cancelled chooser or failed write, can allow closing/replacing the score without a successful save.
- **Smallest improvement:** permit the transition only after a confirmed save or explicit discard; cancellation, closed confirmation and write failure must preserve the current document. Route window close through the same decision.
- **Next scenario:** edit an untitled score, choose New, answer Yes, cancel Save As; the original score remains dirty and open. Then cover write failure, confirmation close and the window close button separately.

### H2 — High: recovery must not become a saved temporary document

- **Evidence:** [MainFrame.java:241–271](../tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/MainFrame.java#L241-L271) routes accepted recovery through normal open. [ScoreDocument.java:85–89](../tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/ScoreDocument.java#L85-L89) sets the recovery path as the document destination and calls `markSaved`; :143–146 marks it clean and :123–126 deletes recovery. All instances share the temporary path (:106–107).
- **Observable condition:** accepted recovery is treated as clean and its disk copy is deleted. A later Save targets the same temporary name and deletes it again through `markSaved`, rather than creating a durable user-selected file. Separate instances can also share or remove each other's recovery file; this was not exercised.
- **Smallest improvement:** recover as dirty, without a save destination, and retain recovery until a durable save or explicit discard; define ownership of recovery files between instances.
- **Next scenario:** accept recovery, verify it remains recoverable and dirty, then cancel Save As without losing the recovery copy. A successful save to a normal path may retire that copy.

### H3 — High: preserve the previous native file if saving fails

- **Evidence:** [JsonScoreFiles.java:35–39](../tabpro-format/src/main/java/com/gstncaruso/tabpro/format/JsonScoreFiles.java#L35-L39) writes directly to the destination with `Files.writeString`.
- **Observable condition:** interruption or an I/O failure after truncation could destroy the previous valid score. This is an inferred durability risk; no destructive experiment was run.
- **Smallest improvement:** write a temporary sibling, then replace atomically where supported, with explicit failure semantics that preserve the previous copy rather than silently degrading to truncation.
- **Next scenario:** start with a valid saved score, inject failure before replacement, and verify its original bytes remain readable and unchanged.

### H4 — High, potential security issue: explicitly restrict XML external access

- **Evidence:** [MusicXmlScoreImporter.java:58–65](../tabpro-format/src/main/java/com/gstncaruso/tabpro/format/exchange/musicxml/MusicXmlScoreImporter.java#L58-L65) disables external DTD loading, but does not explicitly disable external general/parameter entities or set `ACCESS_EXTERNAL_DTD` / `ACCESS_EXTERNAL_SCHEMA`. Actual exposure depends on parser defaults; no exploit was reproduced.
- **Observable condition:** an untrusted MusicXML file could attempt external entity resolution; the current configuration alone is insufficient evidence that all external access is prohibited.
- **Smallest improvement:** explicitly restrict entity resolution and external-access properties. Keep accepting the legitimate DOCTYPE emitted by [MusicXmlScoreExporter.java:32–34](../tabpro-format/src/main/java/com/gstncaruso/tabpro/format/exchange/musicxml/MusicXmlScoreExporter.java#L32-L34) without fetching its DTD.
- **Next scenario:** use an innocuous temporary canary entity, never secrets, and verify no external content is read; separately reimport a normal Tabpro MusicXML export offline.

### H5 — High: avoid silent MusicXML voice and timing loss

- **Evidence:** [MusicXmlScoreExporter.java:95–101](../tabpro-format/src/main/java/com/gstncaruso/tabpro/format/exchange/musicxml/MusicXmlScoreExporter.java#L95-L101) iterates `measure.beats()`, which means only the lead voice ([Measure.java:40–42](../tabpro-core/src/main/java/com/gstncaruso/tabpro/core/model/Measure.java#L40-L42)). [MusicXmlScoreImporter.java:158–179](../tabpro-format/src/main/java/com/gstncaruso/tabpro/format/exchange/musicxml/MusicXmlScoreImporter.java#L158-L179) builds one flat beat list without interpreting `voice` or `backup`.
- **Observable condition:** exporting a score with BASS content omits it; importing simultaneous voices can serialize their notes and alter timing. These paths were read, not demonstrated with a new fixture.
- **Smallest improvement:** support voice/timing semantics, or explicitly warn/reject the unsupported case before silently losing data. Successful reopen alone is not a fidelity check.
- **Next scenario:** an original one-measure, two-voice score must retain both simultaneous lines and their durations, or report the unsupported operation explicitly.

### M1 — Medium: keep the cursor valid when inserting in BASS

- **Evidence:** [Editor.java:336–347](../tabpro-core/src/main/java/com/gstncaruso/tabpro/core/editing/Editor.java#L336-L347) can activate BASS and then insert a measure while retaining that voice. [Score.java:123–128](../tabpro-core/src/main/java/com/gstncaruso/tabpro/core/model/Score.java#L123-L128) inserts an empty measure whose BASS is unused. `change` assigns the cursor unchanged (:887–897); `currentBeat` indexes that empty voice (:104–109).
- **Observable condition:** `editVoice(BASS)`, `insertMeasure()`, then `currentBeat()` can address beat zero in an unused voice. This is a static finding, not an executed crash.
- **Smallest improvement:** initialize the selected voice or normalize the cursor to a valid voice/beat when inserting; do not weaken the model's invariants.
- **Next scenario:** insert while editing BASS, read the current beat and undo; both resulting editor states must have valid cursors.

## Later: bounded follow-up work

- **Input-size limits (medium, inferred):** [GuitarProFile.java:63–83](../tabpro-format/src/main/java/com/gstncaruso/tabpro/format/guitarpro/GuitarProFile.java#L63-L83), :111 and :173–174 reserve collections from file counters; [PowerTabByteReader.java:145–147](../tabpro-format/src/main/java/com/gstncaruso/tabpro/format/powertab/PowerTabByteReader.java#L145-L147) does the same. Negative-count checks and final model limits do not bound large positive reservations before allocation. No OOM was induced. Bound counts against remaining input and format limits; next, reject an oversized truncated input with a controlled file error before large allocation.
- **WAVE responsiveness (medium, unmeasured):** [MainFrame.java:530–543](../tabpro-ui/src/main/java/com/gstncaruso/tabpro/ui/MainFrame.java#L530-L543) calls `exchange.exportWave` synchronously in the UI action. A long export can occupy the EDT; no stall duration was measured. Measure it, then move work off the EDT with completion/error feedback. The next scenario checks that UI events can still be processed while export is pending.
- **Percussion metadata (impact unknown):** investigate the sample's `fretCount` difference using original or opt-in data. First verify that a percussion value above 36 survives editing/rendering before deciding whether clamping needs a behavior change.

## Suggested delivery order

Use small follow-up PRs: H1, H2 and H3 protect work; H4 hardens an untrusted input
boundary; H5 and M1 address fidelity and editor validity. Each should first reproduce
its smallest observable scenario, then change behavior through a separate TDD cycle.
Do not infer that tests are generally absent from these findings or treat this report
as authorization to implement them.

For the current documentation PR, include only this report and its development-guide
reference; leave the sample untracked. Explain the reviewed revision, measured versus
static evidence, GP5 limitations and next increment in the PR description. No production
structural tidying is useful for delivering this report; defer it rather than inventing
an abstraction or bundling fixes with documentation.
