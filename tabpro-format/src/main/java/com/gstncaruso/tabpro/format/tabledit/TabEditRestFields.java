package com.gstncaruso.tabpro.format.tabledit;

import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.VoicePart;

/** What a TablEdit rest carries: its duration and which of the two voices it falls in. */
record TabEditRestFields(Duration duration, VoicePart voice) {
}
