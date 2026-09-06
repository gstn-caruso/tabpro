package com.gstncaruso.tabpro.format.tabledit;

import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.VoicePart;

/** Lo que trae un silencio de TablEdit: su duracion y en cual de las dos voces cae. */
record TabEditRestFields(Duration duration, VoicePart voice) {
}
