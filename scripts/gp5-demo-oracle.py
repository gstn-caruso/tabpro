#!/usr/bin/env python3
"""Record non-musical corpus metadata using PyGuitarPro, independently of tabpro."""
import argparse
import csv
import hashlib
import sys
from pathlib import Path

import guitarpro

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("demos", type=Path, help="Guitar Pro 5 Demo Files directory")
args = parser.parse_args()
files = sorted(args.demos.rglob("*.gp5"))
if not files:
    parser.error("the directory contains no .gp5 files")
writer = csv.writer(sys.stdout, delimiter="|", lineterminator="\n")
writer.writerow(["path", "file_sha256", "tracks", "bars", "tempo", "notes", "notes_sha256"])
for path in files:
    score = guitarpro.parse(str(path))
    fingerprint = hashlib.sha256()
    count = 0
    for ti, track in enumerate(score.tracks):
        for mi, measure in enumerate(track.measures):
            for vi, voice in enumerate(measure.voices):
                for bi, beat in enumerate(voice.beats):
                    for note in sorted(beat.notes, key=lambda note: note.string):
                        tied = int(note.type == guitarpro.NoteType.tie)
                        fingerprint.update(
                            f"{ti}:{mi}:{vi}:{bi}:{note.string}:{note.value}:{tied}\n".encode("utf-8")
                        )
                        count += 1
    writer.writerow([
        path.relative_to(args.demos).as_posix(),
        hashlib.sha256(path.read_bytes()).hexdigest(),
        len(score.tracks), len(score.measureHeaders), score.tempo, count,
        fingerprint.hexdigest(),
    ])
