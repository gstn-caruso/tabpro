package com.gstncaruso.tabpro.format.tabledit;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;

/** The time signature and key signature of a measure, as TablEdit carries them. */
record TabEditMeasure(TimeSignature timeSignature, KeySignature keySignature) {
}
